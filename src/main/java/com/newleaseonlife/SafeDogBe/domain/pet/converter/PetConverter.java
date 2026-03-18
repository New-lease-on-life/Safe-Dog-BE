// domain/pet/converter/PetConverter.java
package com.newleaseonlife.SafeDogBe.domain.pet.converter;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import org.springframework.stereotype.Component;

import java.util.List;

/** 3월 18일 수정
 * ✅ 추가: weight, isWeightUnknown, registrationNumber,
 *          isBirthDateUnknown, hasAllergy, allergyDescription 매핑
 */
@Component
public class PetConverter {

    public PetResponse toResponse(Pet pet) {
        return PetResponse.builder()
            .id(pet.getId())
            .userId(pet.getUser().getId())
            .name(pet.getName())
            .species(pet.getSpecies())
            .breed(pet.getBreed())
            .birthDate(pet.getBirthDate())
            .isBirthDateUnknown(pet.isBirthDateUnknown())
            .gender(pet.getGender())
            .isNeutered(pet.isNeutered())
            .weight(pet.getWeight())
            .isWeightUnknown(pet.isWeightUnknown())
            .registrationNumber(pet.getRegistrationNumber())
            .hasAllergy(pet.getHasAllergy())
            .allergyDescription(pet.getAllergyDescription())
            .profileImageUrl(pet.getProfileImageUrl())
            .diseases(pet.getDiseases())
            .createdAt(pet.getCreatedAt())
            .updatedAt(pet.getUpdatedAt())
            .build();
    }

    public List<PetResponse> toResponseList(List<Pet> pets) {
        return pets.stream().map(this::toResponse).toList();
    }

    public PetGuardianResponse toGuardianResponse(PetGuardian guardian) {
        return PetGuardianResponse.builder()
            .id(guardian.getId())
            .userId(guardian.getUser().getId())
            .role(guardian.getRole())
            .build();
    }

    public List<PetGuardianResponse> toGuardianResponseList(List<PetGuardian> guardians) {
        return guardians.stream().map(this::toGuardianResponse).toList();
    }
}