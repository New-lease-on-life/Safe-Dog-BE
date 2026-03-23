package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DogBreed {
  MALTESE("말티즈"),
  POODLE("푸들"),
  POMERANIAN("포메라니안"),
  CHIHUAHUA("치와와"),
  BICHON_FRISE("비숑 프리제"),
  GOLDEN_RETRIEVER("골든 리트리버"),
  WELSH_CORGI("웰시 코기"),
  SIBERIAN_HUSKY("시베리안 허스키"),
  SHIBA_INU("시바견"),
  MIXED("믹스견"),
  ETC("기타"); // 사용자가 직접 입력하거나 목록에 없는 경우

  private final String description;
}