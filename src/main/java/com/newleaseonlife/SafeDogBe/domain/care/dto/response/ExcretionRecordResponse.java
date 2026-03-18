package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ExcretionType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 3월 18일 수정
 * 배변 기록 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcretionRecordResponse {

  private Long id;
  private Long dailyChecklistId;
  private Long petId;
  private LocalDate recordDate;
  private ExcretionType excretionType;
  private String excretionTypeDescription;
  private boolean isNormal;
  private String urineCount;
  private String urineColor;
  private Boolean isUrineAccident;
  private String fecesCount;
  private String fecesCondition;
  private Long recordedByUserId;
  private String recordedByNickname;
  private LocalDateTime updatedAt;
}