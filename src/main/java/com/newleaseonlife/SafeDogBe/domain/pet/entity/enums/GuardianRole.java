package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuardianRole {
  OWNER("메인 보호자"),
  CAREGIVER("공동 보호자");

  private final String description;
}