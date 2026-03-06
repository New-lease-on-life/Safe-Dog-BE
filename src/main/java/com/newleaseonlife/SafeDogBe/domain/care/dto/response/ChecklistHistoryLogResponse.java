package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ChecklistActionType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ChecklistActionType;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistHistoryLogResponse {

  private Long logId;

  // 어떤 체크리스트에 대한 로그인지
  private Long dailyChecklistId;

  // 행위 종류 (CHECK, UNCHECK 등)
  private ChecklistActionType actionType;
  private String actionTypeDescription;

  // 행위를 한 사람 (공동 보호자 중 누구인지 식별)
  private Long userId;
  private String userNickname;
  private String userProfileImageUrl;

  // 언제 했는지 (앱에서 "오전 8시 5분 완료" 등으로 표시)
  private LocalDateTime createdAt;
}