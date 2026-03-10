package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "daily_checklist",
    indexes = {
        // 백엔드 핵심: 앱의 메인 화면인 "특정 날짜의 반려동물 할 일 목록" 조회 최적화용 복합 인덱스
        @Index(name = "idx_daily_checklist_pet_date", columnList = "pet_id, target_date"),
        // 특정 사용자가 완료한 목록을 찾을 때 풀스캔 방지
        @Index(name = "idx_daily_checklist_completed_by", columnList = "completed_by")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DailyChecklist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 반려동물 삭제 시 할 일 목록도 같이 삭제(CASCADE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_daily_checklist_pet"))
  private Pet pet;

  // 원본 템플릿이 삭제되더라도 기록은 남아야 하므로 제약조건을 SET NULL로 설계
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "care_template_id", foreignKey = @ForeignKey(name = "fk_daily_checklist_template"))
  private CareTemplate careTemplate;

  @Column(nullable = false)
  private LocalDate targetDate;

  // --- 스냅샷(Snapshot) 데이터: 원본 템플릿에서 복사해옴 ---
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CareType careType;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;
  // ---------------------------------------------------

  @Column(nullable = false)
  private boolean isCompleted = false;

  // 누가 이 항목을 완료 처리했는지 추적 (마찬가지로 유저 탈퇴 시 SET NULL 처리)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "completed_by", foreignKey = @ForeignKey(name = "fk_daily_checklist_completed"))
  private User completedBy;

  // 백엔드 핵심: 동시성 제어를 위한 낙관적 락(Optimistic Lock)
  @Version
  @Column(nullable = false)
  private Integer version;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  private LocalDateTime updatedAt;

  @Builder
  public DailyChecklist(Pet pet, CareTemplate careTemplate, LocalDate targetDate,
      CareType careType, String title, String content) {
    this.pet = pet;
    this.careTemplate = careTemplate;
    this.targetDate = targetDate;
    this.careType = careType;
    this.title = title;
    this.content = content;
    this.isCompleted = false;
  }

  // 비즈니스 로직 1: 체크리스트 완료 처리 (어떤 유저가 완료했는지 파라미터로 받음)
  public void complete(User user) {
    this.isCompleted = true;
    this.completedBy = user;
  }

  // 비즈니스 로직 2: 체크리스트 완료 취소
  public void uncomplete() {
    this.isCompleted = false;
    this.completedBy = null;
  }

  // 비즈니스 로직 3: 스냅샷 내용 수정 (오늘만 특별히 내용을 바꿀 때)
  public void updateContent(String newTitle, String newContent) {
    if (newTitle != null) this.title = newTitle;
    if (newContent != null) this.content = newContent;
  }
}