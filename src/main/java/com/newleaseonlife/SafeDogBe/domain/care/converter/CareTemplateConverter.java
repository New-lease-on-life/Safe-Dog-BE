package com.newleaseonlife.SafeDogBe.domain.care.converter;

import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateItemRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateItemResponse;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplateItem;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import java.util.Map;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 3월 18일 수정 ✅ 변경: RepeatCycle → repeatCycleValue + repeatCycleUnit + repeatStartDate ✅ 추가:
 * timeSlot, items, 배변/체중 필드 매핑
 */
@Component
public class CareTemplateConverter {

  /**
   * ✅ 수정: items를 파라미터로 받는 오버로드 추가. CareTemplate.items 필드 제거로 인해 t.getItems() 호출 불가.
   */
  public CareTemplateResponse toResponse(CareTemplate t, List<CareTemplateItem> items) {
    if (t == null) {
      return null;
    }
    return CareTemplateResponse.builder()
        .id(t.getId())
        .petId(t.getPet().getId())
        .careType(t.getCareType())
        .careTypeDescription(t.getCareType().getDescription())
        .title(t.getTitle())
        .timeSlot(t.getTimeSlot())
        .timeSlotDescription(t.getTimeSlot() != null ? t.getTimeSlot().getDescription() : null)
        .customTimeSlot(t.getCustomTimeSlot())
        .repeatCycleValue(t.getRepeatCycleValue())
        .repeatCycleUnit(t.getRepeatCycleUnit())
        .repeatCycleUnitDescription(
            t.getRepeatCycleUnit() != null ? t.getRepeatCycleUnit().getDescription() : null)
        .repeatStartDate(t.getRepeatStartDate())
        .urineTrackingOn(t.isUrineTrackingOn())
        .fecesTrackingOn(t.isFecesTrackingOn())
        .weightRequestOn(t.isWeightRequestOn())
        .memo(t.getMemo())
        .isActive(t.isActive())
        .items(toItemResponseList(items))   // ← items 파라미터로 주입
        .build();
  }

  /**
   * items 없이 호출 시 빈 목록으로 처리 (단순 조회용)
   */
  public CareTemplateResponse toResponse(CareTemplate t) {
    return toResponse(t, Collections.emptyList());
  }

  public List<CareTemplateResponse> toResponseList(List<CareTemplate> templates,
      Map<Long, List<CareTemplateItem>> itemsMap) {
    return templates.stream()
        .map(t -> toResponse(t, itemsMap.getOrDefault(t.getId(), Collections.emptyList())))
        .toList();
  }

  public CareTemplate toEntity(CareTemplateCreateRequest req, Pet pet) {
    return CareTemplate.builder()
        .pet(pet)
        .careType(req.getCareType())
        .title(req.getTitle())
        .timeSlot(req.getTimeSlot())
        .customTimeSlot(req.getCustomTimeSlot())
        .repeatCycleValue(req.getRepeatCycleValue())
        .repeatCycleUnit(req.getRepeatCycleUnit())
        .repeatStartDate(req.getRepeatStartDate())
        .urineTrackingOn(req.isUrineTrackingOn())
        .fecesTrackingOn(req.isFecesTrackingOn())
        .weightRequestOn(req.isWeightRequestOn())
        .memo(req.getMemo())
        .build();
  }

  public CareTemplateItem toItemEntity(CareTemplateItemRequest req, CareTemplate template) {
    return CareTemplateItem.builder()
        .careTemplate(template)
        .itemName(req.getItemName())
        .foodType(req.getFoodType())
        .groomingType(req.getGroomingType())
        .customGroomingType(req.getCustomGroomingType())
        .preventionType(req.getPreventionType())
        .customPreventionType(req.getCustomPreventionType())
        .amount(req.getAmount())
        .amountUnit(req.getAmountUnit())
        .imageUrl(req.getImageUrl())
        .note(req.getNote())
        .sortOrder(req.getSortOrder())
        .build();
  }

  private CareTemplateItemResponse toItemResponse(CareTemplateItem item) {
    return CareTemplateItemResponse.builder()
        .id(item.getId())
        .itemName(item.getItemName())
        .foodType(item.getFoodType())
        .foodTypeDescription(
            item.getFoodType() != null ? item.getFoodType().getDescription() : null)
        .groomingType(item.getGroomingType())
        .customGroomingType(item.getCustomGroomingType())
        .preventionType(item.getPreventionType())
        .customPreventionType(item.getCustomPreventionType())
        .amount(item.getAmount())
        .amountUnit(item.getAmountUnit())
        .imageUrl(item.getImageUrl())
        .note(item.getNote())
        .sortOrder(item.getSortOrder())
        .build();
  }

  private List<CareTemplateItemResponse> toItemResponseList(List<CareTemplateItem> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }
    return items.stream().map(this::toItemResponse).toList();
  }
}