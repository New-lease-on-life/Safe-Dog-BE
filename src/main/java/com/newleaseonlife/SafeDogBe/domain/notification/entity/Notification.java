package com.newleaseonlife.SafeDogBe.domain.notification.entity;

import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String body;

  // 관련된 Pet/Care/PetNote ID
  @Column(name = "related_id")
  private String relatedId;

  // 관련된 타입 (PET_ID, CARE_ID, PETNOTE_ID)
  @Column(name = "related_type")
  private String relatedType;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  @Column(nullable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  /**
   * 알림을 읽음 처리합니다.
   */
  public void markAsRead() {
    this.isRead = true;
  }

  /**
   * 알림을 읽지 않음으로 처리합니다.
   */
  public void markAsUnread() {
    this.isRead = false;
  }

  /**
   * 알림이 2주 이상 된 것인지 확인합니다.
   *
   * @return 2주 이상 된 경우 true
   */
  public boolean isOlderThanTwoWeeks() {
    return LocalDateTime.now().isAfter(createdAt.plusWeeks(2));
  }
}