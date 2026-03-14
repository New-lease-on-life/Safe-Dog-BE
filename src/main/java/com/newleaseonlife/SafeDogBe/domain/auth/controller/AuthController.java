package com.newleaseonlife.SafeDogBe.domain.auth.controller;

import com.newleaseonlife.SafeDogBe.domain.auth.service.dto.SocialSignupCompleteRequest;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.CheckDuplicateRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SocialLinkRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.DeviceLoginProviderResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CookieUtils;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API. 회원가입·로컬 로그인·토큰 갱신·로그아웃·중복 체크.
 * 토큰은 HttpOnly 쿠키로 발급(쿠키 우선), 바디에도 포함하여 모바일 앱도 지원.
 */
@Tag(name = "Auth", description = "회원가입, 로그인, 토큰 관리, 소셜 계정 연결, 기기 로그인 기록 API")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;

    @Operation(summary = "전화번호+이름 중복 체크", description = "중복이면 409(기존 소셜 타입 포함 메시지), 없으면 204")
    @GetMapping("/check-duplicate")
    public ResponseEntity<Void> checkDuplicate(@Valid CheckDuplicateRequest request) {
        log.info("[AuthController] 중복 체크 요청 phone={}, name={}", request.phone(), request.name());
        userService.checkDuplicateByPhoneAndName(request.phone(), request.name());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "소셜 최초 가입 완료", description = "약관 동의와 초대 코드를 받아 PENDING 상태를 ACTIVE로 전환합니다.")
    @PostMapping("/social-signup-complete")
    public ResponseEntity<Void> completeSocialSignup(
        @AuthenticationPrincipal CustomPrincipal principal,
        @Valid @RequestBody SocialSignupCompleteRequest request) {
        log.info("[AuthController] 소셜 가입 완료 요청 userId={}", principal.getUser().getId());
        authService.completeSocialSignup(principal.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 갱신", description = "쿠키 refresh_token 우선, 없으면 바디에서 읽음. 새 토큰 쿠키+바디 반환")
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
    // 💡 앱스토어 심사 통과를 위한 백도어 API (운영 환경에서는 이메일을 통한 권한 탈취 주의)
    @Operation(summary = "테스트 계정 로그인 (운영 환경 주의)", description = "앱스토어 심사용 백도어 API")
    @PostMapping("/test-login")
    public ResponseEntity<TokenResponse> testLogin(
        @RequestParam String email, HttpServletResponse response) {
        log.info("[AuthController] 테스트 로그인 요청 email={}", email);
        User testUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        TokenResponse tokenResponse = authService.issueTokenResponse(testUser);
        cookieUtils.addAccessTokenCookie(response, tokenResponse.getAccessToken(), tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "쿠키 삭제 + refresh_token DB 무효화")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        log.info("[AuthController] logout 요청");

        String refreshToken = resolveRefreshToken(httpRequest, request);
        authService.logout(refreshToken);
        cookieUtils.deleteTokenCookies(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "기기별 마지막 로그인 소셜 타입 조회", description = "FE 툴팁용. 등록된 기기 없으면 204")
    /**
     * 기기별 마지막 로그인 소셜 타입 조회. 비인증 공개 API.
     * FE가 deviceId로 조회하여 로그인 화면에 "이전에 카카오로 로그인했어요" 툴팁 표시.
     * 등록된 기기 없으면 204 반환.
     */
    @GetMapping("/devices/{deviceId}/login-provider")
    public ResponseEntity<DeviceLoginProviderResponse> getDeviceLoginProvider(@PathVariable String deviceId) {
        log.info("[AuthController] getDeviceLoginProvider deviceId={}", deviceId);
        DeviceLoginProviderResponse response = authService.getDeviceLoginProvider(deviceId);
        if (response == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기기별 로그인 소셜 타입 기록", description = "로그인 성공 후 FE가 호출. provider=KAKAO 등 전달")
    @PutMapping("/devices/{deviceId}/login-provider")
    public ResponseEntity<DeviceLoginProviderResponse> registerDeviceLogin(
            @PathVariable String deviceId,
            @RequestParam String provider) {
        log.info("[AuthController] registerDeviceLogin deviceId={}, provider={}", deviceId, provider);
        DeviceLoginProviderResponse response = authService.registerDeviceLogin(deviceId, provider);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소셜 계정 연결", description = "이미 로그인된 사용자가 추가 소셜 계정 연결. FE는 OAuth2 완료 후 providerId 전달")
    @PostMapping("/social-accounts")
    public ResponseEntity<Void> linkSocialAccount(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody SocialLinkRequest request) {
        log.info("[AuthController] linkSocialAccount userId={}, provider={}", principal.getUser().getId(), request.provider());
        authService.linkSocialAccount(principal.getUser().getId(), request);
        return ResponseEntity.noContent().build();
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
