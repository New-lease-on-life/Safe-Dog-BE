package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "care_template",
    indexes = {
        // 반려동물별 템플릿 목록 조회가 매우 빈번하므로 인덱스 필수
        @Index(name = "idx_care_template_pet_id", columnList = "pet_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CareTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 반려동물 삭제 시 템플릿도 연쇄 삭제(Cascade)되도록 DB 레벨 제약조건 적용
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_care_template_pet"))
  private Pet pet;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private CareType careType;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private RepeatCycle repeatCycle;

  // 논리적 삭제 및 스케줄러 생성 여부를 판단하는 핵심 플래그
  @Column(nullable = false)
  private boolean isActive = true;

  // SQL 스키마에 updated_at이 없으므로 생성 시간만 자동 추적
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public CareTemplate(Pet pet, CareType careType, String title, String content, RepeatCycle repeatCycle) {
    this.pet = pet;
    this.careType = careType;
    this.title = title;
    this.content = content;
    this.repeatCycle = repeatCycle;
    this.isActive = true;
  }

  // 비즈니스 로직 1: 템플릿 내용 수정 (제목, 내용, 반복 주기)
  public void updateTemplate(CareType careType, String title, String content, RepeatCycle repeatCycle) {
    if (careType != null) this.careType = careType;
    if (title != null) this.title = title;
    if (content != null) this.content = content;
    if (repeatCycle != null) this.repeatCycle = repeatCycle;
  }

  // 비즈니스 로직 2: 템플릿 비활성화 (Soft Delete 방식)
  // 과거의 완료된 체크리스트 기록을 유지하기 위해 물리적 삭제(DELETE) 대신 비활성화 처리
  public void deactivate() {
    this.isActive = false;
  }
}