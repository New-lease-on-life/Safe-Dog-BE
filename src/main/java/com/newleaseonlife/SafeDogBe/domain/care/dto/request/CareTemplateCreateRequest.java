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
public class CareTemplateCreateRequest {

  @NotNull(message = "반려동물 ID는 필수입니다.")
  private Long petId;

  @NotNull(message = "케어 타입은 필수입니다.")
  private CareType careType;

  @NotBlank(message = "제목은 필수입니다.")
  private String title;

  private String content; // 선택 사항이므로 validation 생략

  @NotNull(message = "반복 주기는 필수입니다.")
  private RepeatCycle repeatCycle;
}