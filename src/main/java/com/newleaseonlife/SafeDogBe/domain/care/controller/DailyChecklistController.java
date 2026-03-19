package com.newleaseonlife.SafeDogBe.domain.care.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.DailyChecklistUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "DailyChecklist", description = "일일 체크리스트 조회, 완료, 취소 및 수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checklists")
public class DailyChecklistController {

  private final DailyChecklistService dailyChecklistService;

  @Operation(
      summary = "날짜별 반려동물 체크리스트 조회",
      description = "특정 반려동물의 특정 날짜(과거/오늘/미래)에 할당된 모든 체크리스트 목록을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공")
  })
  @GetMapping
  public ResponseEntity<List<DailyChecklistResponse>> getChecklists(
      @Parameter(description = "반려동물 식별자(ID)", example = "1")
      @RequestParam Long petId,
      @Parameter(description = "조회할 대상 날짜 (yyyy-MM-dd 형식)", example = "2026-03-19")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(dailyChecklistService.getChecklistsByDate(petId, date));
  }

  @Operation(
      summary = "체크리스트 완료 처리",
      description = "특정 체크리스트를 완료(체크) 상태로 변경합니다. (당일 체크리스트만 처리 가능)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "완료 처리 성공"),
      @ApiResponse(responseCode = "400", description = "이미 완료된 체크리스트이거나 당일 날짜가 아님"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 체크리스트 ID")
  })
  @PostMapping("/{checklistId}/complete")
  public ResponseEntity<DailyChecklistResponse> completeChecklist(
      @Parameter(description = "완료 처리할 체크리스트 ID", example = "10")
      @PathVariable Long checklistId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    return ResponseEntity.ok(
        dailyChecklistService.completeChecklist(checklistId, principal.getUser().getId()));
  }

  @Operation(
      summary = "체크리스트 완료 취소",
      description = "완료된 특정 체크리스트를 미완료(체크 해제) 상태로 되돌립니다. (당일 체크리스트만 처리 가능)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "완료 취소 성공"),
      @ApiResponse(responseCode = "400", description = "완료되지 않은 체크리스트이거나 당일 날짜가 아님"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 체크리스트 ID")
  })
  @PostMapping("/{checklistId}/uncomplete")
  public ResponseEntity<DailyChecklistResponse> uncompleteChecklist(
      @Parameter(description = "완료 취소할 체크리스트 ID", example = "10")
      @PathVariable Long checklistId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    return ResponseEntity.ok(
        dailyChecklistService.uncompleteChecklist(checklistId, principal.getUser().getId()));
  }

  @Operation(
      summary = "체크리스트 수정",
      description = "당일 생성된 체크리스트의 내용을 수정합니다. 동시 다발적인 수정 요청 시 낙관적 락(Optimistic Lock)을 통해 데이터 정합성을 보호합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "당일 날짜가 아니어서 수정 불가"),
      @ApiResponse(responseCode = "403", description = "해당 체크리스트에 접근할 권한이 없음"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 체크리스트 ID"),
      @ApiResponse(responseCode = "409", description = "동시 수정 충돌 감지됨. 바디에 담긴 최신 데이터로 화면을 갱신해야 함")
  })
  @PatchMapping("/{checklistId}")
  public ResponseEntity<DailyChecklistResponse> updateChecklist(
      @Parameter(description = "수정할 체크리스트 ID", example = "10")
      @PathVariable Long checklistId,
      @RequestBody DailyChecklistUpdateRequest request,
      @AuthenticationPrincipal CustomPrincipal principal) {

    try {
      // 1. 접근 권한 확인
      validateAccessToChecklist(checklistId, principal.getUser().getId());

      // 2. 수정 수행
      DailyChecklistResponse response = dailyChecklistService.updateChecklist(checklistId, request);

      return ResponseEntity.ok(response);

    } catch (OptimisticLockingFailureException e) {
      // 2-1. 동시 저장 감지 시 최신 데이터 반환
      DailyChecklistResponse latestData = dailyChecklistService.getLatestChecklist(checklistId);

      // 에러 응답에 최신 데이터 포함
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(latestData);
    }
  }

  private void validateAccessToChecklist(Long checklistId, Long userId) {
    boolean hasAccess = dailyChecklistService.hasAccessToChecklist(checklistId, userId);

    if (!hasAccess) {
      throw new BusinessException(CommonErrorCode.NO_PERMISSION);
    }
  }
}