package com.newleaseonlife.SafeDogBe.domain.auth.dto.request;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.OAuthProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 소셜 계정 연결 요청. 이미 로그인된 사용자가 추가 소셜 계정을 연결할 때 사용.
 * FE는 소셜 OAuth2 플로우를 완료한 후 수신한 providerId와 provider를 전달해야 함.
 *
 * @param provider   연결할 소셜 제공자 (GOOGLE / NAVER / KAKAO)
 * @param providerId 소셜 제공자에서 발급한 사용자 식별자
 */
public record SocialLinkRequest(
        @NotNull(message = "소셜 제공자는 필수입니다")
        OAuthProvider provider,

        @NotBlank(message = "소셜 식별자는 필수입니다")
        String providerId
) {
}
