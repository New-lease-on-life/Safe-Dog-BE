package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** 3월 18일 수정
 * 반려동물 수정 요청. null 필드는 변경하지 않음(부분 수정).
 *
 * ✅ 추가: weight, isWeightUnknown, registrationNumber,
 *          isBirthDateUnknown, hasAllergy, allergyDescription, diseases
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetUpdateRequest {

    @Size(min = 1, max = 20)
    private String name;

    @Size(max = 50)
    private SpeciesType species;

    @Size(max = 100)
    private String breed;

    private LocalDate birthDate;
    private Boolean isBirthDateUnknown;

    private Gender gender;
    private Boolean isNeutered;

    private BigDecimal weight;
    private Boolean isWeightUnknown;

    @Pattern(regexp = "^[0-9]{0,15}$", message = "동물등록번호는 숫자 15자리 이내여야 합니다.")
    private String registrationNumber;

    private Boolean hasAllergy;
    private String allergyDescription;

    private String profileImageUrl;

    @Size(max = 5, message = "질병은 최대 5개까지 선택 가능합니다.")
    private Set<PetDisease> diseases;
}