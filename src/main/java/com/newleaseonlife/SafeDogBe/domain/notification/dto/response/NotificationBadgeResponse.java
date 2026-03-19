package com.newleaseonlife.SafeDogBe.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알림 배지 응답 - 헤더에 표시할 미확인 알림 개수")
public class NotificationBadgeResponse {

  @Schema(description = "읽지 않은 알림 개수", example = "3")
  private Long unreadCount;

  @Schema(description = "알림이 있는지 여부", example = "true")
  private Boolean hasUnread;

  /**
   * 미확인 알림 개수로부터 배지 응답을 생성합니다.
   *
   * @param unreadCount 읽지 않은 알림 개수
   * @return 배지 응답
   */
  public static NotificationBadgeResponse from(Long unreadCount) {
    return NotificationBadgeResponse.builder()
        .unreadCount(unreadCount)
        .hasUnread(unreadCount > 0)
        .build();
  }
}