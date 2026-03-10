package com.newleaseonlife.SafeDogBe.domain.pet.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.GuardianAddRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.converter.PetConverter;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 반려동물(Pet) 도메인 서비스. CRUD, 보호자(Guardian) 추가·삭제·목록 조회.
 * 반려동물 소유자(pet.user)만 수정·삭제·보호자 관리 가능.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final PetGuardianRepository petGuardianRepository;
    private final UserRepository userRepository;
    private final CareTemplateRepository careTemplateRepository;
    private final PetConverter petConverter;

    /** 내가 소유한 반려동물 목록(최신순) */
    public List<PetResponse> findMyPets(Long userId) {
        log.debug("[PetService] findMyPets userId={}", userId);
        List<Pet> pets = petRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return petConverter.toResponseList(pets);
    }

    /** 반려동물 단건 조회. 소유자만 조회 가능(보호자 목록 포함 조회는 getGuardians 사용) */
    public PetResponse findById(Long petId, Long userId) {
        log.debug("[PetService] findById petId={}, userId={}", petId, userId);
        return petConverter.toResponse(getPetAsOwnerOrThrow(petId, userId));
    }

    /** 반려동물 등록. 요청자를 메인 보호자(pet.user)로 저장. 질병 입력 시 질병별 CareTemplate 자동 생성 */
    @Transactional
    public PetResponse create(Long userId, PetCreateRequest request) {
        log.info("[PetService] create userId={}, name={}", userId, request.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        boolean isNeutered = request.getIsNeutered() != null && request.getIsNeutered();
        Set<PetDisease> diseases = request.getDiseases() != null ? request.getDiseases() : Set.of();
        Pet pet = Pet.builder()
                .user(user)
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .isNeutered(isNeutered)
                .profileImageUrl(request.getProfileImageUrl())
                .diseases(diseases)
                .build();
        petRepository.save(pet);

        // 질병 유형별 기본 케어 템플릿 자동 생성
        if (!diseases.isEmpty()) {
            createDefaultCareTemplates(pet, diseases);
        }

        log.info("[PetService] create 완료 petId={}, diseases={}", pet.getId(), diseases);
        return petConverter.toResponse(pet);
    }

    /** 질병 목록을 기반으로 PetDisease에 정의된 기본 CareTemplate 일괄 생성 */
    private void createDefaultCareTemplates(Pet pet, Set<PetDisease> diseases) {
        List<CareTemplate> templates = new ArrayList<>();
        for (PetDisease disease : diseases) {
            for (PetDisease.DefaultTemplate tmpl : disease.getDefaultTemplates()) {
                templates.add(CareTemplate.builder()
                        .pet(pet)
                        .careType(tmpl.careType())
                        .title("[" + disease.getDescription() + "] " + tmpl.title())
                        .content(tmpl.content())
                        .repeatCycle(tmpl.repeatCycle())
                        .build());
            }
        }
        careTemplateRepository.saveAll(templates);
        log.info("[PetService] 질병 기반 CareTemplate {}개 자동 생성 petId={}", templates.size(), pet.getId());
    }

    /** 반려동물 정보 수정. 소유자만 가능 */
    @Transactional
    public PetResponse update(Long petId, Long userId, PetUpdateRequest request) {
        log.info("[PetService] update petId={}, userId={}", petId, userId);
        Pet pet = getPetAsOwnerOrThrow(petId, userId);
        pet.update(
                request.getName(),
                request.getSpecies(),
                request.getBreed(),
                request.getBirthDate(),
                request.getGender(),
                request.getIsNeutered(),
                request.getProfileImageUrl()
        );
        log.info("[PetService] update 완료 petId={}", petId);
        return petConverter.toResponse(pet);
    }

    /** 반려동물 삭제. 소유자만 가능. pet_guardian 행은 cascade로 함께 삭제 */
    @Transactional
    public void delete(Long petId, Long userId) {
        log.info("[PetService] delete petId={}, userId={}", petId, userId);
        Pet pet = getPetAsOwnerOrThrow(petId, userId);
        petRepository.delete(pet);
        log.info("[PetService] delete 완료 petId={}", petId);
    }

    /** 해당 반려동물의 보호자 목록 조회. 소유자만 호출 가능 */
    public List<PetGuardianResponse> getGuardians(Long petId, Long userId) {
        log.debug("[PetService] getGuardians petId={}, userId={}", petId, userId);
        Pet pet = getPetAsOwnerOrThrow(petId, userId);
        List<PetGuardian> guardians = petGuardianRepository.findByPetIdOrderByIdAsc(pet.getId());
        return petConverter.toGuardianResponseList(guardians);
    }

    /** 보호자 추가. 소유자만 가능. 이미 등록된 사용자면 PET_GUARDIAN_ALREADY_EXISTS */
    @Transactional
    public PetGuardianResponse addGuardian(Long petId, Long ownerUserId, GuardianAddRequest request) {
        log.info("[PetService] addGuardian petId={}, ownerUserId={}, targetUserId={}, role={}",
                petId, ownerUserId, request.getUserId(), request.getRole());
        Pet pet = getPetAsOwnerOrThrow(petId, ownerUserId);
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        if (petGuardianRepository.existsByPetIdAndUserId(pet.getId(), request.getUserId())) {
            log.warn("[PetService] addGuardian 실패 - 이미 보호자로 등록됨 petId={}, userId={}", petId, request.getUserId());
            throw new BusinessException(PetErrorCode.PET_GUARDIAN_ALREADY_EXISTS);
        }
        PetGuardian guardian = PetGuardian.builder()
                .user(targetUser)
                .pet(pet)
                .role(request.getRole())
                .build();
        // pet.getGuardians().add()와 repository.save() 동시 호출 시 중복 INSERT 가능 → save()만 사용
        PetGuardian saved = petGuardianRepository.save(guardian);
        log.info("[PetService] addGuardian 완료 guardianId={}", saved.getId());
        return petConverter.toGuardianResponse(saved);
    }

    /** 보호자 제거. 소유자만 가능. 지정한 userId의 보호자 연결만 삭제 */
    @Transactional
    public void removeGuardian(Long petId, Long ownerUserId, Long guardianUserId) {
        log.info("[PetService] removeGuardian petId={}, ownerUserId={}, guardianUserId={}", petId, ownerUserId, guardianUserId);
        Pet pet = getPetAsOwnerOrThrow(petId, ownerUserId);
        PetGuardian guardian = petGuardianRepository.findByPetIdAndUserId(pet.getId(), guardianUserId)
                .orElseThrow(() -> {
                    log.warn("[PetService] removeGuardian 실패 - 보호자 없음 petId={}, userId={}", petId, guardianUserId);
                    return new BusinessException(PetErrorCode.PET_GUARDIAN_NOT_FOUND);
                });
        pet.getGuardians().remove(guardian);
        log.info("[PetService] removeGuardian 완료 guardianId={} (orphanRemoval)", guardian.getId());
    }

    /** 반려동물 조회(소유자만). 없거나 권한 없으면 예외 */
    private Pet getPetAsOwnerOrThrow(Long petId, Long userId) {
        return petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> {
                    if (petRepository.findById(petId).isEmpty()) {
                        return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                    }
                    return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
                });
    }
}
