package com.newleaseonlife.SafeDogBe.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

  private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
  private final UserRepository userRepository;

  /**
   * FCM을 통해 푸시 알림을 전송합니다.
   *
   * @param user 수신자
   * @param notificationType 알림 타입
   * @param title 제목
   * @param body 내용
   * @param relatedId 관련 ID
   */
  @Async
  public void sendPushNotification(User user, NotificationType notificationType,
      String title, String body, String relatedId) {
    FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
    if (firebaseMessaging == null) {
      log.warn("FirebaseMessaging bean is not configured. Skip push send. userId={}", user.getId());
      return;
    }


    // 1. 부하테스트용 더미 토큰 처리
    if (user.getFcmToken() != null && user.getFcmToken().startsWith("dummy-fcm-token")) {
      // 실제 전송 시간을 시뮬레이션하고 싶다면 아주 짧은 sleep을 줄 수도 있습니다.
      // Thread.sleep(10); // 필요 시 10ms 정도 대기
      log.debug("[부하테스트] 더미 토큰 감지 - 성공 처리: userId={}", user.getId());
      return;
    }

    // FCM 토큰이 없으면 전송 불가
    if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
      log.warn("FCM token not found for user: {}", user.getId());
      return;
    }

    try {
      // 알림 생성
      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build();

      // 데이터 페이로드 생성 (앱에서 처리할 데이터)
      Map<String, String> data = new HashMap<>();
      data.put("notificationType", notificationType.name());
      data.put("relatedId", relatedId);
      data.put("title", title);
      data.put("body", body);
      data.put("timestamp", String.valueOf(System.currentTimeMillis()));

      // 메시지 생성
      Message message = Message.builder()
          .setNotification(notification)
          .putAllData(data)
          .setToken(user.getFcmToken())
          .build();

      // 메시지 전송
      String response = firebaseMessaging.send(message);
      log.info("Push notification sent successfully. Message ID: {}", response);

    } catch (FirebaseMessagingException e) {
      log.error("FCM Error Code: {}, Message: {}", e.getMessagingErrorCode(), e.getMessage());

      // 토큰이 더 이상 유효하지 않은 경우 (기기에서 앱 삭제 등)
      if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
          e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
        clearFcmToken(user);
      }
    }
  }

  /**
   * 여러 사용자에게 토픽 기반 푸시 알림을 전송합니다.
   * (예: 특정 반려동물의 모든 보호자들에게)
   *
   * @param topic 토픽명
   * @param title 제목
   * @param body 내용
   */
  public void sendTopicNotification(String topic, String title, String body) {
    FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
    if (firebaseMessaging == null) {
      log.warn("FirebaseMessaging bean is not configured. Skip topic send. topic={}", topic);
      return;
    }
    try {
      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
          .build();

      Message message = Message.builder()
          .setNotification(notification)
          .setTopic(topic)
          .build();

      String response = firebaseMessaging.send(message);
      log.info("Topic notification sent successfully. Topic: {}, Message ID: {}", topic, response);

    } catch (Exception e) {
      log.error("Failed to send topic notification for topic: {}", topic, e);
    }
  }

  /**
   * 사용자의 FCM 토큰을 업데이트합니다.
   *
   * @param user 사용자
   * @param fcmToken 새로운 FCM 토큰
   */
  @Transactional
  public void updateFcmToken(User user, String fcmToken) {
    user.updateFcmToken(fcmToken);
    userRepository.save(user);
    log.info("FCM token updated for user: {}", user.getId());
  }

  /**
   * 사용자의 FCM 토큰을 삭제합니다.
   * (토큰이 유효하지 않을 때)
   *
   * @param user 사용자
   */
  private void clearFcmToken(User user) {
    user.updateFcmToken(null);
    userRepository.save(user);
    log.info("FCM token cleared for user: {}", user.getId());
  }

  /**
   * 특정 토픽을 구독합니다.
   * (클라이언트에서는 SDK로 호출, 서버에서 확인용)
   *
   * @param tokens FCM 토큰 리스트
   * @param topic 토픽명
   */
  public void subscribeToTopic(java.util.List<String> tokens, String topic) {
    FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
    if (firebaseMessaging == null) {
      log.warn("FirebaseMessaging bean is not configured. Skip topic subscribe. topic={}", topic);
      return;
    }
    try {
      firebaseMessaging.subscribeToTopic(tokens, topic);
      log.info("Tokens subscribed to topic: {}", topic);
    } catch (Exception e) {
      log.error("Failed to subscribe tokens to topic: {}", topic, e);
    }
  }
}