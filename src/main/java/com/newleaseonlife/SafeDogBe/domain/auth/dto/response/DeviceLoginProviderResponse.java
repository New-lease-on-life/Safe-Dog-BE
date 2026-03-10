package com.newleaseonlife.SafeDogBe.domain.auth.dto.response;

import java.time.LocalDateTime;

/**
 * 기기 기준 마지막 로그인 소셜 타입 응답.
 * FE 로그인 화면에서 "이전에 {providerDescription}(으)로 로그인했어요" 툴팁에 사용.
 *
 * @param deviceId            기기 식별자
 * @param lastLoginProvider   마지막 로그인 소셜 타입 (LOCAL / GOOGLE / NAVER / KAKAO)
 * @param lastLoginAt         마지막 로그인 일시
 */
public record DeviceLoginProviderResponse(
        String deviceId,
        String lastLoginProvider,
        LocalDateTime lastLoginAt
) {
}
