package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.WeightRecordRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.WeightRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.WeightRecordService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * 체중 기록 API.
 * 기획서 3: 보호자가 숫자(kg)를 직접 입력해서 저장.
 */
@Tag(name = "WeightRecord", description = "체중 기록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weight-records")
public class WeightRecordController {

  private final WeightRecordService weightRecordService;

  @Operation(summary = "체중 기록 등록/수정")
  @PostMapping
  public ResponseEntity<WeightRecordResponse> saveWeightRecord(
      @AuthenticationPrincipal CustomPrincipal principal,
      @Valid @RequestBody WeightRecordRequest request) {
    return ResponseEntity.ok(
        weightRecordService.saveWeightRecord(principal.getUser().getId(), request));
  }

  @Operation(summary = "기간 내 체중 이력 조회")
  @GetMapping
  public ResponseEntity<List<WeightRecordResponse>> getWeightHistory(
      @RequestParam Long petId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(weightRecordService.getWeightHistory(petId, from, to));
  }
}