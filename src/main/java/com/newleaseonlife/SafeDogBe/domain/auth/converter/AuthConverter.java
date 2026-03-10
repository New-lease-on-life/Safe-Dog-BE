package com.newleaseonlife.SafeDogBe.domain.auth.converter;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;

import org.springframework.stereotype.Component;

/** 인증 관련 엔티티·값 → 응답 DTO 변환 */
@Component
public class AuthConverter {

    private static final String GRANT_TYPE = "Bearer";

    /** Access/Refresh Token 값을 TokenResponse DTO로 변환 */
    public TokenResponse toTokenResponse(String accessToken, String refreshToken, Long accessTokenExpiresIn) {
        return TokenResponse.builder()
                .grantType(GRANT_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn)
                .build();
    }
}
