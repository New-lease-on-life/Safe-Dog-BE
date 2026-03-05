package com.newleaseonlife.SafeDogBe.domain.user.controller;

import com.newleaseonlife.SafeDogBe.domain.user.dto.request.RestoreRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.request.UserUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원(User) API. 인증된 사용자 기준으로 내 프로필 조회·수정·온보딩 완료·탈퇴, 닉네임 중복 검사 제공.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 현재 로그인 사용자 프로필 조회 */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] me 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.findById(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 내 프로필 수정 (이름, 닉네임, 프로필 이미지). 닉네임 변경 시 중복 검사 후 반영 */
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("[UserController] updateMe 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.updateProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    /** 온보딩 완료 처리. 최초 설정 완료 후 한 번 호출하여 isOnboardingCompleted = true 로 설정 */
    @PatchMapping("/me/onboarding-complete")
    public ResponseEntity<UserResponse> completeOnboarding(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] onboarding-complete 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.completeOnboarding(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 회원 탈퇴(Soft Delete). withdrawnAt 기록, 30일 내 복구 가능·기록 1년 보관. 로그인 불가 처리는 인증 계층에서 수행 */
    @PostMapping("/me/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] withdraw 요청 userId={}", principal.getUser().getId());
        userService.withdraw(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    /** 탈퇴 복구(비인증). 이메일+비밀번호로 본인 확인, 탈퇴 후 30일 이내만 가능. 소셜 전용 계정은 로그인 화면에서 해당 소셜로 재로그인 시 복구 처리 */
    @PostMapping("/restore")
    public ResponseEntity<UserResponse> restore(@Valid @RequestBody RestoreRequest request) {
        log.info("[UserController] restore 요청 email={}", request.email());
        UserResponse response = userService.restore(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    /** 닉네임 중복 여부 검사. 가입·프로필 수정 전 클라이언트에서 호출. 사용 가능 시 204 */
    @GetMapping("/check-nickname")
    public ResponseEntity<Void> checkNickname(
            @RequestParam
            @NotBlank(message = "닉네임은 필수입니다")
            @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글, 영문, 숫자, 마침표, 밑줄, 하이픈만 사용 가능합니다")
            String nickname) {
        log.info("[UserController] check-nickname 요청 nickname={}", nickname);
        userService.checkNicknameDuplicate(nickname);
        return ResponseEntity.noContent().build();
    }
}
