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
  public MypageResponse getMypage(Long userId, MypagePetScope petScope) {
    UserResponse user = userService.findById(userId);

    List<PetResponse> pets = (petScope == MypagePetScope.SHARED)
        ? petService.findMySharedPetsOrderByCreatedAtAsc(userId)
        : petService.findMyPetsOrderByCreatedAtAsc(userId);

    List<MypagePetResponse> petSections = pets.stream()
        .map(pet -> {
          List<PetGuardianResponse> guardians =
              petService.getGuardiansForPet(pet.getId(), userId);
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
        .user(user)
        .pets(petSections)
        .build();
  }

  private MypageGuardianPermissionResponse toPermissionResponse(PetGuardianResponse guardian) {
    boolean isOwner = guardian.getRole() != null && guardian.getRole().name().equals("OWNER");

    return MypageGuardianPermissionResponse.builder()
        .userId(guardian.getUserId())
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

