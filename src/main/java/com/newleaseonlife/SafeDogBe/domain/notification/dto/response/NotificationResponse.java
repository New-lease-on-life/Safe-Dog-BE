package com.newleaseonlife.SafeDogBe.domain.notification.dto.response;

import com.newleaseonlife.SafeDogBe.domain.notification.entity.Notification;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알림 응답")
public class NotificationResponse {

  @Schema(description = "알림 ID", example = "1")
  private Long id;

  @Schema(description = "알림 타입", example = "CARE_REQUEST")
  private NotificationType type;

  @Schema(description = "알림 제목", example = "케어 요청")
  private String title;

  @Schema(description = "알림 내용", example = "점심 식사를 요청했어요")
  private String body;

  @Schema(description = "관련 ID (Pet/Care/PetNote)", example = "123")
  private String relatedId;

  @Schema(description = "관련 타입", example = "PET_ID")
  private String relatedType;

  @Schema(description = "읽음 여부", example = "false")
  private Boolean isRead;

  @Schema(description = "생성 시간")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  /**
   * Notification 엔티티에서 Response로 변환합니다.
   *
   * @param notification 알림 엔티티
   * @return 알림 응답
   */
  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .title(notification.getTitle())
        .body(notification.getBody())
        .relatedId(notification.getRelatedId())
        .relatedType(notification.getRelatedType())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}