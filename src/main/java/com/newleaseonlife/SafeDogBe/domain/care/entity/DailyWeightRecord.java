package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 수정 3월 18일
 * 일일 체중 기록.
 *
 * ✅ 신규: DailyChecklist.isCompleted만으로는 체중 수치 저장 불가
 */
@Entity
@Table(
    name = "daily_weight_record",
    indexes = {
        @Index(name = "idx_weight_pet_date", columnList = "pet_id, record_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailyWeightRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_weight_pet"))
  private com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet pet;

  @Column(nullable = false)
  private LocalDate recordDate;

  /** 체중 (kg). 소수점 1자리 */
  @Column(nullable = false, precision = 5, scale = 1)
  private BigDecimal weight;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by",
      foreignKey = @ForeignKey(name = "fk_weight_user"))
  private User recordedBy;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public DailyWeightRecord(com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet pet,
      LocalDate recordDate, BigDecimal weight, User recordedBy) {
    this.pet = pet;
    this.recordDate = recordDate;
    this.weight = weight;
    this.recordedBy = recordedBy;
  }

  public void updateWeight(BigDecimal weight, User recordedBy) {
    this.weight = weight;
    this.recordedBy = recordedBy;
  }
}