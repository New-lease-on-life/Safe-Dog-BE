package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.ChecklistHistoryLogResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.ChecklistHistoryLog;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ChecklistHistoryLogConverter {

  // 1. Entity -> Response DTO 변환
  public ChecklistHistoryLogResponse toResponse(ChecklistHistoryLog log) {
    return Optional.ofNullable(log)
        .map(l -> ChecklistHistoryLogResponse.builder()
            .logId(l.getId())
            .dailyChecklistId(l.getDailyChecklist().getId())
            .actionType(l.getActionType())
            .actionTypeDescription(l.getActionType().getDescription())
            // 연관된 User 정보 매핑
            .userId(l.getUser().getId())
            .userNickname(l.getUser().getNickname())
            .userProfileImageUrl(l.getUser().getProfileImageUrl())
            .createdAt(l.getCreatedAt())
            .build())
        .orElse(null);
  }

  // 2. Entity List -> Response DTO List 변환 (타임라인 조회 API용)
  public List<ChecklistHistoryLogResponse> toResponseList(List<ChecklistHistoryLog> logs) {
    if (logs == null || logs.isEmpty()) {
      return Collections.emptyList();
    }

    return logs.stream()
        .map(this::toResponse)
        .toList();
  }
}