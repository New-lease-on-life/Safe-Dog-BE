package com.newleaseonlife.SafeDogBe.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 설정 프로퍼티. application.yaml의 jwt.* 키와 바인딩된다.
 * access-token-secret, refresh-token-secret은 환경 변수(JWT_ACCSECRET, JWT_REFSECRET)로 설정 권장.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Access Token 서명용 시크릿 (환경 변수 JWT_ACCSECRET 권장) */
    private String accessTokenSecret;
    /** Refresh Token 서명용 시크릿 (환경 변수 JWT_REFSECRET 권장) */
    private String refreshTokenSecret;
    /** Access Token 유효 기간(ms). 기본 30분 */
    private long accessTokenExpiration = 1_800_000L;
    /** Refresh Token 유효 기간(ms). 기본 14일 (CookieUtils REFRESH_TOKEN_MAX_AGE와 맞출 것) */
    private long refreshTokenExpiration = 1_209_600_000L;
}
