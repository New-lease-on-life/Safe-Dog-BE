package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 홈 화면 케어 진행률
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeCareProgressResponse {

  private long totalCount; // 오늘 요청된 전체 케어 수
  private long completedCount; // 완료된 케어 수
  private int percent;          // 수행률 수치 (%)
  private String emoji;         // 수행률 구간별 이모지 [cite: 10]
  private String message;       // 수행률 구간별 상태 문구

}
