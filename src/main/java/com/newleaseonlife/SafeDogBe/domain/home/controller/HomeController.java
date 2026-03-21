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
@Tag(name = "Home", description = "홈 화면 API")
public class HomeController {

    private final HomeService homeService;
    private final DailyChecklistService dailyChecklistService;

    @Operation(summary = "반려동물 선택 저장",
            description = "사용자가 선택한 반려동물 ID를 서버에 저장합니다. 이후 홈 화면 조회 시 해당 반려동물이 기본값으로 사용됩니다.")
    @PatchMapping("/selected-pet")
    public ResponseEntity<Void> selectPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody SelectPetRequest request) {
        homeService.selectPet(principal.getUser().getId(), request.petId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "반려동물 목록 조회",
            description = "직접 등록한 반려동물과 공유받은 반려동물 목록을 반환합니다. 마지막 선택 반려동물 ID도 함께 포함됩니다.")
    @GetMapping("/pets")
    public ResponseEntity<HomePetListResponse> getPetList(
            @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(homeService.getPetList(principal.getUser().getId()));
    }

    @Operation(summary = "홈 화면 데이터 조회",
            description = "선택된 반려동물의 프로필, 오늘 케어 진행률, 메모 목록, 체크리스트 존재 여부를 한 번에 반환합니다.")
    @GetMapping
    public ResponseEntity<HomeResponse> getHome(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestParam(required = false) Long petId) {
        return ResponseEntity.ok(homeService.getHomeData(principal.getUser().getId(), petId));
    }

    @Operation(summary = "체크리스트 조회 (탭/카테고리 구분)",
            description = "기본 케어 / 질병 케어 탭으로 구분하여 항목이 있는 카테고리만 반환합니다.")
    @GetMapping("/checklists")
    public ResponseEntity<HomeChecklistResponse> getChecklists(
            @AuthenticationPrincipal CustomPrincipal principal,
            @RequestParam Long petId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(homeService.getChecklists(principal.getUser().getId(), petId, targetDate));
    }

    @Operation(summary = "체크리스트 수행 완료 처리",
            description = "체크박스 '예' 클릭 시 호출. 수행 시각, 수행자 정보를 저장하고 업데이트된 항목 상태를 반환합니다.")
    @PostMapping("/checklists/{checklistId}/complete")
    public ResponseEntity<DailyChecklistResponse> completeChecklist(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long checklistId) {
        return ResponseEntity.ok(
                dailyChecklistService.completeChecklist(checklistId, principal.getUser().getId()));
    }

    @Operation(summary = "체크리스트 수행 취소",
            description = "체크박스 '아니요' 클릭 시 호출. 완료 상태를 되돌립니다.")
    @PostMapping("/checklists/{checklistId}/uncomplete")
    public ResponseEntity<DailyChecklistResponse> uncompleteChecklist(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long checklistId) {
        return ResponseEntity.ok(
                dailyChecklistService.uncompleteChecklist(checklistId, principal.getUser().getId()));
    }
}
