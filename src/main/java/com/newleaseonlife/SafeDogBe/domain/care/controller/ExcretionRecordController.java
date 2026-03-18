package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.ExcretionRecordRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.ExcretionRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.ExcretionRecordService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 3월 18일 수정
 * 배변 기록 API.
 * 기획서 3: 소변/대변 각각 정상/이상 선택 + 세부 항목 기록.
 */
@Tag(name = "ExcretionRecord", description = "배변 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/excretion-records")
public class ExcretionRecordController {

  private final ExcretionRecordService excretionRecordService;

  @Operation(summary = "배변 기록 등록/수정 (당일만 가능)")
  @PostMapping
  public ResponseEntity<ExcretionRecordResponse> saveExcretionRecord(
      @AuthenticationPrincipal CustomPrincipal principal,
      @Valid @RequestBody ExcretionRecordRequest request) {
    return ResponseEntity.ok(
        excretionRecordService.saveExcretionRecord(principal.getUser().getId(), request));
  }

  @Operation(summary = "체크리스트 배변 기록 조회")
  @GetMapping
  public ResponseEntity<List<ExcretionRecordResponse>> getByChecklist(
      @RequestParam Long checklistId) {
    return ResponseEntity.ok(excretionRecordService.getByChecklist(checklistId));
  }
}