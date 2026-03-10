package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareTemplateUpdateRequest {
  // 수정 시에는 펫 ID가 변경될 일이 없으므로 제외합니다.

  @NotNull(message = "케어 타입은 필수입니다.")
  private CareType careType;

  @NotBlank(message = "제목은 필수입니다.")
  private String title;

  private String content;

  @NotNull(message = "반복 주기는 필수입니다.")
  private RepeatCycle repeatCycle;
}