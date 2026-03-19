package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ExcretionType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** 3월 18일 수정
 * 배변 기록 등록/수정 요청.
 *
 * [소변 정상] isNormal=true → urineCount 필수
 * [소변 이상] isNormal=false → urineCount + urineColor + isUrineAccident 필수
 * [대변 정상] isNormal=true → fecesCount 필수
 * [대변 이상] isNormal=false → fecesCount + fecesCondition 필수
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcretionRecordRequest {

  @NotNull(message = "체크리스트 ID는 필수입니다.")
  private Long dailyChecklistId;

  @NotNull(message = "배변 종류는 필수입니다.")
  private ExcretionType excretionType;

  @NotNull(message = "정상/이상 여부는 필수입니다.")
  private Boolean isNormal;

  /** 소변 횟수. "1~2", "3~4", "5+" */
  private String urineCount;

  /** 소변 색상 (이상 시). "맑음", "진함", "혈뇨" */
  private String urineColor;

  /** 소변 실수 여부 (이상 시) */
  private Boolean isUrineAccident;

  /** 대변 횟수. "1~2", "3~4", "5+" */
  private String fecesCount;

  /** 대변 상태 (이상 시). "정상", "묽음", "설사", "혈변" */
  private String fecesCondition;
}