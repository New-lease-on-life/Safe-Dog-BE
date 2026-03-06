package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.CareTemplateConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.CareTemplateCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.CareTemplateResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareTemplateService {

  private final CareTemplateRepository careTemplateRepository;
  private final PetRepository petRepository;
  private final CareTemplateConverter careTemplateConverter;

  @Transactional
  public CareTemplateResponse createTemplate(CareTemplateCreateRequest request) {
    // 1. 반려동물 검증 및 조회
    Pet pet = petRepository.findById(request.getPetId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반려동물입니다."));

    // 2. Converter를 통해 Entity 조립
    CareTemplate careTemplate = careTemplateConverter.toEntity(request, pet);

    // 3. 영속화
    CareTemplate savedTemplate = careTemplateRepository.save(careTemplate);

    // 4. Response DTO로 변환하여 반환
    return careTemplateConverter.toResponse(savedTemplate);
  }

  @Transactional
  public void deactivateTemplate(Long templateId) {
    // 1. 템플릿 조회
    CareTemplate template = careTemplateRepository.findById(templateId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿입니다."));

    // 2. 비활성화 (Soft Delete) - 더티 체킹에 의해 자동 UPDATE 쿼리 발생
    template.deactivate();
  }
}