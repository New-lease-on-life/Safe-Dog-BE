package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 3월 18일 수정
 * 체중 기록 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightRecordResponse {

  private Long id;
  private Long petId;
  private LocalDate recordDate;
  private BigDecimal weight;
  private Long recordedByUserId;
  private String recordedByNickname;
  private LocalDateTime updatedAt;
}