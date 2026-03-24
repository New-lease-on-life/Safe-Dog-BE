package com.newleaseonlife.SafeDogBe.domain.careReport.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CareReportResponse {
  /** 리포트 활성화 여부 (7개 이상 등록 시 true) */
  private boolean isReportAvailable;

  /** 질병 케어 통계 (기획: 무조건 상단 노출) */
  private CareCategoryStat diseaseCareStat;

  /** 기본 케어 통계 */
  private CareCategoryStat basicCareStat;

  @Getter
  @Builder
  public static class CareCategoryStat {
    private int totalNoteCount;
    private int completedNoteCount;
    private List<NoteDetailStat> noteDetails;
  }

  @Getter
  @Builder
  public static class NoteDetailStat {
    private Long templateId; // 노트 ID
    private String noteName; // 노트 이름 (예: 심장사상충 약)
    private int totalDays;
    private int completedDays;
  }
}