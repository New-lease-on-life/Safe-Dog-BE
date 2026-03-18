// domain/pet/dto/response/PetResponse.java
package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** 수정 3월 18일
 * 반려동물 응답 DTO.
 *
 * ✅ 추가: weight, isWeightUnknown, registrationNumber,
 *          isBirthDateUnknown, hasAllergy, allergyDescription
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    private Long userId;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private boolean isBirthDateUnknown;
    private Gender gender;
    private boolean isNeutered;
    private BigDecimal weight;
    private boolean isWeightUnknown;
    private String registrationNumber;
    private Boolean hasAllergy;
    private String allergyDescription;
    private String profileImageUrl;
    private Set<PetDisease> diseases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}