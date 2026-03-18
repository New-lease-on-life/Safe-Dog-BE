package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.FoodType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.GroomingType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.PreventionType;
import lombok.*;

import java.math.BigDecimal;

/** 3월 18일 수정
 * 케어 템플릿 세부 항목 응답 DTO */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTemplateItemResponse {

  private Long id;
  private String itemName;
  private FoodType foodType;
  private String foodTypeDescription;
  private GroomingType groomingType;
  private String customGroomingType;
  private PreventionType preventionType;
  private String customPreventionType;
  private BigDecimal amount;
  private String amountUnit;
  private String imageUrl;
  private String note;
  private int sortOrder;
}