package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycleUnit;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.TimeSlot;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * 케어 템플릿 수정 요청.
 * ✅ 변경: RepeatCycle → repeatCycleValue + repeatCycleUnit + repeatStartDate
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateUpdateRequest {

  private CareType careType;
  private String title;
  private TimeSlot timeSlot;
  private String customTimeSlot;
  private Integer repeatCycleValue;
  private RepeatCycleUnit repeatCycleUnit;
  private LocalDate repeatStartDate;
  private boolean urineTrackingOn;
  private boolean fecesTrackingOn;
  private boolean weightRequestOn;
  private String memo;
  private List<CareTemplateItemRequest> items;
}