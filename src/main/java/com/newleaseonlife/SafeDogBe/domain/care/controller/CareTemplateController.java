package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.CareTemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/care-templates")
public class CareTemplateController {

  private final CareTemplateService careTemplateService;

  @PostMapping
  public ResponseEntity<CareTemplateResponse> createTemplate(@Valid @RequestBody CareTemplateCreateRequest request) {
    CareTemplateResponse response = careTemplateService.createTemplate(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{templateId}")
  public ResponseEntity<Void> deactivateTemplate(@PathVariable Long templateId) {
    careTemplateService.deactivateTemplate(templateId);
    return ResponseEntity.noContent().build();
  }
}