package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 홈 화면 상단 반려동물 프로필 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePetProfileResponse {

    private Long id;
    private String name;
    private String profileImageUrl;

    /** 현재 사용자의 역할 (OWNER / CAREGIVER) */
    private String role;
}
