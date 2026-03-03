package com.newleaseonlife.SafeDogBe.domain.term.controller;

import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementListRequest;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.TermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.UserTermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.service.TermService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @GetMapping
    public ResponseEntity<List<TermResponse>> getTerms() {
        log.info("[TermController] 약관 목록 조회");
        return ResponseEntity.ok(termService.getAllTerms());
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserTermResponse>> getMyTerms(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[TermController] 내 약관 동의 현황 조회 userId={}", principal.getUser().getId());
        return ResponseEntity.ok(termService.getUserTerms(principal.getUser().getId()));
    }

    @PostMapping("/agree")
    public ResponseEntity<List<UserTermResponse>> agreeTerms(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody TermAgreementListRequest request) {
        log.info("[TermController] 약관 동의 요청 userId={}", principal.getUser().getId());
        List<UserTermResponse> response = termService.agreeTerms(principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }
}
