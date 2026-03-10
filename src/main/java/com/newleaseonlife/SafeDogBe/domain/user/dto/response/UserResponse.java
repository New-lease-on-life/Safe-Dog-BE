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

    /** 회원 PK */
    private Long id;
    /** 이메일 (소셜 전용 가입 시 null 가능) */
    private String email;
    /** 닉네임 */
    private String nickname;
    /** 실명 */
    private String name;
    /** 생년월일 */
    private LocalDate birthDate;
    /** 가입 경로 (LOCAL / GOOGLE / NAVER / KAKAO) */
    private ProviderType providerType;
    /** 프로필 이미지 URL */
    private String profileImageUrl;
    /** 권한 (USER / ADMIN) */
    private UserRole role;
    /** 온보딩 완료 여부. true이면 최초 설정 완료로 간주 */
    private boolean isOnboardingCompleted;
}
