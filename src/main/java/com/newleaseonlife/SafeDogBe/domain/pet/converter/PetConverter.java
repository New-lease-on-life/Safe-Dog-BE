package com.newleaseonlife.SafeDogBe.domain.pet.converter;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Pet · PetGuardian 엔티티 → API 응답 DTO 변환.
 */
@Component
public class PetConverter {

    /** Pet → PetResponse. 보호자 목록은 별도 API로 조회 */
    public PetResponse toResponse(Pet pet) {
        return PetResponse.builder()
                .id(pet.getId())
                .userId(pet.getUser().getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .gender(pet.getGender())
                .isNeutered(pet.isNeutered())
                .profileImageUrl(pet.getProfileImageUrl())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }

    /** Pet 목록 → PetResponse 목록 */
    public List<PetResponse> toResponseList(List<Pet> pets) {
        return pets.stream()
                .map(this::toResponse)
                .toList();
    }

    /** PetGuardian → PetGuardianResponse. 보호자 목록·추가 응답에 사용 */
    public PetGuardianResponse toGuardianResponse(PetGuardian guardian) {
        return PetGuardianResponse.builder()
                .id(guardian.getId())
                .userId(guardian.getUser().getId())
                .role(guardian.getRole())
                .build();
    }

    /** PetGuardian 목록 → PetGuardianResponse 목록 */
    public List<PetGuardianResponse> toGuardianResponseList(List<PetGuardian> guardians) {
        return guardians.stream()
                .map(this::toGuardianResponse)
                .toList();
    }
}
