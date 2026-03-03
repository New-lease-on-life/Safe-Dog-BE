package com.newleaseonlife.SafeDogBe.domain.user.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[UserController] me 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.findById(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("[UserController] updateMe 요청 userId={}", principal.getUser().getId());
        UserResponse response = userService.updateProfile(principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

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
