package com.newleaseonlife.SafeDogBe.domain.mypage.controller;

import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.response.MypageMarketingConsentResponse;
import com.newleaseonlife.SafeDogBe.domain.mypage.dto.request.MypageMarketingConsentRequest;
import com.newleaseonlife.SafeDogBe.domain.notification.service.NotificationService;
import com.newleaseonlife.SafeDogBe.domain.term.service.TermService;
import com.newleaseonlife.SafeDogBe.domain.mypage.service.MypageService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "Mypage 도메인(개발완)", description = "마이페이지 API")
public class MypageController {

  private final MypageService mypageService;
  private final TermService termService;
  private final NotificationService notificationService;

  private static final String APP_VERSION = "0.0.1-SNAPSHOT";

  @Operation(summary = "마이페이지 초기 조회", description = "내 프로필 + (OWNER/SHARED) 반려동물 목록 및 보호자 목록을 반환합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 파라미터 요청", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "MYPAGE_INVALID_PET_SCOPE", value = "{\"code\": 400, \"message\": \"petScope는 OWNER 또는 SHARED만 입력할 수 있습니다.\"}")
      })),
      @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"존재하지 않는 유저입니다.\"}")
      }))
  })
  @GetMapping
  public ResponseEntity<MypageResponse> getMypage(
      @RequestParam(defaultValue = "OWNER") String petScope,
      @AuthenticationPrincipal CustomPrincipal principal) {
    Long userId = principal.getUser().getId();
    log.info("[MypageController] getMypage userId={}", userId);
    return ResponseEntity.ok(mypageService.getMypage(userId, petScope));
  }

  @Operation(summary = "마케팅 정보 수신 동의 조회", description = "마이페이지 마케팅 수신 ON/OFF 현재 상태를 반환합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"존재하지 않는 유저입니다.\"}")
      }))
  })
  @GetMapping("/marketing")
  public ResponseEntity<MypageMarketingConsentResponse> getMarketingConsent(
      @AuthenticationPrincipal CustomPrincipal principal) {
    Long userId = principal.getUser().getId();
    boolean agreed = termService.getMarketingConsent(userId);

    // agreedAt은 현재 getMarketingConsent에서 직접 반환하지 않으므로, 별도 API 호출 없이 null로 둡니다.
    // (정확한 agreedAt까지 필요하면 UserTermResponse로 확장 가능합니다.)
    return ResponseEntity.ok(MypageMarketingConsentResponse.of(agreed, null));
  }

  @Operation(summary = "마케팅 정보 수신 동의 변경", description = "ON/OFF 변경 후 OFF일 경우 기존 마케팅 알림 내역을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
      @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음", content = @Content(mediaType = "application/json", examples = {
          @ExampleObject(name = "USER_NOT_FOUND", value = "{\"code\": 404, \"message\": \"존재하지 않는 유저입니다.\"}")
      }))
  })
  @PatchMapping("/marketing")
  public ResponseEntity<MypageMarketingConsentResponse> updateMarketingConsent(
      @AuthenticationPrincipal CustomPrincipal principal,
      @RequestBody MypageMarketingConsentRequest request) {
    Long userId = principal.getUser().getId();
    boolean requestAgreed = request.agreed();
    // 상태 업데이트
    boolean currentAgreed = termService.updateMarketingConsent(userId, requestAgreed);

    // 기획: 차단(false)할 경우 알림 발송 내역에서 삭제
    if (!requestAgreed) {
      notificationService.deleteMarketingNotifications(userId);
    }

    return ResponseEntity.ok(MypageMarketingConsentResponse.of(currentAgreed, null));
  }

  @Operation(summary = "서비스 업데이트 버전 조회", description = "마이페이지 서비스 정보 영역에서 노출할 앱 버전을 반환합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "버전 조회 성공")
  })
  @GetMapping("/app-version")
  public ResponseEntity<String> getAppVersion() {
    return ResponseEntity.ok(APP_VERSION);
  }
}