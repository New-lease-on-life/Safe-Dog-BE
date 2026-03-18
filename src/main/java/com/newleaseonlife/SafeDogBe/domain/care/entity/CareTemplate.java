package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycleUnit;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.TimeSlot;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 수정 3월 18일 케어 템플릿. 반려노트 등록 시 설정한 케어 항목.
 * <p>
 * ✅ 제거: RepeatCycle enum → repeatCycleValue + repeatCycleUnit + repeatStartDate ✅ 추가: timeSlot
 * (아침/점심/저녁/직접입력) ✅ 추가: customTimeSlot (직접입력 시 텍스트값) ✅ 추가: memo (케어 블록 하단 메모) ✅ 추가: urinTrackingOn,
 * fecesTrackingOn (배변노트 전용) ✅ items: CareTemplateItem과 1:N 관계 (식사/영양제 등 세부 항목)
 * <p>
 * ⚠️ 단방향 원칙: - [기존 리팩토링 문서 오류] items(@OneToMany) 필드 추가됐었음 → 제거 - CareTemplateItem → CareTemplate
 * 단방향 @ManyToOne 유지 - items 조회는 CareTemplateItemRepository.findByCareTemplateId()로 처리 - items 삭제는
 * CareTemplateItemRepository.deleteByCareTemplateId()로 처리
 */
@Entity
@Table(
    name = "care_template",
    indexes = {
        @Index(name = "idx_care_template_pet_id", columnList = "pet_id"),
        @Index(name = "idx_care_template_pet_type", columnList = "pet_id, care_type")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CareTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_care_template_pet"))
  private Pet pet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CareType careType;

  @Column(nullable = false, length = 200)
  private String title;

  // ─── 시간대 ──────────────────────────────────────────────────
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private TimeSlot timeSlot;

  /**
   * CUSTOM 선택 시 직접 입력값
   */
  @Column(length = 50)
  private String customTimeSlot;

  // ─── 반복 주기 ────────────────────────────────────────────────
  @Column
  private Integer repeatCycleValue;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private RepeatCycleUnit repeatCycleUnit;

  @Column
  private LocalDate repeatStartDate;

  // ─── 배변 전용 ────────────────────────────────────────────────
  @Column(nullable = false)
  private boolean urineTrackingOn = false;

  @Column(nullable = false)
  private boolean fecesTrackingOn = false;

  // ─── 체중 전용 ────────────────────────────────────────────────
  @Column(nullable = false)
  private boolean weightRequestOn = false;

  // ─── 메모 ──────────────────────────────────────────────────
  @Column(columnDefinition = "TEXT")
  private String memo;

  @Column(nullable = false)
  private boolean isActive = true;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // ❌ 제거: @OneToMany(mappedBy = "careTemplate") List<CareTemplateItem> items
  //    이유: CareTemplateItem이 CareTemplate으로 @ManyToOne 단방향을 이미 가짐.
  //         items 목록은 CareTemplateItemRepository.findByCareTemplateIdOrderBySortOrderAsc()로 조회.
  //         items 삭제는 CareTemplateItemRepository.deleteByCareTemplateId()로 처리.

  @Builder
  public CareTemplate(Pet pet, CareType careType, String title,
      TimeSlot timeSlot, String customTimeSlot,
      Integer repeatCycleValue, RepeatCycleUnit repeatCycleUnit,
      LocalDate repeatStartDate,
      boolean urineTrackingOn, boolean fecesTrackingOn,
      boolean weightRequestOn, String memo) {
    this.pet = pet;
    this.careType = careType;
    this.title = title;
    this.timeSlot = timeSlot;
    this.customTimeSlot = customTimeSlot;
    this.repeatCycleValue = repeatCycleValue;
    this.repeatCycleUnit = repeatCycleUnit;
    this.repeatStartDate = repeatStartDate;
    this.urineTrackingOn = urineTrackingOn;
    this.fecesTrackingOn = fecesTrackingOn;
    this.weightRequestOn = weightRequestOn;
    this.memo = memo;
    this.isActive = true;
  }

  public void update(CareType careType, String title,
      TimeSlot timeSlot, String customTimeSlot,
      Integer repeatCycleValue, RepeatCycleUnit repeatCycleUnit,
      LocalDate repeatStartDate,
      boolean urineTrackingOn, boolean fecesTrackingOn,
      boolean weightRequestOn, String memo) {
    if (careType != null) {
      this.careType = careType;
    }
    if (title != null) {
      this.title = title;
    }
    this.timeSlot = timeSlot;
    this.customTimeSlot = customTimeSlot;
    this.repeatCycleValue = repeatCycleValue;
    this.repeatCycleUnit = repeatCycleUnit;
    this.repeatStartDate = repeatStartDate;
    this.urineTrackingOn = urineTrackingOn;
    this.fecesTrackingOn = fecesTrackingOn;
    this.weightRequestOn = weightRequestOn;
    if (memo != null) {
      this.memo = memo;
    }
  }

  public void deactivate() {
    this.isActive = false;
  }

  /**
   * 스케줄러에서 오늘 생성 여부 판단
   */
  public boolean shouldGenerateToday(LocalDate today) {
    if (!isActive) {
      return false;
    }
    if (repeatCycleValue == null || repeatCycleUnit == null || repeatStartDate == null) {
      return true; // 주기 미설정 → 매일
    }
    if (today.isBefore(repeatStartDate)) {
      return false;
    }
    return switch (repeatCycleUnit) {
      case DAY -> {
        long days = today.toEpochDay() - repeatStartDate.toEpochDay();
        yield days % repeatCycleValue == 0;
      }
      case WEEK -> {
        long daysBetween = today.toEpochDay() - repeatStartDate.toEpochDay();
        yield daysBetween % (repeatCycleValue * 7L) == 0;
      }
      case MONTH -> {
        int months = (today.getYear() - repeatStartDate.getYear()) * 12
            + (today.getMonthValue() - repeatStartDate.getMonthValue());
        yield months >= 0 && months % repeatCycleValue == 0
            && today.getDayOfMonth() == repeatStartDate.getDayOfMonth();
      }
      case YEAR -> {
        int years = today.getYear() - repeatStartDate.getYear();
        yield years >= 0 && years % repeatCycleValue == 0
            && today.getMonthValue() == repeatStartDate.getMonthValue()
            && today.getDayOfMonth() == repeatStartDate.getDayOfMonth();
      }
    };
  }
}