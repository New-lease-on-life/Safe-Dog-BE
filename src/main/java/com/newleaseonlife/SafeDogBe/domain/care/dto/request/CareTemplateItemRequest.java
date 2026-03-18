package com.newleaseonlife.SafeDogBe.domain.care.dto.request;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.FoodType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.GroomingType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.PreventionType;
import lombok.*;

import java.math.BigDecimal;

/** 3월 18일 수정
 * 케어 템플릿 세부 항목 요청 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateItemRequest {

  /** 항목명 (사료명/영양제명/복약명 등) */
  private String itemName;

  /** 사료 종류 (MEAL 타입) */
  private FoodType foodType;

  /** 미용 종류 (GROOMING 타입) */
  private GroomingType groomingType;

  /** 미용 직접 입력 */
  private String customGroomingType;

  /** 예방 종류 (PREVENTION 타입) */
  private PreventionType preventionType;

  /** 예방 직접 입력 */
  private String customPreventionType;

  private BigDecimal amount;
  private String amountUnit;
  private String imageUrl;
  private String note;
  private int sortOrder;
}