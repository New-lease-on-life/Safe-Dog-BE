package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_3월18일
 * 반복 주기 단위. CareTemplate.repeatCycleUnit 컬럼에 저장.
 * CareTemplate.repeatCycleValue(숫자)와 조합하여 "N일/N월/N년마다" 표현.
 *
 * ✅ 신규: 기존 RepeatCycle(NONE/DAILY/WEEKLY/MONTHLY) 대체
 * 예) repeatCycleValue=1, repeatCycleUnit=DAY → 매일
 *     repeatCycleValue=7, repeatCycleUnit=DAY → 7일마다
 *     repeatCycleValue=1, repeatCycleUnit=MONTH → 매월
 */
@Getter
@RequiredArgsConstructor
public enum RepeatCycleUnit {
  DAY("일"),
  WEEK("주"),
  MONTH("월"),
  YEAR("년");

  private final String description;
}