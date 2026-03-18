package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_ 3월 18일
 * 사료 종류. CareTemplateItem.foodType 컬럼에 저장. */
@Getter
@RequiredArgsConstructor
public enum FoodType {
  DRY("건식"),
  WET("습식");

  private final String description;
}