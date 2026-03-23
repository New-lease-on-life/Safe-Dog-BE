package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CatBreed {
  KOREAN_SHORTHAIR("코리안 숏헤어"),
  PERSIAN("페르시안"),
  RUSSIAN_BLUE("러시안 블루"),
  SIAMESE("샴"),
  MUNCHKIN("먼치킨"),
  SCOTTISH_FOLD("스코티시 폴드"),
  RAGDOLL("랙돌"),
  BRITISH_SHORTHAIR("브리티시 숏헤어"),
  SPHYNX("스핑크스"),
  MIXED("믹스묘"),
  ETC("기타"); // 사용자가 직접 입력하거나 목록에 없는 경우

  private final String description;
}