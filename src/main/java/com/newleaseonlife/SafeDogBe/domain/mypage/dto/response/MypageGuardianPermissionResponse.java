package com.newleaseonlife.SafeDogBe.domain.mypage.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypageGuardianPermissionResponse {

  private Long userId;
  private String nickname;
  private PetGuardianRole role;

  // 기능 별 권한 (FE에서 O/X 표시용)
  private boolean canEditPetInfo;
  private boolean canDeletePet;
  private boolean canInviteGuardian;
  private boolean canMoveToAdminChangePage;

  private boolean canChangeAdminOwner;

  // 케어 요청/체크는 (기획 기준) OWNER/CAREGIVER 모두 O
  private boolean canRequestCare;
  private boolean canCheckPetNote;

  // 반려노트 등록/수정은 OWNER만 O
  private boolean canCreateOrUpdatePetNote;
}