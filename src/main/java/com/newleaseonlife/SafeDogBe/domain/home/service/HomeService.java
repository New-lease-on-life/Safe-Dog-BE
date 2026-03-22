package com.newleaseonlife.SafeDogBe.domain.home.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeCareProgressResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeChecklistCategoryResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeChecklistItemResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomePetListResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomePetProfileResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomePetSummaryResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;
import com.newleaseonlife.SafeDogBe.domain.petnote.repository.PetNoteRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.HomeErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final int NOTE_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PetGuardianRepository petGuardianRepository;
    private final PetNoteRepository petNoteRepository;
    private final DailyChecklistRepository dailyChecklistRepository;

    /**
     * API 1: 반려동물 선택 저장.
     * 해당 사용자가 petId 반려동물에 접근 권한이 있는지 검증 후 lastSelectedPetId 갱신.
     */
    @Transactional
    public void selectPet(Long userId, Long petId) {
        User user = getUserOrThrow(userId);
        if (!petGuardianRepository.existsByPetIdAndUserId(petId, userId)) {
            throw new BusinessException(HomeErrorCode.HOME_PET_ACCESS_DENIED);
        }
        user.updateLastSelectedPet(petId);
        log.info("[HomeService] selectPet userId={}, petId={}", userId, petId);
    }

    /**
     * API 2: 반려동물 목록 조회.
     * 직접 등록(OWNER) + 공유받은(CAREGIVER) 반려동물을 합쳐 등록일 오름차순으로 반환.
     * lastSelectedPetId를 함께 내려줌.
     */
    public HomePetListResponse getPetList(Long userId) {
        User user = getUserOrThrow(userId);

        List<PetGuardian> guardians = petGuardianRepository.findByUserId(userId);

        List<HomePetSummaryResponse> pets = guardians.stream()
                .sorted(Comparator.comparing(g -> g.getPet().getCreatedAt()))
                .map(g -> {
                    Pet pet = g.getPet();
                    boolean isOwner = g.getRole() == PetGuardianRole.OWNER;
                    return HomePetSummaryResponse.builder()
                            .id(pet.getId())
                            .name(pet.getName())
                            .profileImageUrl(pet.getProfileImageUrl())
                            .birthDate(pet.getBirthDate())
                            .isBirthDateUnknown(pet.isBirthDateUnknown())
                            .species(pet.getSpecies())
                            .registrationType(isOwner ? "OWNER" : "SHARED")
                            .role(g.getRole().name())
                            .build();
                })
                .toList();

        return HomePetListResponse.builder()
                .lastSelectedPetId(user.getLastSelectedPetId())
                .pets(pets)
                .build();
    }

    /**
     * API 3: 홈 화면 데이터 통합 조회.
     * 반려동물 프로필, 케어 진행률, 메모 목록, 체크리스트 존재 여부를 한 번에 반환.
     */
    public HomeResponse getHomeData(Long userId, Long petId) {
        User user = getUserOrThrow(userId);

        List<PetGuardian> allGuardians = petGuardianRepository.findByUserId(userId);
        boolean hasPets = !allGuardians.isEmpty();

        if (!hasPets) {
            return HomeResponse.builder()
                    .hasPets(false)
                    .build();
        }

        // petId가 없으면 lastSelectedPetId → 없으면 가장 최근 등록 반려동물
        Long resolvedPetId = resolveSelectedPetId(user, petId, allGuardians);

        PetGuardian myGuardian = petGuardianRepository.findByPetIdAndUserId(resolvedPetId, userId)
                .orElseThrow(() -> new BusinessException(HomeErrorCode.HOME_PET_ACCESS_DENIED));
        Pet pet = myGuardian.getPet();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        HomePetProfileResponse petProfile = HomePetProfileResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .profileImageUrl(pet.getProfileImageUrl())
                .role(myGuardian.getRole().name())
                .build();

        long totalCount = dailyChecklistRepository.countByPet_IdAndTargetDate(resolvedPetId, today);
        long completedCount = dailyChecklistRepository.countByPet_IdAndTargetDateAndIsCompleted(resolvedPetId, today, true);

        HomeCareProgressResponse careProgress = HomeCareProgressResponse.builder()
                .totalCount(totalCount)
                .completedCount(completedCount)
                .build();

        // 메모: 최신 20건, 마지막 로그인 이후 작성된 메모는 isNew=true
        LocalDateTime lastLoginAt = user.getLastLoginAt();
        List<PetNote> latestNotes = petNoteRepository.findLatestByPetIdWithWriter(
                resolvedPetId, PageRequest.of(0, NOTE_PAGE_SIZE));

        // 각 메모 작성자의 역할을 구하기 위해 guardianMap 준비
        Map<Long, String> guardianRoleMap = buildGuardianRoleMap(resolvedPetId);

        List<HomeNoteResponse> notes = latestNotes.stream().map(note -> {
            Long writerId = note.getWrittenBy() != null ? note.getWrittenBy().getId() : null;
            boolean isNew = lastLoginAt != null && note.getCreatedAt() != null
                    && note.getCreatedAt().isAfter(lastLoginAt);
            return HomeNoteResponse.builder()
                    .id(note.getId())
                    .content(note.getContent())
                    .writtenByUserId(writerId)
                    .writtenByNickname(note.getWrittenBy() != null ? note.getWrittenBy().getNickname() : null)
                    .writtenByProfileImageUrl(note.getWrittenBy() != null ? note.getWrittenBy().getProfileImageUrl() : null)
                    .writtenByRole(writerId != null ? guardianRoleMap.getOrDefault(writerId, null) : null)
                    .sentAt(note.getCreatedAt())
                    .isNew(isNew)
                    .linkedChecklistId(note.getLinkedChecklistId())
                    .build();
        }).toList();

        boolean hasChecklist = dailyChecklistRepository.existsByPet_IdAndTargetDate(resolvedPetId, today);

        return HomeResponse.builder()
                .hasPets(true)
                .petProfile(petProfile)
                .careProgress(careProgress)
                .notes(notes)
                .hasChecklist(hasChecklist)
                .build();
    }

    /**
     * API 4: 체크리스트 조회 (탭/카테고리 구분).
     * DISEASE_CARE → 질병 케어 탭, 그 외 → 기본 케어 탭.
     * 항목이 있는 카테고리만 반환.
     */
    public HomeChecklistResponse getChecklists(Long userId, Long petId, LocalDate date) {
        if (!petGuardianRepository.existsByPetIdAndUserId(petId, userId)) {
            throw new BusinessException(HomeErrorCode.HOME_PET_ACCESS_DENIED);
        }

        List<DailyChecklist> checklists = dailyChecklistRepository
                .findAllByPetIdAndTargetDateWithUser(petId, date);

        // careType 기준으로 그룹핑 (등장 순서 유지)
        Map<CareType, List<HomeChecklistItemResponse>> groupMap = new LinkedHashMap<>();
        for (DailyChecklist cl : checklists) {
            groupMap.computeIfAbsent(cl.getCareType(), k -> new ArrayList<>())
                    .add(toChecklistItemResponse(cl));
        }

        List<HomeChecklistCategoryResponse> basicCare = new ArrayList<>();
        List<HomeChecklistCategoryResponse> diseaseCare = new ArrayList<>();

        for (Map.Entry<CareType, List<HomeChecklistItemResponse>> entry : groupMap.entrySet()) {
            HomeChecklistCategoryResponse category = HomeChecklistCategoryResponse.builder()
                    .careType(entry.getKey().name())
                    .careTypeDescription(entry.getKey().getDescription())
                    .items(entry.getValue())
                    .build();
            if (entry.getKey() == CareType.DISEASE_CARE) {
                diseaseCare.add(category);
            } else {
                basicCare.add(category);
            }
        }

        return HomeChecklistResponse.builder()
                .basicCare(basicCare)
                .diseaseCare(diseaseCare)
                .build();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private HomeChecklistItemResponse toChecklistItemResponse(DailyChecklist cl) {
        return HomeChecklistItemResponse.builder()
                .id(cl.getId())
                .title(cl.getTitle())
                .content(cl.getContent())
                .isCompleted(cl.isCompleted())
                .isRequested(cl.getCareTemplate() != null)
                .completedAt(cl.getCompletedAt())
                .completedByProfileImageUrl(
                        cl.getCompletedBy() != null ? cl.getCompletedBy().getProfileImageUrl() : null)
                .completedByNickname(
                        cl.getCompletedBy() != null ? cl.getCompletedBy().getNickname() : null)
                .version(cl.getVersion())
                .build();
    }

    /** 보호자 userId → role 문자열 맵 */
    private Map<Long, String> buildGuardianRoleMap(Long petId) {
        Map<Long, String> map = new HashMap<>();
        petGuardianRepository.findByPetIdOrderByIdAsc(petId)
                .forEach(g -> map.put(g.getUser().getId(), g.getRole().name()));
        return map;
    }

    /**
     * petId를 결정하는 우선순위:
     * 1. 파라미터로 전달된 petId (접근 권한 검증 후)
     * 2. lastSelectedPetId (보유 목록에 있는 경우)
     * 3. 가장 최근에 등록된 반려동물
     */
    private Long resolveSelectedPetId(User user, Long petId, List<PetGuardian> guardians) {
        if (petId != null) {
            boolean hasAccess = guardians.stream().anyMatch(g -> g.getPet().getId().equals(petId));
            if (hasAccess) return petId;
        }
        Long lastId = user.getLastSelectedPetId();
        if (lastId != null) {
            boolean stillValid = guardians.stream().anyMatch(g -> g.getPet().getId().equals(lastId));
            if (stillValid) return lastId;
        }
        // 가장 최근 등록 반려동물(createdAt 최신)
        return guardians.stream()
                .max(Comparator.comparing(g -> g.getPet().getCreatedAt()))
                .map(g -> g.getPet().getId())
                .orElseThrow(() -> new BusinessException(HomeErrorCode.HOME_PET_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HomeErrorCode.HOME_USER_NOT_FOUND));
    }
}
