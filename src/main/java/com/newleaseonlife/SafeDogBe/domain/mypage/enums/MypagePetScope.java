package com.newleaseonlife.SafeDogBe.domain.mypage.enums;

public enum MypagePetScope {
  OWNER,
  SHARED;

  public static MypagePetScope from(String value) {
    if (value == null || value.isBlank()) {
      return OWNER;
    }
    return MypagePetScope.valueOf(value.trim().toUpperCase());
  }
}

