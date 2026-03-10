package com.newleaseonlife.SafeDogBe.domain.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.InviteCodeResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.InviteInfoResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.service.InviteCodeService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공동 보호자 초대 코드 API.
 * 초대 코드 생성 (OWNER 전용), 초대 정보 조회 (비인증), 코드로 보호자 참여 (인증 필요).
 */
@Tag(name = "Invite", description = "공동 보호자 초대 코드 생성·조회·참여 API")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class InviteController {

    private final InviteCodeService inviteCodeService;

    @Operation(summary = "초대 코드 생성", description = "반려동물 OWNER만 가능. 7일 유효 코드 발급")
    @PostMapping("/api/pets/{petId}/invite")
    public ResponseEntity<InviteCodeResponse> generateInviteCode(
            @PathVariable Long petId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[InviteController] generateInviteCode petId={}, userId={}", petId, principal.getUser().getId());
        InviteCodeResponse response = inviteCodeService.generateInviteCode(petId, principal.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "초대 링크 정보 조회", description = "비인증 공개 API. 코드 기반 반려동물·초대자 정보 반환. FE 초대 화면 구성용")
    @GetMapping("/api/invites/{code}")
    public ResponseEntity<InviteInfoResponse> getInviteInfo(@PathVariable String code) {
        log.info("[InviteController] getInviteInfo code={}", code);
        InviteInfoResponse response = inviteCodeService.getInviteInfo(code);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "초대 코드로 공동 보호자 참여", description = "인증 필요. 코드 유효성 검증 후 CAREGIVER로 등록. 코드 사용 완료 처리")
    @PostMapping("/api/invites/{code}/join")
    public ResponseEntity<PetGuardianResponse> joinByInviteCode(
            @PathVariable String code,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[InviteController] joinByInviteCode code={}, userId={}", code, principal.getUser().getId());
        PetGuardianResponse response = inviteCodeService.joinByInviteCode(code, principal.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
