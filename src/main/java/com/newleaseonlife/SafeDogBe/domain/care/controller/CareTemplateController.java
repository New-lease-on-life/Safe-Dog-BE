package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.CareTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 3월 18일 수정
 * ✅ 추가: GET /api/care-templates?petId={petId} — 반려동물별 템플릿 목록 조회
 * ✅ 추가: PUT /api/care-templates/{templateId} — 템플릿 수정
 * ✅ 변경: RepeatCycle → 새 주기 방식 반영
 */
@Tag(name = "CareTemplate", description = "반려노트 케어 템플릿(설정) API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/care-templates")
public class CareTemplateController {

  private final CareTemplateService careTemplateService;

  @Operation(summary = "반려동물 케어 템플릿 목록 조회")
  @GetMapping
  public ResponseEntity<List<CareTemplateResponse>> getTemplates(@RequestParam Long petId) {
    return ResponseEntity.ok(careTemplateService.getTemplatesByPet(petId));
  }

  @Operation(summary = "케어 템플릿 등록")
  @PostMapping
  public ResponseEntity<CareTemplateResponse> createTemplate(
      @Valid @RequestBody CareTemplateCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(careTemplateService.createTemplate(request));
  }

  @Operation(summary = "케어 템플릿 수정")
  @PutMapping("/{templateId}")
  public ResponseEntity<CareTemplateResponse> updateTemplate(
      @PathVariable Long templateId,
      @Valid @RequestBody CareTemplateUpdateRequest request) {
    return ResponseEntity.ok(careTemplateService.updateTemplate(templateId, request));
  }

  @Operation(summary = "케어 템플릿 비활성화 (Soft Delete)")
  @DeleteMapping("/{templateId}")
  public ResponseEntity<Void> deactivateTemplate(@PathVariable Long templateId) {
    careTemplateService.deactivateTemplate(templateId);
    return ResponseEntity.noContent().build();
  }
}