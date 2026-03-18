package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 수정_3월18일
 * 케어 시간대. 식사·영양제·급수·산책·의약복용 등에 공통 사용.
 * CUSTOM 선택 시 CareTemplate.customTimeSlot 필드에 직접 입력값 저장.
 */
@Getter
@RequiredArgsConstructor
public enum TimeSlot {
  MORNING("아침"),
  LUNCH("점심"),
  EVENING("저녁"),
  CUSTOM("직접입력");

  private final String description;
}