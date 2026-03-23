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

  /** 마이페이지용 공유 받은 반려동물 목록(보호자=CAREGIVER, 오래된 순) */
  public List<PetResponse> findMySharedPetsOrderByCreatedAtAsc(Long userId) {
    List<PetGuardian> caregiverGuardians =
        petGuardianRepository.findByUser_IdAndRole(userId, PetGuardianRole.CAREGIVER);

    // 사용자 기준으로 가져오되, pet이 중복되지 않도록 petId로 정리합니다.
    Map<Long, Pet> petMap = new HashMap<>();
    for (PetGuardian guardian : caregiverGuardians) {
      Pet pet = guardian.getPet();
      petMap.put(pet.getId(), pet);
    }

    List<Pet> pets = new ArrayList<>(petMap.values());
    pets.sort(Comparator.comparing(Pet::getCreatedAt));

    return petConverter.toResponseList(pets);
  }

  public PetResponse findById(Long petId, Long userId) {
    return petConverter.toResponse(getPetAsOwnerOrThrow(petId, userId));
  }

  /**
   * 사용자가 반려동물의 관리자인지 확인합니다.
   *
   * @param petId  반려동물 ID
   * @param userId 사용자 ID
   * @return 관리자이면 true, 아니면 false
   */
  public boolean isAdminOfPet(Long petId, Long userId) {
    return petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .map(guardian -> guardian.getRole() == PetGuardianRole.OWNER)
        .orElse(false);
  }

  /**
   * 사용자가 반려동물에 대한 접근 권한이 있는지 확인합니다.
   *
   * @param petId  반려동물 ID
   * @param userId 사용자 ID
   * @return 접근 권한이 있으면 true
   */
  public boolean hasAccessToPet(Long petId, Long userId) {
    return petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .isPresent();
  }

// ---------------반려동물 생성(등록) ----------------------
  @Transactional
  public PetResponse create(Long userId, PetCreateRequest req) {
    log.info("[PetService] create userId={}, name={}", userId, req.getName());
    // 유저 검증
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    // 등록번호 중복 체크
    if (req.getRegistrationNumber() != null && !req.getRegistrationNumber().isBlank()) {
      if (petRepository.existsByRegistrationNumber(req.getRegistrationNumber())) {
        throw new BusinessException(PetErrorCode.DUPLICATE_REGISTRATION_NUMBER);
      }
    }

    Set<PetDisease> diseases = req.getDiseases() != null ? req.getDiseases() : Set.of();

    Pet pet = Pet.builder()
        .user(user)
        .name(req.getName())
        .species(req.getSpecies())
        .breed(req.getBreed())
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

    // 생성자를 반려동물의 메인 보호자(OWNER)로 자동 등록하는 필수 로직 추가
    PetGuardian guardian = PetGuardian.builder()
        .user(user)
        .pet(pet)
        .role(PetGuardianRole.OWNER)
        .build();
    petGuardianRepository.save(guardian);

    // 질병별 기본 DISEASE_CARE 케어 템플릿 자동 생성
    if (!diseases.isEmpty()) {
      createDefaultCareTemplates(pet, diseases);
    }

    return petConverter.toResponse(pet);
  }

  /**
   * 질병별 기본 DISEASE_CARE 케어 템플릿 생성.
   * ✅ 변경: 기존 MEDICINE/HOSPITAL 타입 → DISEASE_CARE 타입 ✅ 변경: defaultTemplates() → defaultCheckItems()
   * 기반으로 각 체크 항목을 개별 CareTemplate으로 생성
   */
  private void createDefaultCareTemplates(Pet pet, Set<PetDisease> diseases) {
    List<CareTemplate> templates = new ArrayList<>();
    for (PetDisease disease : diseases) {
      for (String checkItem : disease.getDefaultCheckItems()) {
        templates.add(CareTemplate.builder()
            .pet(pet)
            .careType(CareType.DISEASE_CARE)
            .title("[" + disease.getDescription() + "] " + checkItem)
            // 주기 미설정 → 매일 생성
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
    // 권한(isAdminOfPet)은 Controller에서 이미 검증되었으므로, 불필요한 최초 생성자(user_id) 검증을 제거합니다.
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_NOT_FOUND));

    pet.update(
        req.getName(), req.getSpecies(), req.getBreed(),
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

  /**
   * 마이페이지용 보호자 목록 조회.
   * OWNER만 허용하는 getGuardians()와 달리, 해당 사용자(OWNER/CAREGIVER)가 접근 권한이 있으면 조회 가능합니다.
   */
  public List<PetGuardianResponse> getGuardiansForPet(Long petId, Long userId) {
    if (!hasAccessToPet(petId, userId)) {
      throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
    }
    return petConverter.toGuardianResponseList(petGuardianRepository.findByPetIdOrderByIdAsc(petId));
  }

  /**
   * 반려동물의 관리자(OWNER) 변경.
   * - 현재 요청자는 petId의 OWNER만 가능
   * - 새 OWNER로 지정된 구성원이 PetGuardian에 존재해야 함
   * - 역할을 OWNER <-> CAREGIVER로 스왑
   */
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

    // 이미 OWNER인 사용자를 선택한 경우: 상태 유지
    if (newOwner.getRole() == PetGuardianRole.OWNER) {
      return petConverter.toGuardianResponseList(petGuardianRepository.findByPetIdOrderByIdAsc(petId));
    }

    currentOwner.changeRole(PetGuardianRole.CAREGIVER);
    newOwner.changeRole(PetGuardianRole.OWNER);
    // pet.user(메인 보호자)는 반려노트/일부 소유권 검증에서 사용되므로 함께 변경
    pet.changeOwner(newOwner.getUser());

    // 스왑 후 전체 보호자 목록 반환
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

  /**
   * 보호자 제거. 소유자만 가능
   */
  @Transactional
  public void removeGuardian(Long petId, Long ownerUserId, Long guardianUserId) {
    log.info("[PetService] removeGuardian petId={}, ownerUserId={}, guardianUserId={}",
        petId, ownerUserId, guardianUserId);
    Pet pet = getPetAsOwnerOrThrow(petId, ownerUserId);
    PetGuardian guardian = petGuardianRepository.findByPetIdAndUserId(pet.getId(), guardianUserId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_GUARDIAN_NOT_FOUND));

    // ✅ [수정] Repository에서 직접 삭제
    petGuardianRepository.delete(guardian);
    log.info("[PetService] removeGuardian 완료 guardianId={}", guardian.getId());
  }



  private Pet getPetAsOwnerOrThrow(Long petId, Long userId) {
    // 1. PetGuardian 테이블을 조회하여 해당 유저가 이 반려동물의 권한이 있는지 확인
    PetGuardian guardian = petGuardianRepository.findByPetIdAndUserId(petId, userId)
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_ACCESS_DENIED));

    // 2. 권한이 OWNER인지 확인 (선택적: ADMIN이나 READ_ONLY 등 세부 권한이 있다면 분기 처리)
    if (guardian.getRole() != PetGuardianRole.OWNER) {
      throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
    }

    // 3. 검증이 끝났으므로 Pet 객체를 반환
    return guardian.getPet();
  }
}