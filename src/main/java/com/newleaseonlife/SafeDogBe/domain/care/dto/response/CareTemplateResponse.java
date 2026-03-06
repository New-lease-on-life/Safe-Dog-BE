package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateResponse {

  private Long id;
  private Long petId;

  // Enum의 원본 값(서버용)과 프론트엔드 노출용 한글 값을 동시에 내려줍니다.
  private CareType careType;
  private String careTypeDescription;

  private String title;
  private String content;

  private RepeatCycle repeatCycle;
  private String repeatCycleDescription;

  private boolean isActive;
}