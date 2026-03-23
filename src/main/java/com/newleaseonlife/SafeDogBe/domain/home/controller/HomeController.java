package com.newleaseonlife.SafeDogBe.domain.home.controller;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService;
import com.newleaseonlife.SafeDogBe.domain.home.dto.request.SelectPetRequest;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomePetListResponse;
import com.newleaseonlife.SafeDogBe.domain.home.dto.response.HomeResponse;
import com.newleaseonlife.SafeDogBe.domain.home.service.HomeService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home 도메인 (개발완)", description = "홈 화면 API (반려동물 전환 및 통합 데이터 조회)")
public class HomeController {

    private final HomeService homeService;
    private final DailyChecklistService dailyChecklistService;

    @Operation(summary = "반려동물 선택 저장", description = "사용자가 선택한 반려동물 ID를 서버에 저장합니다. 이후 홈 화면 조회 시 해당 반려동물이 기본값으로 사용됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "저장 성공 (응답 바디 없음)"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "HOME_PET_ACCESS_DENIED", value = "{\"code\": 403, \"message\": \"해당 반려동물의 정보를 조회할 권한이 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "회원 정보 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "HOME_USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"회원 정보를 찾을 수 없습니다.\"}")
        }))
    })
    @PatchMapping("/selected-pet")
    public ResponseEntity<Void> selectPet(
        @AuthenticationPrincipal CustomPrincipal principal,
        @Valid @RequestBody SelectPetRequest request) {
        homeService.selectPet(principal.getUser().getId(), request.petId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "반려동물 목록 조회", description = "직접 등록한 반려동물과 공유받은 반려동물 목록을 반환합니다. 마지막 선택 반려동물 ID도 함께 포함됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "데이터 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "HOME_USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"회원 정보를 찾을 수 없습니다.\"}")
        }))
    })
    @GetMapping("/pets")
    public ResponseEntity<HomePetListResponse> getPetList(
        @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(homeService.getPetList(principal.getUser().getId()));
    }

    @Operation(summary = "홈 화면 데이터 통합 조회", description = "선택된 반려동물의 프로필, 오늘 케어 진행률, 메모 목록, 체크리스트 존재 여부를 한 번에 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "홈 데이터 조회 성공"),
        @ApiResponse(responseCode = "404", description = "데이터 미존재", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "HOME_PET_NOT_FOUND", value = "{\"code\": 404, \"message\": \"등록된 반려동물이 존재하지 않습니다.\"}"),
            @ExampleObject(name = "HOME_SPECIFIC_PET_NOT_FOUND", value = "{\"code\": 404, \"message\": \"요청하신 반려동물 정보를 찾을 수 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "500", description = "서버 계산 오류", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "HOME_CALCULATION_ERROR", value = "{\"code\": 500, \"message\": \"홈 데이터 처리 중 오류가 발생했습니다.\"}")
        }))
    })
    @GetMapping
    public ResponseEntity<HomeResponse> getHome(
        @AuthenticationPrincipal CustomPrincipal principal,
        @RequestParam(required = false) Long petId) {
        return ResponseEntity.ok(homeService.getHomeData(principal.getUser().getId(), petId));
    }

    @Operation(summary = "체크리스트 조회 (탭/카테고리 구분)", description = "기본 케어 / 질병 케어 탭으로 구분하여 항목이 있는 카테고리만 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "체크리스트 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "필수값 누락", value = "{\"code\": 400, \"message\": \"유효하지 않은 요청이거나 필수 파라미터가 누락되었습니다.\"}")
        })),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "반려동물 접근 거부", value = "{\"code\": 403, \"message\": \"해당 반려동물의 정보를 조회할 권한이 없습니다.\"}")
        }))
    })
    @GetMapping("/checklists")
    public ResponseEntity<HomeChecklistResponse> getChecklists(
        @AuthenticationPrincipal CustomPrincipal principal,
        @RequestParam Long petId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(homeService.getChecklists(principal.getUser().getId(), petId, targetDate));
    }

    @Operation(summary = "체크리스트 수행 완료 처리", description = "체크박스 '예' 클릭 시 호출. 당일 항목만 완료 처리 가능합니다. [정책: 과거 날짜는 읽기 전용]")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수행 완료 처리 성공"),
        @ApiResponse(responseCode = "400", description = "수행 조건 미충족 (비즈니스 로직 에러)", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "CHECKLIST_ALREADY_COMPLETED", description = "이미 체크가 완료된 상태에서 다시 완료를 요청한 경우", value = "{\"code\": 400, \"message\": \"이미 완료 처리된 항목입니다.\"}"),
            @ExampleObject(name = "CHECKLIST_DATE_NOT_TODAY", description = "과거 혹은 미래 날짜의 체크리스트를 수정하려 시도한 경우", value = "{\"code\": 400, \"message\": \"오늘 날짜의 체크리스트만 수정할 수 있습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "항목 미존재", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "CHECKLIST_NOT_FOUND", description = "DB에 해당 checklistId가 존재하지 않는 경우", value = "{\"status\": 404, \"code\": 404, \"message\": \"존재하지 않는 체크리스트입니다.\"}")
        }))
    })
    @PostMapping("/checklists/{checklistId}/complete")
    public ResponseEntity<DailyChecklistResponse> completeChecklist(
        @AuthenticationPrincipal CustomPrincipal principal,
        @PathVariable Long checklistId) {
        return ResponseEntity.ok(
            dailyChecklistService.completeChecklist(checklistId, principal.getUser().getId()));
    }

    @Operation(summary = "체크리스트 수행 취소", description = "체크박스 '아니요' 클릭 시 호출. 당일 항목만 취소 처리 가능합니다. [정책: 과거 날짜는 읽기 전용]")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수행 취소 성공"),
        @ApiResponse(responseCode = "400", description = "취소 조건 미충족 (비즈니스 로직 에러)", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "CHECKLIST_NOT_COMPLETED", description = "체크가 되어있지 않은 상태에서 취소(언체크)를 요청한 경우", value = "{\"code\": 400, \"message\": \"아직 완료되지 않은 항목입니다.\"}"),
            @ExampleObject(name = "CHECKLIST_DATE_NOT_TODAY", description = "과거 혹은 미래 날짜의 체크리스트를 수정하려 시도한 경우", value = "{\"code\": 400, \"message\": \"오늘 날짜의 체크리스트만 수정할 수 있습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "항목 미존재", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "CHECKLIST_NOT_FOUND", description = "DB에 해당 checklistId가 존재하지 않는 경우", value = "{\"status\": 404, \"code\": 404, \"message\": \"존재하지 않는 체크리스트입니다.\"}")
        }))
    })
    @PostMapping("/checklists/{checklistId}/uncomplete")
    public ResponseEntity<DailyChecklistResponse> uncompleteChecklist(
        @AuthenticationPrincipal CustomPrincipal principal,
        @PathVariable Long checklistId) {
        return ResponseEntity.ok(
            dailyChecklistService.uncompleteChecklist(checklistId, principal.getUser().getId()));
    }
}