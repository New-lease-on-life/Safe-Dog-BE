package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_3월18일
 * 케어 타입 (기획서 기준).
 *
 * ✅ 변경: SNACK(간식), SUPPLEMENT(영양제), WATER(급수), EXCRETION(배변),
 *          WEIGHT(체중), GROOMING(미용), MEDICATION(의약복용), PREVENTION(예방/접종),
 *          DISEASE_CARE(질병케어) 추가
 * ✅ 변경: MEDICINE → MEDICATION (명칭 통일)
 * ✅ 제거: BATH (GROOMING의 세부항목으로 이동), HOSPITAL (기획서에 없음)
 */
@Getter
@RequiredArgsConstructor
public enum CareType {
  MEAL("식사"),
  SNACK("간식"),
  SUPPLEMENT("영양제"),
  WATER("급수"),
  EXCRETION("배변"),
  WALK("산책"),
  WEIGHT("체중"),
  GROOMING("미용"),
  MEDICATION("의약복용"),
  PREVENTION("예방/접종"),
  DISEASE_CARE("질병케어"),
  ETC("기타");

  private final String description;
}