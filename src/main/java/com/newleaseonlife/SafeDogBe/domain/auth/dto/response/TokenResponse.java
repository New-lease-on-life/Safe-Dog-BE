package com.newleaseonlife.SafeDogBe.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인·토큰 갱신 응답. HttpOnly 쿠키와 함께 바디에도 포함(모바일 호환).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /** 인증 방식. 항상 "Bearer" */
    private String grantType;
    /** JWT Access Token */
    private String accessToken;
    /** JWT Refresh Token */
    private String refreshToken;
    /** Access Token 만료 시간(ms) */
    private Long accessTokenExpiresIn;
}
