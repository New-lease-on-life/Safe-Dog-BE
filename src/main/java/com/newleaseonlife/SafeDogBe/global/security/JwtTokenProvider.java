package com.newleaseonlife.SafeDogBe.global.security;

import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;
import com.newleaseonlife.SafeDogBe.global.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Access/Refresh Token 발급·검증·파싱.
 * JwtProperties의 시크릿과 만료 시간을 사용하며, claim에 userId, email, role, type(access/refresh)을 담는다.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;

    /** Access Token 생성 (type=access, JwtProperties.accessTokenExpiration 적용). */
    public String createAccessToken(Long userId, String email, UserRole role) {
        return createToken(
                userId,
                email,
                role,
                TOKEN_TYPE_ACCESS,
                jwtProperties.getAccessTokenSecret(),
                jwtProperties.getAccessTokenExpiration()
        );
    }

    /** Refresh Token 생성 (type=refresh, JwtProperties.refreshTokenExpiration 적용). */
    public String createRefreshToken(Long userId, String email, UserRole role) {
        return createToken(
                userId,
                email,
                role,
                TOKEN_TYPE_REFRESH,
                jwtProperties.getRefreshTokenSecret(),
                jwtProperties.getRefreshTokenExpiration()
        );
    }

    private String createToken(Long userId, String email, UserRole role, String type,
                               String secret, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_USER_ID, userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Access Token에서 userId claim 추출. */
    public Long getUserIdFromAccessToken(String token) {
        Claims claims = parseToken(token, jwtProperties.getAccessTokenSecret());
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    /** Access Token 서명·만료·type 검증. */
    public boolean validateAccessToken(String token) {
        return validateToken(token, jwtProperties.getAccessTokenSecret(), TOKEN_TYPE_ACCESS);
    }

    /** Refresh Token 서명·만료·type 검증. */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, jwtProperties.getRefreshTokenSecret(), TOKEN_TYPE_REFRESH);
    }

    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = parseToken(token, jwtProperties.getRefreshTokenSecret());
        return claims.get(CLAIM_USER_ID, Long.class);
    }

    private boolean validateToken(String token, String secret, String expectedType) {
        try {
            Claims claims = parseToken(token, secret);
            String type = claims.get(CLAIM_TYPE, String.class);
            return expectedType.equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpiration();
    }
}
