package com.newleaseonlife.SafeDogBe.domain.mypage.service;

import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageGuardianPermissionResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypagePetResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.enums.MypagePetScope;
import com.newleaseonlife.SafeDogBe.domain.pet.converter.PetConverter;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.MypageErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

  private final UserService userService;
  private final PetGuardianRepository petGuardianRepository; // 추가
  private final PetConverter petConverter; // 추가

  /**
   * 마이페이지 초기 조회 (성능 최적화 버전)
   */
  public MypageResponse getMypage(Long userId, String petScopeQuery) {
    MypagePetScope petScope = resolvePetScope(petScopeQuery);
    PetGuardianRole targetRole = (petScope == MypagePetScope.SHARED) ? PetGuardianRole.CAREGIVER : PetGuardianRole.OWNER;

    UserResponse user = userService.findById(userId);

    // 1️⃣ [성능 개선] Fetch Join으로 펫 정보와 모든 보호자 정보를 단 1번의 쿼리로 가져옴
    List<PetGuardian> allGuardianships = petGuardianRepository.findAllMyPetsWithGuardiansByRole(userId, targetRole);

    // 2️⃣ 펫별로 보호자 리스트를 그룹화 (N+1 발생 원천 차단)
    // LinkedHashMap을 사용하여 DB에서 넘어온 정렬 순서(createdAt ASC)를 유지합니다.
    Map<Pet, List<PetGuardian>> petGroupedMap = allGuardianships.stream()
        .collect(Collectors.groupingBy(PetGuardian::getPet, LinkedHashMap::new, Collectors.toList()));

    // 3️⃣ DTO 변환 (이미 데이터를 다 들고 있으므로 루프 내부 추가 쿼리 0개)
    List<MypagePetResponse> petSections = petGroupedMap.entrySet().stream()
        .map(entry -> {
          Pet pet = entry.getKey();
          List<PetGuardianResponse> guardianResponses = entry.getValue().stream()
              .map(petConverter::toGuardianResponse)
              .toList();

          return MypagePetResponse.builder()
              .pet(petConverter.toResponse(pet))
              .guardians(guardianResponses.stream().map(this::toPermissionResponse).toList())
              .build();
        })
        .toList();

    return MypageResponse.builder()
        .user(user)
        .pets(petSections)
        .build();
  }

  private MypagePetScope resolvePetScope(String value) {
    if (value == null || value.isBlank()) {
      return MypagePetScope.OWNER;
    }
    try {
      return MypagePetScope.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BusinessException(MypageErrorCode.MYPAGE_INVALID_PET_SCOPE);
    }
  }

  private MypageGuardianPermissionResponse toPermissionResponse(PetGuardianResponse guardian) {
    boolean isOwner = guardian.getRole() != null && guardian.getRole().name().equals("OWNER");

    // PetGuardianResponse에 닉네임과 탈퇴여부 필드가 보강되어야 정상 작동함
    // 만약 에러가 난다면 PetConverter.toGuardianResponse를 수정해야 합니다.
    String displayName = guardian.getNickname() == null ? "알 수 없음" : guardian.getNickname();

    return MypageGuardianPermissionResponse.builder()
        .userId(guardian.getUserId())
        .nickname(displayName)
        .role(guardian.getRole())
        .canEditPetInfo(isOwner)
        .canDeletePet(isOwner)
        .canInviteGuardian(isOwner)
        .canMoveToAdminChangePage(isOwner)
        .canChangeAdminOwner(isOwner)
        .canRequestCare(true)
        .canCheckPetNote(true)
        .canCreateOrUpdatePetNote(isOwner)
        .build();
  }
}