// domain/care/controller/DailyChecklistController.java
package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * ✅ 변경: userId @RequestParam → @AuthenticationPrincipal (보안 적용)
 * ✅ 추가: GET /api/checklists?petId={}&date={} — 날짜별 체크리스트 조회
 */
@Tag(name = "DailyChecklist", description = "일일 체크리스트 조회·완료·취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checklists")
public class DailyChecklistController {

  private final DailyChecklistService dailyChecklistService;

  @Operation(summary = "날짜별 반려동물 체크리스트 조회")
  @GetMapping
  public ResponseEntity<List<DailyChecklistResponse>> getChecklists(
      @RequestParam Long petId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(dailyChecklistService.getChecklistsByDate(petId, date));
  }

  @Operation(summary = "체크리스트 완료 처리")
  @PostMapping("/{checklistId}/complete")
  public ResponseEntity<DailyChecklistResponse> completeChecklist(
      @PathVariable Long checklistId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    return ResponseEntity.ok(
        dailyChecklistService.completeChecklist(checklistId, principal.getUser().getId()));
  }

  @Operation(summary = "체크리스트 완료 취소")
  @PostMapping("/{checklistId}/uncomplete")
  public ResponseEntity<DailyChecklistResponse> uncompleteChecklist(
      @PathVariable Long checklistId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    return ResponseEntity.ok(
        dailyChecklistService.uncompleteChecklist(checklistId, principal.getUser().getId()));
  }
}