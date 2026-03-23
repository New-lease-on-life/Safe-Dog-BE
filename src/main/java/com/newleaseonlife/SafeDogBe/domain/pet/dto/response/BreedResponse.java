package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BreedResponse {

  @Schema(description = "품종 식별 코드 (DB 저장용)", example = "MALTESE")
  private String code;

  @Schema(description = "화면 노출용 품종 이름", example = "말티즈")
  private String description;
}