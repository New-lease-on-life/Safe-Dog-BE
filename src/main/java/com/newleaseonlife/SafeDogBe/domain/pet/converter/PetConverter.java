package com.newleaseonlife.SafeDogBe.domain.pet.converter;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;

import org.springframework.stereotype.Component;

import java.util.List;

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
                .gender(pet.getGender())
                .isNeutered(pet.isNeutered())
                .profileImageUrl(pet.getProfileImageUrl())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }

    public List<PetResponse> toResponseList(List<Pet> pets) {
        return pets.stream()
                .map(this::toResponse)
                .toList();
    }
}
