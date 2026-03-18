package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_ 3월 18일
 * 예방/접종 종류. CareTemplateItem.preventionType 컬럼에 저장. */
@Getter
@RequiredArgsConstructor
public enum PreventionType {
  HEARTWORM("심장사상충"),
  EXTERNAL_PARASITE("외부 기생충"),
  DEWORMING("종합구충"),
  RABIES("광견병 주사"),
  CUSTOM("직접입력");

  private final String description;
}