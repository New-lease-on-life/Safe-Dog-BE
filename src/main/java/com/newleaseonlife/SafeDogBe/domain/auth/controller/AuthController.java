package com.newleaseonlife.SafeDogBe.domain.auth.controller;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService;
import com.newleaseonlife.SafeDogBe.domain.auth.service.dto.SocialSignupCompleteRequest;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CookieUtils;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth 도메인(개발완_v1)", description = "인증 및 회원가입 API (Naver 소셜 로그인 전용)")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;
    private final UserRepository userRepository;

    @Operation(summary = "소셜 최초 가입 완료 (온보딩)", description = "필수 약관 동의, 생년월일, 초대 코드를 받아 PENDING 상태의 회원을 ACTIVE로 전환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "가입 완료 및 정식 회원(ACTIVE) 전환 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = "application/json", examples = {
            // Service 계층의 비즈니스 에러
            @ExampleObject(name = "만 14세 미만", value = "{\"code\": 400, \"message\": \"만 14세 이상만 가입할 수 있습니다.\"}"),
            @ExampleObject(name = "생년월일 누락", value = "{\"code\": 400, \"message\": \"생년월일 입력은 필수입니다.\"}"),
            @ExampleObject(name = "필수 약관 미동의", value = "{\"code\": 400, \"message\": \"서비스 이용을 위해 필수 정보 제공이 필요합니다.\"}"),

            // Controller 계층의 @Valid 에러 (추가)
            @ExampleObject(name = "미래 날짜 입력(@Past)", value = "{\"code\": 400, \"message\": \"생년월일은 과거의 날짜여야 합니다.\"}"),
            @ExampleObject(name = "약관 목록 누락(@NotEmpty)", value = "{\"code\": 400, \"message\": \"약관 동의 목록은 필수입니다.\"}"),
        })),
        @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음 (토큰 오류 등)", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"사용자를 찾을 수 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "409", description = "상태 충돌", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "이미 활성화된 회원", value = "{\"code\": 409, \"message\": \"이미 온보딩을 완료한 계정입니다.\"}")
        }))
    })
    @PostMapping("/social-signup-complete")
    public ResponseEntity<Void> completeSocialSignup(
        @AuthenticationPrincipal CustomPrincipal principal,
        @Valid @RequestBody SocialSignupCompleteRequest request) {
        log.info("[AuthController] 소셜 가입 완료 요청 userId={}", principal.getUser().getId());
        authService.completeSocialSignup(principal.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 갱신", description = "만료된 Access Token을 갱신합니다. \n" +
        "1. 쿠키의 'refresh_token'을 최우선으로 사용합니다. \n" +
        "2. 쿠키가 없는 환경(모바일 등)에서는 Body의 refreshToken을 사용합니다."
        )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공 (쿠키 및 바디에 포함)"),

        @ApiResponse(responseCode = "404", description = "DB에서 회원 정보를 찾을 수 없음 (탈퇴 등)", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"사용자를 찾을 수 없습니다.\"}")
        })),

        @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "INVALID_REFRESH_TOKEN", value = "{\"code\": 401, \"message\": \"유효하지 않은 리프레시 토큰입니다.\"}")
        }))
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {
        String refreshToken = resolveRefreshToken(httpRequest, request);
        TokenResponse tokenResponse = authService.refresh(new RefreshTokenRequest(refreshToken));
        cookieUtils.addAccessTokenCookie(httpResponse, tokenResponse.getAccessToken(),
            tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(httpResponse, tokenResponse.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "현재 기기에서 로그아웃 처리하며 DB의 Refresh Token을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "로그아웃 성공 (응답 바디 없음)"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @RequestBody(required = false) RefreshTokenRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse) {
        String refreshToken = resolveRefreshToken(httpRequest, request);
        authService.logout(refreshToken);
        cookieUtils.deleteTokenCookies(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "테스트 계정 로그인/가입 (🚨 운영 환경 배포 주의)",
        description = "실제 네이버 연동 없이 강제로 유저를 생성하고 토큰을 발급하는 백도어 API입니다.")
    @PostMapping("/test-login")
    @Transactional
    public ResponseEntity<TokenResponse> testLogin(
        @RequestParam String email,
        @RequestParam(defaultValue = "true") boolean autoActivate, // true면 바로 ACTIVE, false면 PENDING
        HttpServletResponse response) {

        log.warn("[AuthController] 🚨 테스트 로그인 API 호출됨 email={}", email);

        // 1. 이메일로 기존 유저 조회, 없으면 새로 강제 생성 (네이버 우회)
        User testUser = userRepository.findByEmail(email)
            .orElseGet(() -> {
                log.info("[AuthController] 테스트 계정이 존재하지 않아 새로 생성합니다. email={}", email);
                String randomNickname = "Tester_" + UUID.randomUUID().toString().substring(0, 5);

                return userRepository.save(User.builder()
                    .email(email)
                    .nickname(randomNickname)
                    .name("테스트유저")
                    .status(autoActivate ? UserStatus.ACTIVE : UserStatus.PENDING) // 상태 선택 생성
                    .role(UserRole.USER)
                    .providerType(ProviderType.LOCAL) // 백도어 식별용
                    .build());
            });

        // 2. 로그인 처리 및 토큰 발급
        testUser.updateLastLogin("TEST_BACKDOOR");
        TokenResponse tokenResponse = authService.issueTokenResponse(testUser);

        // 3. 쿠키 설정 (실제 로그인과 동일한 환경 제공)
        cookieUtils.addAccessTokenCookie(response, tokenResponse.getAccessToken(), tokenResponse.getAccessTokenExpiresIn());
        cookieUtils.addRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    // (testLogin 메서드 생략 - 운영 API가 아니므로 어노테이션 최소화)

    private String resolveRefreshToken(HttpServletRequest httpRequest, RefreshTokenRequest body) {
        // ... (기존 로직 동일)
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
