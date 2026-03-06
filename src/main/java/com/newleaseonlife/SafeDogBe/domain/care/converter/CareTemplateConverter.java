package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CareTemplateConverter {

  // 1. Entity -> Response DTO 변환
  public CareTemplateResponse toResponse(CareTemplate careTemplate) {
    if (careTemplate == null) {
      return null;
    }

    return CareTemplateResponse.builder()
        .id(careTemplate.getId())
        .petId(careTemplate.getPet().getId())
        .careType(careTemplate.getCareType())
        .careTypeDescription(careTemplate.getCareType().getDescription())
        .title(careTemplate.getTitle())
        .content(careTemplate.getContent())
        .repeatCycle(careTemplate.getRepeatCycle())
        .repeatCycleDescription(careTemplate.getRepeatCycle().getDescription())
        .isActive(careTemplate.isActive())
        .build();
  }

  // 2. Entity List -> Response DTO List 변환 (this 키워드로 내부 인스턴스 메서드 호출)
  public List<CareTemplateResponse> toResponseList(List<CareTemplate> templates) {
    return templates.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  // 3. Request DTO + 연관 Entity -> Entity 변환
  public CareTemplate toEntity(CareTemplateCreateRequest request, Pet pet) {
    return CareTemplate.builder()
        .pet(pet)
        .careType(request.getCareType())
        .title(request.getTitle())
        .content(request.getContent())
        .repeatCycle(request.getRepeatCycle())
        // isActive는 Entity의 기본값(true) 또는 빌더 내에서 처리됨
        .build();
  }
}