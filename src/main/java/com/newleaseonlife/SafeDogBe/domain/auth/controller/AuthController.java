package com.newleaseonlife.SafeDogBe.domain.auth.controller;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.CheckDuplicateRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.LoginRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SignupRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtils cookieUtils;

    /**
     * 전화번호 + 이름 기준 중복 체크. 중복이면 409, 없으면 204.
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<Void> checkDuplicate(@Valid CheckDuplicateRequest request) {
        log.info("[AuthController] 중복 체크 요청 phone={}, name={}", request.phone(), request.name());
        userService.checkDuplicateByPhoneAndName(request.phone(), request.name());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        log.info("[AuthController] signup 요청 email={}, nickname={}", request.getEmail(), request.getNickname());
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인 성공 시 HttpOnly 쿠키로 토큰 발급.
     * JSON 바디에도 토큰 포함 (모바일 앱 호환용).
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletResponse response) {
        log.info("[AuthController] login 요청 email={}", request.getEmail());
        TokenResponse tokenResponse = authService.login(request);
        cookieUtils.addAccessTokenCookie(response, tokenResponse.getAccessToken(),
                tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 토큰 갱신: 쿠키의 refresh_token 우선 사용, 없으면 요청 바디에서 읽음.
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("[AuthController] refresh 요청");

        String refreshToken = resolveRefreshToken(httpRequest, request);
        TokenResponse tokenResponse = authService.refresh(new RefreshTokenRequest(refreshToken));

        cookieUtils.addAccessTokenCookie(httpResponse, tokenResponse.getAccessToken(),
                tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(httpResponse, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    /**
     * 로그아웃: 쿠키 삭제 + refresh_token DB에서 무효화.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("[AuthController] logout 요청");

        String refreshToken = resolveRefreshToken(httpRequest, request);
        authService.logout(refreshToken);
        cookieUtils.deleteTokenCookies(httpResponse);
        return ResponseEntity.ok().build();
    }

    /**
     * refresh_token 추출: 쿠키 우선 → 요청 바디 폴백.
     * 둘 다 없으면 INVALID_REFRESH_TOKEN 예외.
     */
    private String resolveRefreshToken(HttpServletRequest httpRequest, RefreshTokenRequest body) {
        String fromCookie = CookieUtils.readCookie(httpRequest, CookieUtils.REFRESH_TOKEN_COOKIE);
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie;
        }
        if (body != null && StringUtils.hasText(body.getRefreshToken())) {
            return body.getRefreshToken();
        }
        throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
