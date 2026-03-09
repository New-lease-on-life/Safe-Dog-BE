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

    /** redirectUriAfterLogin?error=oauth2_login_failed&message=... 로 리다이렉트 */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUriAfterLogin)
                .queryParam("error", "oauth2_login_failed")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
