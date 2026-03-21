package com.newleaseonlife.SafeDogBe.domain.mypage.dto.response;

import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypageResponse {

  /** 내 프로필 */
  private UserResponse user;

  /** 내가 OWNER인 반려동물 목록(각 항목별 보호자 목록 포함) */
  private List<MypagePetResponse> pets;
}

