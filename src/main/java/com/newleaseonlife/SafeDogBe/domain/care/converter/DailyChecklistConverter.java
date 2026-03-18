package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** 3월 18일 수정
 * ✅ 추가: completedByProfileImageUrl 매핑
 * ✅ 추가: updatedAt 매핑
 */
@Component
public class DailyChecklistConverter {

  public DailyChecklistResponse toResponse(DailyChecklist cl) {
    return Optional.ofNullable(cl)
        .map(c -> DailyChecklistResponse.builder()
            .id(c.getId())
            .petId(c.getPet().getId())
            .careTemplateId(c.getCareTemplate() != null ? c.getCareTemplate().getId() : null)
            .targetDate(c.getTargetDate())
            .careType(c.getCareType())
            .careTypeDescription(c.getCareType().getDescription())
            .title(c.getTitle())
            .content(c.getContent())
            .isCompleted(c.isCompleted())
            .completedByUserId(c.getCompletedBy() != null ? c.getCompletedBy().getId() : null)
            .completedByNickname(c.getCompletedBy() != null ? c.getCompletedBy().getNickname() : null)
            .completedByProfileImageUrl(c.getCompletedBy() != null ? c.getCompletedBy().getProfileImageUrl() : null)
            .version(c.getVersion())
            .updatedAt(c.getUpdatedAt())
            .build())
        .orElse(null);
  }

  public List<DailyChecklistResponse> toResponseList(List<DailyChecklist> list) {
    if (list == null || list.isEmpty()) return Collections.emptyList();
    return list.stream().map(this::toResponse).toList();
  }
}