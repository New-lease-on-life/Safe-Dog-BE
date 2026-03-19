package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ExcretionType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 3월 18일 일일 배변 상세 기록.
 * <p>
 * ✅ 신규: DailyChecklist.isCompleted만으로는 배변 상태 저장 불가
 * <p>
 * [소변 정상] → urineCount 필수 [소변 이상] → urineCount + urineColor + isUrineAccident 필수 [대변 정상] →
 * fecesCount 필수 [대변 이상] → fecesCount + fecesCondition 필수
 */
@Entity
@Table(
    name = "daily_excretion_record",
    indexes = {
        @Index(name = "idx_excretion_checklist", columnList = "daily_checklist_id"),
        @Index(name = "idx_excretion_pet_date", columnList = "pet_id, record_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailyExcretionRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_checklist_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_excretion_checklist"))
  private DailyChecklist dailyChecklist;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_excretion_pet"))
  private com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet pet;

  @Column(nullable = false)
  private LocalDate recordDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private ExcretionType excretionType;

  /**
   * 정상/이상 여부
   */
  @Column(nullable = false)
  private boolean isNormal;

  // ─── 소변 관련 ────────────────────────────────────────────────
  /**
   * 소변 횟수 선택. "1~2", "3~4", "5+"
   */
  @Column(length = 10)
  private String urineCount;

  /**
   * 소변 색상 (이상 시). "맑음", "진함", "혈뇨"
   */
  @Column(length = 20)
  private String urineColor;

  /**
   * 소변 실수 여부 (이상 시)
   */
  private Boolean isUrineAccident;

  // ─── 대변 관련 ────────────────────────────────────────────────
  /**
   * 대변 횟수 선택. "1~2", "3~4", "5+"
   */
  @Column(length = 10)
  private String fecesCount;

  /**
   * 대변 상태 (이상 시). "정상", "묽음", "설사", "혈변"
   */
  @Column(length = 20)
  private String fecesCondition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by",
      foreignKey = @ForeignKey(name = "fk_excretion_user"))
  private User recordedBy;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public DailyExcretionRecord(DailyChecklist dailyChecklist,
      com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet pet,
      LocalDate recordDate, ExcretionType excretionType,
      boolean isNormal,
      String urineCount, String urineColor, Boolean isUrineAccident,
      String fecesCount, String fecesCondition,
      User recordedBy) {
    this.dailyChecklist = dailyChecklist;
    this.pet = pet;
    this.recordDate = recordDate;
    this.excretionType = excretionType;
    this.isNormal = isNormal;
    this.urineCount = urineCount;
    this.urineColor = urineColor;
    this.isUrineAccident = isUrineAccident;
    this.fecesCount = fecesCount;
    this.fecesCondition = fecesCondition;
    this.recordedBy = recordedBy;
  }

  public void update(Boolean isNormal,
      String urineCount, String urineColor, Boolean isUrineAccident,
      String fecesCount, String fecesCondition,
      User recordedBy) {
    if (isNormal != null) {
      this.isNormal = isNormal;
    }

    this.urineCount = urineCount;
    this.urineColor = urineColor;
    this.isUrineAccident = isUrineAccident;
    this.fecesCount = fecesCount;
    this.fecesCondition = fecesCondition;
    this.recordedBy = recordedBy;
  }
}