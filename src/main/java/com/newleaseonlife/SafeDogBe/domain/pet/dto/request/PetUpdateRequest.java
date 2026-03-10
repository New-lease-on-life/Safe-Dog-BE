package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 반려동물 수정 요청. PATCH /api/pets/{petId} body.
 * null 필드는 변경하지 않음(부분 수정).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String species;

    @Size(max = 100)
    private String breed;

    private LocalDate birthDate;

    private Gender gender;

    private Boolean isNeutered;

    @Size(max = 500)
    private String profileImageUrl;
}
