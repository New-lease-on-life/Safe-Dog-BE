package com.newleaseonlife.SafeDogBe.domain.mypage.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypagePetResponse {

  /** 반려동물 */
  private PetResponse pet;

  /** 해당 반려동물의 보호자 목록 */
  private List<MypageGuardianPermissionResponse> guardians;
}

