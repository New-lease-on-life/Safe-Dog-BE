// domain/care/dto/request/CareTemplateCreateRequest.java
package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycleUnit;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.TimeSlot;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * 케어 템플릿 등록 요청.
 *
 * ✅ 변경: RepeatCycle → repeatCycleValue + repeatCycleUnit + repeatStartDate
 * ✅ 추가: timeSlot, customTimeSlot
 * ✅ 추가: items (세부 항목 목록)
 * ✅ 추가: urineTrackingOn, fecesTrackingOn (배변노트)
 * ✅ 추가: weightRequestOn (체중노트)
 * ✅ 추가: memo
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateCreateRequest {

  @NotNull(message = "반려동물 ID는 필수입니다.")
  private Long petId;

  @NotNull(message = "케어 타입은 필수입니다.")
  private CareType careType;

  /** 시간대 (식사/영양제/산책/급수/의약복용) */
  private TimeSlot timeSlot;

  /** CUSTOM 선택 시 직접 입력값 */
  private String customTimeSlot;

  /**
   * 반복 주기 숫자. null이면 주기 미설정(매일 생성).
   * 예) 1 + DAY → 매일, 7 + DAY → 7일마다
   */
  private Integer repeatCycleValue;

  private RepeatCycleUnit repeatCycleUnit;

  private LocalDate repeatStartDate;

  /** 배변 노트 소변 기록 on/off */
  private boolean urineTrackingOn = false;

  /** 배변 노트 대변 기록 on/off */
  private boolean fecesTrackingOn = false;

  /** 체중 노트 주기 요청 on/off */
  private boolean weightRequestOn = false;

  /** 식사/간식/영양제/의약복용/예방접종/미용 세부 항목 */
  @Valid
  private List<CareTemplateItemRequest> items;

  // [기획 반영] 제목 미입력 방어. 프론트에서 미발송 시 기본값 처리 필요
  @NotBlank(message = "노트 제목은 필수입니다.")
  private String title = "기타"; // 기획서 명시: 기본값 '기타'

  // [기획 반영] 반려노트 자체의 메모 500자 제한 (DB varchar(500) 초과 방지)
  @Size(max = 500, message = "메모는 최대 500자까지 입력 가능합니다.")
  private String memo;
}