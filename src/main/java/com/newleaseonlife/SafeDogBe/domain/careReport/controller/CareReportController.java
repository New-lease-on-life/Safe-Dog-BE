package com.newleaseonlife.SafeDogBe.domain.careReport.controller;

import com.newleaseonlife.SafeDogBe.domain.careReport.dto.response.CareReportResponse;
import com.newleaseonlife.SafeDogBe.domain.careReport.service.CareReportService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets/{petId}/care-report")
@RequiredArgsConstructor
@Tag(name = "Care Report 도메인 (개발완)", description = "케어리포트 통계 API")
public class CareReportController {

  private final CareReportService careReportService;

  @Operation(summary = "주간 리포트 조회", description = "최근 7일간의 노트 단위 완료 통계를 반환합니다. (선행조건: 체크리스트 최소 7개 등록)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공. (데이터가 7개 미만일 경우 isReportAvailable=false 반환)"),
      @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사람의 반려동물 조회 시도)", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "PET_ACCESS_DENIED", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
      })),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "UNAUTHORIZED", value = "{\"code\": 401, \"message\": \"로그인이 필요합니다.\"}")
      }))
  })
  @GetMapping("/weekly")
  public ResponseEntity<CareReportResponse> getWeeklyReport(
      @AuthenticationPrincipal CustomPrincipal principal,
      @PathVariable Long petId) {
    Long userId = principal.getUser().getId();
    // ✅ Service 계층의 보안 검증을 위해 userId 파라미터 추가 주입
    return ResponseEntity.ok(careReportService.getCareReport(userId, petId, 7));
  }

  @Operation(summary = "월간 리포트 조회", description = "최근 30일간의 노트 단위 완료 통계를 반환합니다. (선행조건: 체크리스트 최소 7개 등록)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공. (데이터가 7개 미만일 경우 isReportAvailable=false 반환)"),
      @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사람의 반려동물 조회 시도)", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "PET_ACCESS_DENIED", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
      })),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 요청", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "UNAUTHORIZED", value = "{\"code\": 401, \"message\": \"로그인이 필요합니다.\"}")
      }))
  })
  @GetMapping("/monthly")
  public ResponseEntity<CareReportResponse> getMonthlyReport(
      @AuthenticationPrincipal CustomPrincipal principal,
      @PathVariable Long petId) {
    Long userId = principal.getUser().getId();
    // ✅ Service 계층의 보안 검증을 위해 userId 파라미터 추가 주입
    return ResponseEntity.ok(careReportService.getCareReport(userId, petId, 30));
  }
}