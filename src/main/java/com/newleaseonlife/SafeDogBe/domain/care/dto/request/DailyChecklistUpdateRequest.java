package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChecklistUpdateRequest {

  @NotBlank(message = "제목은 필수입니다.")
  private String title;

  private String content;
}