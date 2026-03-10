package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CareType {
  MEAL("식사"),
  WALK("산책"),
  MEDICINE("약 복용"),
  BATH("목욕"),
  HOSPITAL("병원 방문"),
  ETC("기타");

  private final String description;
}