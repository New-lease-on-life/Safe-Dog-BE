package com.newleaseonlife.SafeDogBe.domain.care.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ChecklistActionType;
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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "checklist_history_log",
    indexes = {
        // 특정 체크리스트의 이력을 빠르게 조회하기 위한 인덱스
        @Index(name = "idx_checklist_log_checklist_id", columnList = "daily_checklist_id"),
        // 특정 유저의 활동 이력을 조회하기 위한 인덱스 (옵션)
        @Index(name = "idx_checklist_log_user_id", columnList = "user_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChecklistHistoryLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 부모인 데일리 체크리스트가 지워지면 로그도 함께 날아가도록 CASCADE 설정 (DB 단에서 처리)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_checklist_id", nullable = false, foreignKey = @ForeignKey(name = "fk_log_checklist"))
  private DailyChecklist dailyChecklist;

  // 누가 행위를 했는지 유저 정보 연결
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_log_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private ChecklistActionType actionType;

  // 로그는 수정되지 않으므로 @LastModifiedDate 없음
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public ChecklistHistoryLog(DailyChecklist dailyChecklist, User user, ChecklistActionType actionType) {
    this.dailyChecklist = dailyChecklist;
    this.user = user;
    this.actionType = actionType;
  }
}