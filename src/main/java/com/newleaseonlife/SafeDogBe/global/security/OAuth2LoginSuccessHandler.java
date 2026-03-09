package com.newleaseonlife.SafeDogBe.global.security;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 토큰 발급·쿠키 설정 후 리다이렉트.
 * AuthService.issueTokenResponse로 Access/Refresh 토큰 발급 후 쿠키에 담고, app.oauth2.redirect-uri-after-login으로 이동.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Value("${app.oauth2.redirect-uri-after-login:/}")
    private String redirectUriAfterLogin;

    /** 토큰 발급 → 쿠키 추가 → redirectUriAfterLogin으로 리다이렉트 */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        TokenResponse tokenResponse = authService.issueTokenResponse(principal.getUser());

        cookieUtils.addAccessTokenCookie(response, tokenResponse.getAccessToken(),
                tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        log.info("[OAuth2LoginSuccessHandler] 소셜 로그인 성공 - 쿠키 발급 userId={}",
                principal.getUser().getId());

        getRedirectStrategy().sendRedirect(request, response, redirectUriAfterLogin);
    }
}
