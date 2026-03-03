package com.newleaseonlife.SafeDogBe.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String accsecret;
    private String refsecret;
    private long accessTokenExpiration = 1_800_000L;    // 30분
    private long refreshTokenExpiration = 1_209_600_000L; // 14일 (CookieUtils REFRESH_TOKEN_MAX_AGE와 동일)
}
