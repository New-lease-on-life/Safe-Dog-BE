package com.newleaseonlife.SafeDogBe.domain.petnote.entity;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "checklist_memo")
public class ChecklistMemo extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // [기획 반영] 100자 제한: DB 레벨에서도 강제
  @Column(length = 100, nullable = false)
  private String content;

  // [단방향 매핑] DailyChecklist에는 양방향 필드를 두지 않음
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "daily_checklist_id", nullable = false)
  private DailyChecklist dailyChecklist;

  // 작성자 (단방향)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Builder
  public ChecklistMemo(String content, DailyChecklist dailyChecklist, User author) {
    this.content = content;
    this.dailyChecklist = dailyChecklist;
    this.author = author;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}