package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.DailyChecklistCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DailyChecklistConverter {

  // 1. Entity -> Response DTO 변환
  public DailyChecklistResponse toResponse(DailyChecklist checklist) {
    return Optional.ofNullable(checklist)
        .map(cl -> DailyChecklistResponse.builder()
            .id(cl.getId())
            .petId(cl.getPet().getId())
            // 템플릿이 없는 1회성 체크리스트 방어 로직
            .careTemplateId(cl.getCareTemplate() != null ? cl.getCareTemplate().getId() : null)
            .targetDate(cl.getTargetDate())
            .careType(cl.getCareType())
            .careTypeDescription(cl.getCareType().getDescription())
            .title(cl.getTitle())
            .content(cl.getContent())
            .isCompleted(cl.isCompleted())
            // 완료자가 아직 없는 경우를 방어하는 Null-Safe 로직
            .completedByUserId(cl.getCompletedBy() != null ? cl.getCompletedBy().getId() : null)
            .completedByNickname(cl.getCompletedBy() != null ? cl.getCompletedBy().getNickname() : null)
            .version(cl.getVersion())
            .build())
        .orElse(null);
  }

  // 2. Entity List -> Response DTO List 변환
  public List<DailyChecklistResponse> toResponseList(List<DailyChecklist> checklists) {
    if (checklists == null || checklists.isEmpty()) {
      return Collections.emptyList();
    }

    return checklists.stream()
        .map(this::toResponse)
        .toList();
  }

  // 3. Request DTO + 연관 Entity -> Entity 변환 (수동 1회성 할 일 생성용)
  // 템플릿 없이 수동으로 추가하는 경우이므로 careTemplate 파라미터는 생략합니다.
  public DailyChecklist toEntity(DailyChecklistCreateRequest request, Pet targetPet) {
    return DailyChecklist.builder()
        .pet(targetPet)
        .targetDate(request.getTargetDate())
        .careType(request.getCareType())
        .title(request.getTitle())
        .content(request.getContent())
        // careTemplate = null (빌더 기본 동작)
        // isCompleted = false (엔티티 기본 동작)
        .build();
  }
}