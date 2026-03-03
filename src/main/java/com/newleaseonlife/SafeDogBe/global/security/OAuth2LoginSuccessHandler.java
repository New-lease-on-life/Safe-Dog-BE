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

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Value("${app.oauth2.redirect-uri-after-login:/}")
    private String redirectUriAfterLogin;

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
