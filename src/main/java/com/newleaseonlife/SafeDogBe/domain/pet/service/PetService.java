// domain/pet/service/PetService.java
package com.newleaseonlife.SafeDogBe.domain.pet.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.converter.PetConverter;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.GuardianAddRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 3월 18일 수정 ✅ 수정: create() — 신규 필드(weight, registrationNumber 등) 반영 ✅ 수정: update() — 신규 필드 반영,
 * diseases 수정 지원 ✅ 수정: createDefaultCareTemplates() — PetDisease.defaultCheckItems() 기반
 * DISEASE_CARE 템플릿 생성
 * 3월 23일 수정 ✅ 수정: 품종(breed) 필드를 breedCode와 breedName으로 분리하여 매핑 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

  private final PetRepository petRepository;
  private final PetGuardianRepository petGuardianRepository;
  private final UserRepository userRepository;
  private final CareTemplateRepository careTemplateRepository;
  private final PetConverter petConverter;

  public List<PetResponse> findMyPets(Long userId) {
    return petConverter.toResponseList(
        petRepository.findAllByUserIdOrderByCreatedAtDesc(userId));
  }

  /** 마이페이지용 내 반려동물 목록(오래된 순) */
  public List<PetResponse> findMyPetsOrderByCreatedAtAsc(Long userId) {
    return petConverter.toResponseList(
        petRepository.findAllByUserIdOrderByCreatedAtAsc(userId));
  }


  /** 마이페이지용 공유 받은 반려동물 목록 (보호자=CAREGIVER, 등록일 오래된 순) */
  public List<PetResponse> findMySharedPetsOrderByCreatedAtAsc(Long userId) {
    // [기획 반영] 타인의 초대코드를 통해 등록된(CAREGIVER) 동물만 열람
    return petGuardianRepository.findByUser_IdAndRole(userId, PetGuardianRole.CAREGIVER).stream()
        .map(PetGuardian::getPet)
        .distinct() // 중복 제거
        .sorted(Comparator.comparing(Pet::getCreatedAt)) // [기획 반영] 등록된 날짜(오래된) 순
        .map(petConverter::toResponse)
        .toList();
  }

  public PetResponse findById(Long petId, Long userId) {
    return petConverter.toResponse(getPetAsOwnerOrThrow(petId, userId));
  }

  public boolean isAdminOfPet(Long petId, Long userId) {
    return petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .map(guardian -> guardian.getRole() == PetGuardianRole.OWNER)
        .orElse(false);
  }

  public boolean hasAccessToPet(Long petId, Long userId) {
    return petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .isPresent();
  }

  // ---------------반려동물 생성(등록) ----------------------
  @Transactional
  public PetResponse create(Long userId, PetCreateRequest req) {
    log.info("[PetService] create userId={}, name={}", userId, req.getName());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    if (petRepository.existsByUserIdAndName(userId, req.getName())) {
      throw new BusinessException(PetErrorCode.DUPLICATE_PET_NAME);
    }

    if (req.getRegistrationNumber() != null && !req.getRegistrationNumber().isBlank()) {
      if (petRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
        throw new BusinessException(PetErrorCode.DUPLICATE_REGISTRATION_NUMBER);
      }
    }

    Set<PetDisease> diseases = req.getDiseases() != null ? req.getDiseases() : Set.of();

    // ✅ 변경점: breedCode와 breedName으로 분리하여 빌더에 주입
    Pet pet = Pet.builder()
        .user(user)
        .name(req.getName())
        .species(req.getSpecies())
        .breedCode(req.getBreedCode())
        .breedName(req.getBreedName())
        .birthDate(req.getBirthDate())
        .isBirthDateUnknown(req.isBirthDateUnknown())
        .gender(req.getGender())
        .isNeutered(req.getIsNeutered() != null && req.getIsNeutered())
        .weight(req.getWeight())
        .isWeightUnknown(req.isWeightUnknown())
        .registrationNumber(req.getRegistrationNumber())
        .hasAllergy(req.getHasAllergy())
        .allergyDescription(req.getAllergyDescription())
        .profileImageUrl(req.getProfileImageUrl())
        .diseases(diseases)
        .build();
    petRepository.save(pet);

    PetGuardian guardian = PetGuardian.builder()
        .user(user)
        .pet(pet)
        .role(PetGuardianRole.OWNER)
        .build();
    petGuardianRepository.save(guardian);

    if (!diseases.isEmpty()) {
      createDefaultCareTemplates(pet, diseases);
    }

    return petConverter.toResponse(pet);
  }

  private void createDefaultCareTemplates(Pet pet, Set<PetDisease> diseases) {
    List<CareTemplate> templates = new ArrayList<>();
    for (PetDisease disease : diseases) {
      for (String checkItem : disease.getDefaultCheckItems()) {
        templates.add(CareTemplate.builder()
            .pet(pet)
            .careType(CareType.DISEASE_CARE)
            .title("[" + disease.getDescription() + "] " + checkItem)
            .build());
      }
    }
    careTemplateRepository.saveAll(templates);
    log.info("[PetService] 질병 기반 CareTemplate {}개 자동 생성 petId={}", templates.size(), pet.getId());
  }

  // --------------------반려동물 수정----------------------
  @Transactional
  public PetResponse update(Long petId, Long userId, PetUpdateRequest req) {
    log.info("[PetService] update petId={}, userId={}", petId, userId);

    Pet pet = getPetAsOwnerOrThrow(petId, userId);

    if (!pet.getName().equals(req.getName())) {
      if (petRepository.existsByUserIdAndName(userId, req.getName())) {
        throw new BusinessException(PetErrorCode.DUPLICATE_PET_NAME);
      }
    }

    String newRegNum = req.getRegistrationNumber();
    if (newRegNum != null && !newRegNum.isBlank() && !newRegNum.equals(pet.getRegistrationNumber())) {
      if (petRepository.existsByRegistrationNumber(newRegNum)) {
        throw new BusinessException(PetErrorCode.DUPLICATE_REGISTRATION_NUMBER);
      }
    }

    // ✅ 변경점: update 메서드에 breedCode와 breedName을 각각 분리하여 전달
    pet.update(
        req.getName(), req.getSpecies(), req.getBreedCode(), req.getBreedName(),
        req.getBirthDate(), req.getIsBirthDateUnknown(),
        req.getGender(), req.getIsNeutered(),
        req.getWeight(), req.getIsWeightUnknown(),
        req.getRegistrationNumber(),
        req.getHasAllergy(), req.getAllergyDescription(),
        req.getProfileImageUrl()
    );

    if (req.getDiseases() != null) {
      pet.updateDiseases(req.getDiseases());
    }

    return petConverter.toResponse(pet);
  }

  @Transactional
  public void delete(Long petId, Long userId) {
    petRepository.delete(getPetAsOwnerOrThrow(petId, userId));
  }

  public List<PetGuardianResponse> getGuardians(Long petId, Long userId) {
    Pet pet = getPetAsOwnerOrThrow(petId, userId);
    return petConverter.toGuardianResponseList(
        petGuardianRepository.findByPetIdOrderByIdAsc(pet.getId()));
  }

  public List<PetGuardianResponse> getGuardiansForPet(Long petId, Long userId) {
    if (!hasAccessToPet(petId, userId)) {
      throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
    }
    return petConverter.toGuardianResponseList(petGuardianRepository.findByPetIdOrderByIdAsc(petId));
  }

  @Transactional
  public List<PetGuardianResponse> changePetOwner(Long petId, Long currentOwnerUserId, Long newOwnerUserId) {
    PetGuardian currentOwner = petGuardianRepository.findByPetIdAndUserId(petId, currentOwnerUserId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_GUARDIAN_NOT_FOUND));

    if (currentOwner.getRole() != PetGuardianRole.OWNER) {
      throw new BusinessException(CommonErrorCode.NO_PERMISSION);
    }

    PetGuardian newOwner = petGuardianRepository.findByPetIdAndUserId(petId, newOwnerUserId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_GUARDIAN_NOT_FOUND));

    Pet pet = currentOwner.getPet();

    if (newOwner.getRole() == PetGuardianRole.OWNER) {
      return petConverter.toGuardianResponseList(petGuardianRepository.findByPetIdOrderByIdAsc(petId));
    }

    currentOwner.changeRole(PetGuardianRole.CAREGIVER);
    newOwner.changeRole(PetGuardianRole.OWNER);
    pet.changeOwner(newOwner.getUser());

    return petConverter.toGuardianResponseList(petGuardianRepository.findByPetIdOrderByIdAsc(petId));
  }

  @Transactional
  public PetGuardianResponse addGuardian(Long petId, Long ownerUserId, GuardianAddRequest req) {
    Pet pet = getPetAsOwnerOrThrow(petId, ownerUserId);
    User targetUser = userRepository.findById(req.getUserId())
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    if (petGuardianRepository.existsByPetIdAndUserId(pet.getId(), req.getUserId())) {
      throw new BusinessException(PetErrorCode.PET_GUARDIAN_ALREADY_EXISTS);
    }

    PetGuardian guardian = PetGuardian.builder()
        .user(targetUser).pet(pet).role(req.getRole()).build();
    return petConverter.toGuardianResponse(petGuardianRepository.save(guardian));
  }

  @Transactional
  public void removeGuardian(Long petId, Long ownerUserId, Long guardianUserId) {
    log.info("[PetService] removeGuardian petId={}, ownerUserId={}, guardianUserId={}",
        petId, ownerUserId, guardianUserId);
    Pet pet = getPetAsOwnerOrThrow(petId, ownerUserId);
    PetGuardian guardian = petGuardianRepository.findByPetIdAndUserId(pet.getId(), guardianUserId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_GUARDIAN_NOT_FOUND));

    petGuardianRepository.delete(guardian);
    log.info("[PetService] removeGuardian 완료 guardianId={}", guardian.getId());
  }

  private Pet getPetAsOwnerOrThrow(Long petId, Long userId) {
    PetGuardian guardian = petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_ACCESS_DENIED));

    if (guardian.getRole() != PetGuardianRole.OWNER) {
      throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
    }

    return guardian.getPet();
  }
}