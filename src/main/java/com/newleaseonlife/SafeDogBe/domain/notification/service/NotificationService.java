package com.newleaseonlife.SafeDogBe.domain.notification.service;

import com.newleaseonlife.SafeDogBe.domain.notification.dto.response.NotificationBadgeResponse;
import com.newleaseonlife.SafeDogBe.domain.notification.dto.response.NotificationResponse;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.Notification;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.notification.repository.NotificationRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final FCMService fcmService;

  /**
   * 특정 사용자에게 알림을 전송합니다.
   * 알림을 저장하고 FCM으로 푸시 알림을 전송합니다.
   *
   * @param userId 사용자 ID
   * @param type 알림 타입
   * @param title 알림 제목
   * @param body 알림 내용
   * @param relatedId 관련 ID
   * @param relatedType 관련 타입
   */
  public void sendNotification(Long userId, NotificationType type, String title,
      String body, String relatedId, String relatedType) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    // 1. 알림 데이터 저장
    Notification notification = Notification.builder()
        .user(user)
        .type(type)
        .title(title)
        .body(body)
        .relatedId(relatedId)
        .relatedType(relatedType)
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();

    notificationRepository.save(notification);

    // 2. FCM 푸시 알림 전송
    fcmService.sendPushNotification(user, type, title, body, relatedId);

    // FCM을 통해 푸시 알림 전송
    log.info("Notification saved and push sent for user: {}, type: {}", userId, type);
  }

  /**
   * 2주 이내의 사용자 알림을 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 알림 목록
   */
  @Transactional(readOnly = true)
  public List<NotificationResponse> getRecentNotifications(Long userId) {
    LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

    return notificationRepository.findRecentNotifications(userId, twoWeeksAgo)
        .stream()
        .map(NotificationResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * 특정 알림을 읽음 처리합니다.
   *
   * @param notificationId 알림 ID
   */
  public void markAsRead(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new BusinessException(
            com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode.BAD_REQUEST));

    notification.markAsRead();
    notificationRepository.save(notification);
  }

  /**
   * 모든 알림을 읽음 처리합니다.
   *
   * @param userId 사용자 ID
   */
  public void markAllAsRead(Long userId) {
    notificationRepository.markAllAsRead(userId);
  }

  /**
   * 사용자의 읽지 않은 알림 개수를 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 배지 응답
   */
  @Transactional(readOnly = true)
  public NotificationBadgeResponse getBadgeCount(Long userId) {
    long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
    return NotificationBadgeResponse.from(unreadCount);
  }

  /**
   * 특정 사용자의 2주 이상 된 알림을 삭제합니다.
   * (내부적으로 호출, 직접 API로는 노출하지 않음)
   *
   * @param userId 사용자 ID
   */
  public void deleteOldNotifications(Long userId) {
    LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
    notificationRepository.deleteByUserIdAndCreatedAtBefore(userId, twoWeeksAgo);
    log.info("Old notifications deleted for user: {}", userId);
  }

  /**
   * 마케팅 수신 OFF 시, 해당 타입의 기존 알림을 모두 삭제합니다.
   */
  @Transactional
  public void deleteMarketingNotifications(Long userId) {
    notificationRepository.deleteByUser_IdAndType(userId, NotificationType.MARKETING);
    log.info("[NotificationService] marketing notifications deleted userId={}", userId);
  }

  /**
   * 모든 사용자의 2주 이상 된 알림을 삭제합니다.
   * 매일 자정에 자동 실행됩니다.
   */
  @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
  public void deleteAllOldNotifications() {
    LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
    notificationRepository.deleteByCreatedAtBefore(twoWeeksAgo);
    log.info("Old notifications deleted for all users");
  }
}