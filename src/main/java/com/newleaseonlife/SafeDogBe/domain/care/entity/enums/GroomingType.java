package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_3월18일
 * 미용 케어 종류. CareTemplateItem.groomingType 컬럼에 저장. */
@Getter
@RequiredArgsConstructor
public enum GroomingType {
  GROOMING("미용"),
  NAIL("발톱"),
  BRUSHING("양치"),
  EAR_CLEANING("귀청소"),
  BATH("목욕"),
  ANAL_GLAND("항문낭"),
  CUSTOM("직접입력");

  private final String description;
}