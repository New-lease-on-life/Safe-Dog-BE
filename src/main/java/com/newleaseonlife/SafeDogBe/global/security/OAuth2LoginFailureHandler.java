package com.newleaseonlife.SafeDogBe.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 실패 시 리다이렉트. 쿼리 파라미터 error=oauth2_login_failed, message=예외 메시지로 전달.
 */
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri-after-login:/}")
    private String redirectUriAfterLogin;

    /**
     * OAuth2 인증 실패 시 FE로 리다이렉트.
     * 예외 메시지에 {@code "KEY|설명"} 형식이 포함된 경우 error 파라미터를 KEY로 분리하여 전달.
     * FE는 error 파라미터로 화면을 분기한다.
     *
     * <ul>
     *   <li>{@code error=ACCOUNT_RESTORE_EXPIRED} — 탈퇴 30일 초과, 복구 불가 안내</li>
     *   <li>{@code error=DUPLICATE_ACCOUNT} — 동일 이름 타 소셜 계정 존재</li>
     *   <li>{@code error=oauth2_login_failed} — 기타 일반 실패</li>
     * </ul>
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String rawMessage = exception.getMessage() != null ? exception.getMessage() : "";

        // "KEY|상세 메시지" 형식 파싱: KEY → error 파라미터, 상세 → message 파라미터
        String errorCode;
        String message;
        if (rawMessage.contains("|")) {
            String[] parts = rawMessage.split("\\|", 2);
            errorCode = parts[0];
            message   = parts[1];
        } else if (rawMessage.startsWith("DUPLICATE_ACCOUNT:")) {
            // "DUPLICATE_ACCOUNT:{provider}|..." 는 위 분기에서 처리되지만 혹시 | 없을 때 대비
            errorCode = "DUPLICATE_ACCOUNT";
            message   = rawMessage;
        } else {
            errorCode = "oauth2_login_failed";
            message   = rawMessage;
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUriAfterLogin)
                .queryParam("error",   errorCode)
                .queryParam("message", message)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
