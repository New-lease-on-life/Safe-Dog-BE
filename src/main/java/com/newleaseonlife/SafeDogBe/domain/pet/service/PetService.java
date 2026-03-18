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
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 3월 18일 수정
 * ✅ 수정: create() — 신규 필드(weight, registrationNumber 등) 반영
 * ✅ 수정: update() — 신규 필드 반영, diseases 수정 지원
 * ✅ 수정: createDefaultCareTemplates() — PetDisease.defaultCheckItems() 기반 DISEASE_CARE 템플릿 생성
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

    public PetResponse findById(Long petId, Long userId) {
        return petConverter.toResponse(getPetAsOwnerOrThrow(petId, userId));
    }

    @Transactional
    public PetResponse create(Long userId, PetCreateRequest req) {
        log.info("[PetService] create userId={}, name={}", userId, req.getName());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

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

        // 질병별 기본 DISEASE_CARE 케어 템플릿 자동 생성
        if (!diseases.isEmpty()) {
            createDefaultCareTemplates(pet, diseases);
        }

        log.info("[PetService] create 완료 petId={}", pet.getId());
        return petConverter.toResponse(pet);
    }

    @Transactional
    public PetResponse update(Long petId, Long userId, PetUpdateRequest req) {
        log.info("[PetService] update petId={}, userId={}", petId, userId);
        Pet pet = getPetAsOwnerOrThrow(petId, userId);

        pet.update(
            req.getName(), req.getSpecies(), req.getBreed(),
            req.getBirthDate(), req.getIsBirthDateUnknown(),
            req.getGender(), req.getIsNeutered(),
            req.getWeight(), req.getIsWeightUnknown(),
            req.getRegistrationNumber(),
            req.getHasAllergy(), req.getAllergyDescription(),
            req.getProfileImageUrl()
        );

        // 질병 목록 수정 시 업데이트
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

    /** 보호자 제거. 소유자만 가능 */
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

    /**
     * 질병별 기본 DISEASE_CARE 케어 템플릿 생성.
     *
     * ✅ 변경: 기존 MEDICINE/HOSPITAL 타입 → DISEASE_CARE 타입
     * ✅ 변경: defaultTemplates() → defaultCheckItems() 기반으로 각 체크 항목을 개별 CareTemplate으로 생성
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

    private Pet getPetAsOwnerOrThrow(Long petId, Long userId) {
        return petRepository.findByIdAndUserId(petId, userId)
            .orElseThrow(() -> {
                if (petRepository.findById(petId).isEmpty()) {
                    return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                }
                return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
            });
    }
}