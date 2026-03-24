package com.newleaseonlife.SafeDogBe.domain.petnote.controller;


import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.ChecklistMemoRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.ChecklistMemoResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.service.ChecklistMemoService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklists/{checklistId}/memos")
@RequiredArgsConstructor
@Tag(name = "Checklist Memo", description = "체크리스트 소통 메모 API (단방향 설계)")
public class ChecklistMemoController {

  private final ChecklistMemoService memoService;

  @Operation(summary = "메모 목록 조회", description = "해당 체크리스트에 달린 메모 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<List<ChecklistMemoResponse>> getMemos(@PathVariable Long checklistId) {
    return ResponseEntity.ok(memoService.getMemos(checklistId));
  }

  @Operation(summary = "메모 생성", description = "체크리스트에 새로운 메모를 추가합니다. (100자 제한)")
  @PostMapping
  public ResponseEntity<ChecklistMemoResponse> createMemo(
      @PathVariable Long checklistId,
      @AuthenticationPrincipal CustomPrincipal principal,
      @Valid @RequestBody ChecklistMemoRequest request) {
    Long userId = principal.getUser().getId();
    return ResponseEntity.ok(memoService.createMemo(checklistId, userId, request));
  }

  @Operation(summary = "메모 수정", description = "메모를 수정합니다. (작성자 본인만 가능)")
  @PatchMapping("/{memoId}")
  public ResponseEntity<ChecklistMemoResponse> updateMemo(
      @PathVariable Long checklistId, // URL 규격용 (Service에선 memoId로 바로 찾음)
      @PathVariable Long memoId,
      @AuthenticationPrincipal CustomPrincipal principal,
      @Valid @RequestBody ChecklistMemoRequest request) {
    Long userId = principal.getUser().getId();
    return ResponseEntity.ok(memoService.updateMemo(memoId, userId, request));
  }

  @Operation(summary = "메모 삭제", description = "메모를 삭제합니다. (작성자 본인 또는 반려동물 OWNER만 가능)")
  @DeleteMapping("/{memoId}")
  public ResponseEntity<Void> deleteMemo(
      @PathVariable Long checklistId,
      @PathVariable Long memoId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    Long userId = principal.getUser().getId();
    memoService.deleteMemo(memoId, userId);
    return ResponseEntity.noContent().build();
  }
}