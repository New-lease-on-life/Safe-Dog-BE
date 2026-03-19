package com.newleaseonlife.SafeDogBe.domain.notification.controller;

import com.newleaseonlife.SafeDogBe.domain.notification.dto.response.NotificationBadgeResponse;
import com.newleaseonlife.SafeDogBe.domain.notification.dto.response.NotificationResponse;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.notification.service.FCMService;
import com.newleaseonlife.SafeDogBe.domain.notification.service.NotificationService;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "푸시 알림 내역 조회 및 상태 관리, FCM 토큰 등록 API")
public class NotificationController {

  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final FCMService fcmService;

  @GetMapping
  @Operation(
      summary = "알림 목록 조회",
      description = "현재 사용자의 최근 2주 이내 알림 내역을 최신순으로 조회합니다. 읽음 여부(isRead) 및 알림 타입 정보를 포함합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 조회 성공",
          content = @Content(schema = @Schema(implementation = NotificationResponse.class)))
  })
  public ResponseEntity<Map<String, Object>> getNotifications(
      @AuthenticationPrincipal CustomPrincipal principal) {

    List<NotificationResponse> notifications =
        notificationService.getRecentNotifications(principal.getUser().getId());

    Map<String, Object> response = new HashMap<>();
    response.put("notifications", notifications);
    response.put("totalCount", notifications.size());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/badge")
  @Operation(
      summary = "미확인 알림 배지 개수 조회",
      description = "사용자가 아직 읽지 않은 알림의 총 개수를 반환합니다. 앱 헤더의 종 모양 아이콘이나 앱 아이콘 배지 숫자에 사용됩니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "배지 개수 조회 성공",
          content = @Content(schema = @Schema(implementation = NotificationBadgeResponse.class)))
  })
  public ResponseEntity<NotificationBadgeResponse> getBadgeCount(
      @AuthenticationPrincipal CustomPrincipal principal) {

    NotificationBadgeResponse badge =
        notificationService.getBadgeCount(principal.getUser().getId());

    return ResponseEntity.ok(badge);
  }

  @PostMapping("/fcm-token")
  @Operation(
      summary = "FCM 디바이스 토큰 등록 및 갱신",
      description = "푸시 알림 수신을 위해 클라이언트에서 발급받은 FCM 토큰을 서버에 저장합니다. 앱 최초 실행, 로그인, 토큰 갱신 시점에 호출해야 합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "토큰 업데이트 성공"),
      @ApiResponse(responseCode = "400", description = "요청 바디에 fcmToken 값이 누락되거나 비어있음")
  })
  public ResponseEntity<Map<String, String>> updateFcmToken(
      @Parameter(description = "FCM 토큰 정보 (키: fcmToken)", required = true)
      @RequestBody Map<String, String> request,
      @AuthenticationPrincipal CustomPrincipal principal) {

    String fcmToken = request.get("fcmToken");

    if (fcmToken == null || fcmToken.trim().isEmpty()) {
      Map<String, String> response = new HashMap<>();
      response.put("message", "FCM 토큰이 유효하지 않습니다.");
      return ResponseEntity.badRequest().body(response);
    }

    User user = userRepository.findById(principal.getUser().getId())
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    fcmService.updateFcmToken(user, fcmToken);

    Map<String, String> response = new HashMap<>();
    response.put("message", "FCM 토큰이 업데이트되었습니다.");
    response.put("fcmToken", fcmToken);

    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{notificationId}/read")
  @Operation(
      summary = "단일 알림 읽음 처리",
      description = "특정 알림의 상태를 '읽음(isRead=true)'으로 변경합니다. 사용자가 알림 목록에서 특정 알림을 클릭했을 때 호출합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID")
  })
  public ResponseEntity<Map<String, String>> markAsRead(
      @Parameter(description = "읽음 처리할 알림 ID", example = "10")
      @PathVariable Long notificationId,
      @AuthenticationPrincipal CustomPrincipal principal) {

    notificationService.markAsRead(notificationId);

    Map<String, String> response = new HashMap<>();
    response.put("message", "알림을 읽음으로 처리했습니다.");

    return ResponseEntity.ok(response);
  }

  @PatchMapping("/read-all")
  @Operation(
      summary = "모든 알림 일괄 읽음 처리",
      description = "현재 사용자의 '읽지 않은 모든 알림'을 한 번에 '읽음' 상태로 변경합니다. (예: '모두 읽음' 버튼 클릭 시)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "일괄 읽음 처리 성공")
  })
  public ResponseEntity<Map<String, String>> markAllAsRead(
      @AuthenticationPrincipal CustomPrincipal principal) {

    notificationService.markAllAsRead(principal.getUser().getId());

    Map<String, String> response = new HashMap<>();
    response.put("message", "모든 알림을 읽음으로 처리했습니다.");

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{notificationId}")
  @Operation(
      summary = "단일 알림 삭제",
      description = "특정 알림 내역을 사용자의 알림함에서 삭제합니다. (참고: 2주가 지난 알림은 서버에서 스케줄러에 의해 자동 삭제됩니다.)"
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 알림 ID")
  })
  public ResponseEntity<Map<String, String>> deleteNotification(
      @Parameter(description = "삭제할 알림 ID", example = "10")
      @PathVariable Long notificationId,
      @AuthenticationPrincipal CustomPrincipal principal) {

    Map<String, String> response = new HashMap<>();
    response.put("message", "알림을 삭제했습니다.");

    return ResponseEntity.ok(response);
  }

  @PostMapping("/test-send")
  @Operation(
      summary = "FCM 푸시 알림 발송 테스트 (개발/QA용)",
      description = "현재 로그인한 사용자 본인의 기기로 테스트 푸시 알림(케어 요청 타입)을 발송합니다. 비동기로 처리되며, 사전에 FCM 토큰이 등록되어 있어야 실제 기기로 알림이 수신됩니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "푸시 발송 요청 성공")
  })
  public ResponseEntity<Map<String, String>> testSendPush(
      @AuthenticationPrincipal CustomPrincipal principal) {

    NotificationType testType = NotificationType.CARE_REQUEST;

    notificationService.sendNotification(
        principal.getUser().getId(),
        testType,
        testType.getLabel(),
        testType.getDefaultMessage(),
        "test-care-id-999",
        "CARE"
    );

    Map<String, String> response = new HashMap<>();
    response.put("message", "FCM 푸시 전송이 백그라운드(Async)로 요청되었습니다.");
    response.put("type", testType.name());

    return ResponseEntity.ok(response);
  }
}