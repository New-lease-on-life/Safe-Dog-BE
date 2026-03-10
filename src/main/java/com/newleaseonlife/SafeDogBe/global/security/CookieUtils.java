package com.newleaseonlife.SafeDogBe.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * 인증 쿠키(access_token, refresh_token) 추가·삭제·읽기.
 * HttpOnly, SameSite=Lax, path/secure는 설정에 따라 적용. refresh_token은 /api/auth/refresh 경로로만 전송.
 */
@Component
public class CookieUtils {

    /** Access Token 쿠키 이름. path=/. */
    public static final String ACCESS_TOKEN_COOKIE  = "access_token";
    /** Refresh Token 쿠키 이름. path=/api/auth/refresh 로만 전송. */
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";
    private static final int    REFRESH_TOKEN_MAX_AGE_SECONDS = 14 * 24 * 60 * 60; // 14일

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    // ------------------------------------------------------------------
    // 쿠키 추가
    // ------------------------------------------------------------------

    public void addAccessTokenCookie(HttpServletResponse response, String token, long expiresInMs) {
        int maxAge = (int) (expiresInMs / 1000);
        addCookie(response, ACCESS_TOKEN_COOKIE, token, maxAge, "/");
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        addCookie(response, REFRESH_TOKEN_COOKIE, token, REFRESH_TOKEN_MAX_AGE_SECONDS, REFRESH_TOKEN_PATH);
    }

    // ------------------------------------------------------------------
    // 쿠키 삭제 (로그아웃)
    // ------------------------------------------------------------------

    public void deleteTokenCookies(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE, "/");
        deleteCookie(response, REFRESH_TOKEN_COOKIE, REFRESH_TOKEN_PATH);
    }

    // ------------------------------------------------------------------
    // 쿠키 읽기 (static — HttpServletRequest만 필요하므로 주입 불필요)
    // ------------------------------------------------------------------

    public static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // 내부 헬퍼
    // ------------------------------------------------------------------

    private void addCookie(HttpServletResponse response, String name, String value,
                           int maxAgeSeconds, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
