package com.newleaseonlife.SafeDogBe.domain.notification.repository;

import com.newleaseonlife.SafeDogBe.domain.notification.entity.Notification;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /**
   * 특정 사용자의 2주 이내 알림을 최신순으로 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 2주 이내의 알림 목록
   */
  @Query("SELECT n FROM Notification n " +
      "WHERE n.user.id = :userId " +
      "AND n.createdAt >= :twoWeeksAgo " +
      "ORDER BY n.createdAt DESC")
  List<Notification> findRecentNotifications(@Param("userId") Long userId,
      @Param("twoWeeksAgo") LocalDateTime twoWeeksAgo);

  /**
   * 특정 사용자의 읽지 않은 알림 개수를 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 읽지 않은 알림 개수
   */
  long countByUserIdAndIsReadFalse(Long userId);

  /**
   * 특정 사용자의 2주 이상 된 알림을 삭제합니다.
   *
   * @param userId 사용자 ID
   * @param twoWeeksAgo 2주 전 시간
   */
  void deleteByUserIdAndCreatedAtBefore(Long userId, LocalDateTime twoWeeksAgo);

  /**
   * 모든 2주 이상 된 알림을 삭제합니다.
   * (스케줄러에서 사용)
   *
   * @param twoWeeksAgo 2주 전 시간
   */
  void deleteByCreatedAtBefore(LocalDateTime twoWeeksAgo);

  /**
   * 특정 사용자의 알림을 모두 읽음 처리합니다.
   *
   * @param userId 사용자 ID
   */
  @Query("UPDATE Notification n SET n.isRead = true " +
      "WHERE n.user.id = :userId")
  void markAllAsRead(@Param("userId") Long userId);

  /** 특정 사용자의 특정 알림 타입을 모두 삭제 (예: 마케팅 수신 OFF 시) */
  void deleteByUser_IdAndType(Long userId, NotificationType type);
}