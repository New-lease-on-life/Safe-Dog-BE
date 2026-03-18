package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.WeightRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyWeightRecord;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** 3월 18일 수정*/
@Component
public class WeightRecordConverter {

  public WeightRecordResponse toResponse(DailyWeightRecord r) {
    if (r == null) return null;
    return WeightRecordResponse.builder()
        .id(r.getId())
        .petId(r.getPet().getId())
        .recordDate(r.getRecordDate())
        .weight(r.getWeight())
        .recordedByUserId(r.getRecordedBy() != null ? r.getRecordedBy().getId() : null)
        .recordedByNickname(r.getRecordedBy() != null ? r.getRecordedBy().getNickname() : null)
        .updatedAt(r.getUpdatedAt())
        .build();
  }

  public List<WeightRecordResponse> toResponseList(List<DailyWeightRecord> list) {
    if (list == null || list.isEmpty()) return Collections.emptyList();
    return list.stream().map(this::toResponse).toList();
  }
}