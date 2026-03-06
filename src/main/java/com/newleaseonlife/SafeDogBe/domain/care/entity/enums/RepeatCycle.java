package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RepeatCycle {
  NONE("반복 없음"),
  DAILY("매일"),
  WEEKLY("매주"),
  MONTHLY("매월");

  private final String description;
}