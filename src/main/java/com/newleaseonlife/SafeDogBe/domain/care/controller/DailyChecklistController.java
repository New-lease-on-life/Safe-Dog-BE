package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checklists")
public class DailyChecklistController {

  private final DailyChecklistService dailyChecklistService;

  // TODO: userId는 추후 Spring Security의 @AuthenticationPrincipal 등으로 대체 예정

  @PostMapping("/{checklistId}/complete")
  public ResponseEntity<DailyChecklistResponse> completeChecklist(
      @PathVariable Long checklistId,
      @RequestParam Long userId) {
    DailyChecklistResponse response = dailyChecklistService.completeChecklist(checklistId, userId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{checklistId}/uncomplete")
  public ResponseEntity<DailyChecklistResponse> uncompleteChecklist(
      @PathVariable Long checklistId,
      @RequestParam Long userId) {
    DailyChecklistResponse response = dailyChecklistService.uncompleteChecklist(checklistId, userId);
    return ResponseEntity.ok(response);
  }
}