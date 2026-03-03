package com.newleaseonlife.SafeDogBe.domain.auth.controller;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.CheckDuplicateRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.LoginRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SignupRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("[AuthController] login 요청 email={}", request.getEmail());
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("[AuthController] refresh 요청");
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("[AuthController] logout 요청");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        log.info("[AuthController] me 요청 user={}", user != null ? user.getId() : "null");
        if (user == null) {
            log.warn("[AuthController] me 인증 없음");
            return ResponseEntity.status(401).build();
        }
        UserResponse response = userService.findById(user.getId());
        return ResponseEntity.ok(response);
    }
}
