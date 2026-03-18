package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycleUnit;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.TimeSlot;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * 케어 템플릿 응답 DTO.
 * ✅ 변경: repeatCycle → repeatCycleValue + repeatCycleUnit + repeatStartDate
 * ✅ 추가: timeSlot, customTimeSlot, items, urineTrackingOn, fecesTrackingOn, memo
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateResponse {

  private Long id;
  private Long petId;
  private CareType careType;
  private String careTypeDescription;
  private String title;
  private TimeSlot timeSlot;
  private String timeSlotDescription;
  private String customTimeSlot;
  private Integer repeatCycleValue;
  private RepeatCycleUnit repeatCycleUnit;
  private String repeatCycleUnitDescription;
  private LocalDate repeatStartDate;
  private boolean urineTrackingOn;
  private boolean fecesTrackingOn;
  private boolean weightRequestOn;
  private String memo;
  private boolean isActive;
  private List<CareTemplateItemResponse> items;
}