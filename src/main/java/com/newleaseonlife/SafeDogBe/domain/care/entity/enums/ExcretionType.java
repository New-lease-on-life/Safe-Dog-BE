package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_3월18일
 * 배변 종류. DailyExcretionRecord.excretionType 컬럼에 저장. */
@Getter
@RequiredArgsConstructor
public enum ExcretionType {
  URINE("소변"),
  FECES("대변");

  private final String description;
}