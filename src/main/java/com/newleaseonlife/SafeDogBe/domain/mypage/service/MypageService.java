package com.newleaseonlife.SafeDogBe.domain.mypage.service;

import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypagePetResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageGuardianPermissionResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.enums.MypagePetScope;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.service.PetService;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.MypageErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

  private final UserService userService;
  private final PetService petService;

  /**
   * 마이페이지 초기 조회:
   * - 내 프로필
   * - 반려동물 scope(OWNER/SHARED)에 따른 목록(오래된 순)
   * - 각 반려동물의 보호자 목록
   */
  /**
   * @param petScopeQuery GET 파라미터 petScope 원문 (null/공백 → OWNER, 그 외 OWNER·SHARED만 허용)
   */
  public MypageResponse getMypage(Long userId, String petScopeQuery) {
    MypagePetScope petScope = resolvePetScope(petScopeQuery);
    UserResponse user = userService.findById(userId);

    // [기획 반영] 반려동물 목록: 등록된 날짜(오래된) 순 정렬
    List<PetResponse> pets = (petScope == MypagePetScope.SHARED)
        ? petService.findMySharedPetsOrderByCreatedAtAsc(userId)
        : petService.findMyPetsOrderByCreatedAtAsc(userId);

    List<MypagePetResponse> petSections = pets.stream()
        .map(pet -> {
          List<PetGuardianResponse> guardians = petService.getGuardiansForPet(pet.getId(), userId);
          List<MypageGuardianPermissionResponse> permissionResponses = guardians.stream()
              .map(this::toPermissionResponse)
              .toList();
          return MypagePetResponse.builder()
              .pet(pet)
              .guardians(permissionResponses)
              .build();
        })
        .toList();

    return MypageResponse.builder()
        .user(user) // 프론트엔드에서 user.nickname ?? user.name으로 처리하거나 DTO에서 가공
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

    // [기획 반영] 탈퇴 회원 이름 처리 규칙
    String displayName = guardian.isUserDeleted() ? "알 수 없음" : guardian.getNickname();

    return MypageGuardianPermissionResponse.builder()
        .userId(guardian.getUserId())
        .nickname(displayName) // 가공된 이름 전달
        .role(guardian.getRole())
        // 동물 정보 수정/삭제/초대/관리자 변경: OWNER만 가능
        .canEditPetInfo(isOwner)
        .canDeletePet(isOwner)
        .canInviteGuardian(isOwner)
        .canMoveToAdminChangePage(isOwner)
        .canChangeAdminOwner(isOwner)
        // 케어 요청/체크 권한: OWNER/CAREGIVER 모두 가능(기획 기준)
        .canRequestCare(true)
        .canCheckPetNote(true)
        // 반려노트 등록/수정: OWNER만 가능
        .canCreateOrUpdatePetNote(isOwner)
        .build();
  }
}

