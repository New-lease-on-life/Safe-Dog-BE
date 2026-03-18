package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.FoodType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.GroomingType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.PreventionType;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 수정 3월 18일
 * 케어 템플릿 세부 항목.
 * 식사/간식/영양제/의약복용/예방접종/미용 등 CareType별 상세 정보.
 *
 * ✅ 신규: 기존 CareTemplate.content(TEXT 1개)로는 복수 항목 표현 불가 → 별도 테이블로 분리
 */
@Entity
@Table(
    name = "care_template_item",
    indexes = {
        @Index(name = "idx_care_item_template_id", columnList = "care_template_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareTemplateItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "care_template_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_care_item_template"))
  private CareTemplate careTemplate;

  /** 항목명 (사료명/간식명/영양제명/복약명/예방약명) */
  @Column(length = 200)
  private String itemName;

  // ─── 식사 전용 ─────────────────────────────────────────────────
  /** 사료 종류 (건식/습식). MEAL 타입에서 사용 */
  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private FoodType foodType;

  // ─── 미용 전용 ─────────────────────────────────────────────────
  /** 미용 종류. GROOMING 타입에서 사용 */
  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private GroomingType groomingType;

  /** 미용 직접입력 시 텍스트 */
  @Column(length = 100)
  private String customGroomingType;

  // ─── 예방/접종 전용 ────────────────────────────────────────────
  /** 예방 종류. PREVENTION 타입에서 사용 */
  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private PreventionType preventionType;

  /** 예방 직접입력 시 텍스트 */
  @Column(length = 100)
  private String customPreventionType;

  // ─── 공통 ─────────────────────────────────────────────────────
  /** 제공량 (숫자) */
  @Column(precision = 8, scale = 2)
  private BigDecimal amount;

  /** 제공량 단위 (g, 컵, 밥그릇, 개, 포, 방울, 봉지 등 직접입력 포함) */
  @Column(length = 50)
  private String amountUnit;

  /** 이미지 URL (S3) */
  @Column(columnDefinition = "TEXT")
  private String imageUrl;

  /** 특이사항 */
  @Column(columnDefinition = "TEXT")
  private String note;

  /** 정렬 순서 */
  @Column(nullable = false)
  private int sortOrder = 0;

  @Builder
  public CareTemplateItem(CareTemplate careTemplate, String itemName,
      FoodType foodType,
      GroomingType groomingType, String customGroomingType,
      PreventionType preventionType, String customPreventionType,
      BigDecimal amount, String amountUnit,
      String imageUrl, String note, int sortOrder) {
    this.careTemplate = careTemplate;
    this.itemName = itemName;
    this.foodType = foodType;
    this.groomingType = groomingType;
    this.customGroomingType = customGroomingType;
    this.preventionType = preventionType;
    this.customPreventionType = customPreventionType;
    this.amount = amount;
    this.amountUnit = amountUnit;
    this.imageUrl = imageUrl;
    this.note = note;
    this.sortOrder = sortOrder;
  }

  public void update(String itemName, BigDecimal amount, String amountUnit,
      String imageUrl, String note) {
    if (itemName != null) this.itemName = itemName;
    if (amount != null) this.amount = amount;
    if (amountUnit != null) this.amountUnit = amountUnit;
    if (imageUrl != null) this.imageUrl = imageUrl;
    if (note != null) this.note = note;
  }
}