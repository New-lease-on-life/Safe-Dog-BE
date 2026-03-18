package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.CareTemplateConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateItemRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplateItem;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateItemRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CareErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/** 3월 18일 수정
 * ✅ 추가: updateTemplate() — 수정 기능
 * ✅ 추가: getTemplatesByPet() — 조회 기능
 * ✅ 변경: createTemplate() — 세부 항목(items) 저장 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareTemplateService {

  private final CareTemplateRepository careTemplateRepository;
  private final CareTemplateItemRepository careTemplateItemRepository;
  private final PetRepository petRepository;
  private final CareTemplateConverter converter;

  /** 반려동물의 활성 케어 템플릿 목록 조회 (items 포함) */
  public List<CareTemplateResponse> getTemplatesByPet(Long petId) {
    List<CareTemplate> templates = careTemplateRepository.findActiveByPetId(petId);
    if (templates.isEmpty()) return Collections.emptyList();

    // ✅ 수정: CareTemplate.items 필드 제거로 인해 별도 조회 필요
    List<Long> templateIds = templates.stream().map(CareTemplate::getId).toList();
    Map<Long, List<CareTemplateItem>> itemsMap =
        careTemplateItemRepository.findByCareTemplateIdIn(templateIds)
            .stream()
            .collect(Collectors.groupingBy(item -> item.getCareTemplate().getId()));

    return converter.toResponseList(templates, itemsMap);
  }

  /** 케어 템플릿 등록 (세부 항목 포함) */
  @Transactional
  public CareTemplateResponse createTemplate(CareTemplateCreateRequest req) {
    Pet pet = petRepository.findById(req.getPetId())
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_NOT_FOUND));

    CareTemplate template = converter.toEntity(req, pet);
    careTemplateRepository.save(template);

    List<CareTemplateItem> savedItems = Collections.emptyList();
    if (req.getItems() != null && !req.getItems().isEmpty()) {
      List<CareTemplateItem> items = req.getItems().stream()
          .map(itemReq -> converter.toItemEntity(itemReq, template))
          .toList();
      savedItems = careTemplateItemRepository.saveAll(items);
    }

    log.info("[CareTemplateService] createTemplate 완료 templateId={}", template.getId());
    return converter.toResponse(template, savedItems);
  }

  /** 케어 템플릿 수정 (세부 항목 전체 교체) */
  @Transactional
  public CareTemplateResponse updateTemplate(Long templateId, CareTemplateUpdateRequest req) {
    CareTemplate template = getTemplateOrThrow(templateId);

    template.update(
        req.getCareType(), req.getTitle(),
        req.getTimeSlot(), req.getCustomTimeSlot(),
        req.getRepeatCycleValue(), req.getRepeatCycleUnit(), req.getRepeatStartDate(),
        req.isUrineTrackingOn(), req.isFecesTrackingOn(),
        req.isWeightRequestOn(), req.getMemo()
    );

    if (req.getItems() != null) {
      // ✅ 수정: @Modifying(clearAutomatically = true) 덕분에 삭제 후 캐시 자동 클리어
      careTemplateItemRepository.deleteByCareTemplateId(templateId);

      List<CareTemplateItem> newItems = req.getItems().stream()
          .map(itemReq -> converter.toItemEntity(itemReq, template))
          .toList();
      careTemplateItemRepository.saveAll(newItems);
    }

    log.info("[CareTemplateService] updateTemplate 완료 templateId={}", templateId);
    // ✅ 수정: items 필드가 CareTemplate에 없으므로 converter에서 별도 조회 필요
    return converter.toResponse(template, careTemplateItemRepository.findByCareTemplateIdOrderBySortOrderAsc(templateId));
  }

  /** 케어 템플릿 비활성화 (Soft Delete) */
  @Transactional
  public void deactivateTemplate(Long templateId) {
    getTemplateOrThrow(templateId).deactivate();
  }

  private CareTemplate getTemplateOrThrow(Long templateId) {
    return careTemplateRepository.findById(templateId)
        .orElseThrow(() -> new BusinessException(CareErrorCode.CARE_TEMPLATE_NOT_FOUND));
  }
}