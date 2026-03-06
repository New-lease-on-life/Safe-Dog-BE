package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyChecklistResponse {

  private Long id;
  private Long petId;

  // 원본 템플릿 정보 (수동으로 생성한 체크리스트면 null일 수 있음)
  private Long careTemplateId;

  private LocalDate targetDate;

  private CareType careType;
  private String careTypeDescription;

  private String title;
  private String content;

  private boolean isCompleted;

  // 누가 완료했는지 (아직 완료 안 됐으면 null)
  private Long completedByUserId;
  private String completedByNickname;

  // 클라이언트에서 완료 API 호출 시 함께 넘겨야 할 낙관적 락 버전
  private Integer version;
}