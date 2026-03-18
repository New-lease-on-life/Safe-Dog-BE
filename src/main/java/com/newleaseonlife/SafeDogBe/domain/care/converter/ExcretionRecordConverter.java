package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.ExcretionRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyExcretionRecord;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
/**3월 18일 수정 */
@Component
public class ExcretionRecordConverter {

  public ExcretionRecordResponse toResponse(DailyExcretionRecord r) {
    if (r == null) return null;
    return ExcretionRecordResponse.builder()
        .id(r.getId())
        .dailyChecklistId(r.getDailyChecklist().getId())
        .petId(r.getPet().getId())
        .recordDate(r.getRecordDate())
        .excretionType(r.getExcretionType())
        .excretionTypeDescription(r.getExcretionType().getDescription())
        .isNormal(r.isNormal())
        .urineCount(r.getUrineCount())
        .urineColor(r.getUrineColor())
        .isUrineAccident(r.getIsUrineAccident())
        .fecesCount(r.getFecesCount())
        .fecesCondition(r.getFecesCondition())
        .recordedByUserId(r.getRecordedBy() != null ? r.getRecordedBy().getId() : null)
        .recordedByNickname(r.getRecordedBy() != null ? r.getRecordedBy().getNickname() : null)
        .updatedAt(r.getUpdatedAt())
        .build();
  }

  public List<ExcretionRecordResponse> toResponseList(List<DailyExcretionRecord> list) {
    if (list == null || list.isEmpty()) return Collections.emptyList();
    return list.stream().map(this::toResponse).toList();
  }
}