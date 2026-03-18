package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 3우러 18일 수정
 *  체중 기록 등록/수정 요청 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightRecordRequest {

  @NotNull(message = "반려동물 ID는 필수입니다.")
  private Long petId;

  @NotNull(message = "기록 날짜는 필수입니다.")
  private LocalDate recordDate;

  @NotNull(message = "체중은 필수입니다.")
  @DecimalMin(value = "0.1", message = "체중은 0.1kg 이상이어야 합니다.")
  private BigDecimal weight;
}