package com.newleaseonlife.SafeDogBe.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.newleaseonlife.SafeDogBe.domain.user.dto.request.RestoreRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.request.UserUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.OnboardingStatusResponse;
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

import jakarta.validation.constraints.Positive;

/**
 * 회원(User) API. 인증된 사용자 기준으로 내 프로필 조회·수정·온보딩 완료·탈퇴, 닉네임 중복 검사 제공.
 */
@Tag(name = "User", description = "회원 프로필, 온보딩, 탈퇴/복구, 닉네임 중복 검사 API")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] me 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.findById(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 수정", description = "이름·닉네임·프로필 이미지 수정. 닉네임 변경 시 중복 검사")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("[UserController] updateMe 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.updateProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "온보딩 노출 여부 조회", description = "shouldShowOnboarding=true면 FE 온보딩 화면 표시, false면 홈으로 이동")
    /**
     * 온보딩 노출 여부 조회.
     * isOnboardingCompleted == false이면 shouldShowOnboarding = true 반환 → FE 온보딩 화면 표시.
     * 재로그인 회원(이미 완료)은 false 반환 → FE 홈으로 이동.
     */
    @GetMapping("/me/onboarding-status")
    public ResponseEntity<OnboardingStatusResponse> getOnboardingStatus(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] onboarding-status 요청 userId={}", principal.getUser().getId());
        boolean shouldShow = !principal.getUser().isOnboardingCompleted();
        return ResponseEntity.ok(new OnboardingStatusResponse(shouldShow));
    }

    @Operation(summary = "온보딩 완료 처리", description = "최초 설정 완료 후 1회 호출. isOnboardingCompleted=true로 갱신")
    @PatchMapping("/me/onboarding-complete")
    public ResponseEntity<UserResponse> completeOnboarding(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] onboarding-complete 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.completeOnboarding(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "Soft Delete. 30일 내 복구 가능. OWNER는 탈퇴 방어")
    @PostMapping("/me/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] withdraw 요청 userId={}", principal.getUser().getId());
        userService.withdraw(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "회원 복구", description = "탈퇴 후 30일 이내만 가능. 이메일+비밀번호 인증 방식")
    @PostMapping("/restore")
    public ResponseEntity<UserResponse> restore(@Valid @RequestBody RestoreRequest request) {
        log.info("[UserController] restore 요청 email={}", request.email());
        UserResponse response = userService.restore(request.email(), request.password());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마지막 선택 반려동물 갱신", description = "반려동물 선택 시 호출. 다음 접속 기본값으로 저장")
    /** 마지막으로 선택한 반려동물 ID 갱신. 반려동물 선택 시 호출하여 다음 접속 기본값으로 저장.
     *  petId = 0 또는 파라미터 미전송은 허용하지 않음(양수 필수) */
    @PatchMapping("/me/last-selected-pet")
    public ResponseEntity<UserResponse> updateLastSelectedPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestParam @Positive(message = "petId는 1 이상이어야 합니다") Long petId) {
        log.info("[UserController] last-selected-pet 요청 userId={}, petId={}", principal.getUser().getId(), petId);
        UserResponse response = userService.updateLastSelectedPet(principal.getUser().getId(), petId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 중복 검사", description = "사용 가능 시 204, 중복 시 409")
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
