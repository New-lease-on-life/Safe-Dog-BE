package com.newleaseonlife.SafeDogBe.domain.user.dto.response;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 회원 프로필 응답. GET /api/users/me, 프로필 수정·온보딩 완료 API 응답에 사용.
 * password, status 등 민감/내부 필드는 제외.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String name;
    private LocalDate birthDate;
    private ProviderType providerType;
    private String profileImageUrl;
    private UserRole role;
    /** 온보딩 완료 여부. true 이면 최초 설정 완료로 간주 */
    private boolean isOnboardingCompleted;
}
