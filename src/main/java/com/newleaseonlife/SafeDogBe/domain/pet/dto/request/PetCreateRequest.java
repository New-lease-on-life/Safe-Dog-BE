package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 반려동물 등록 요청. POST /api/pets body.
 * 요청자는 메인 보호자(pet.user)로 저장됨.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
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
