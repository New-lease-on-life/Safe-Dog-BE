// domain/pet/dto/response/PetResponse.java
package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 3월 18일 수정 ✅ 추가: weight, isWeightUnknown, registrationNumber, isBirthDateUnknown, hasAllergy, allergyDescription
 * 3월 23일 수정 ✅ 수정: breed 단일 필드를 breedCode(DB 식별용)와 breedName(화면 표출용)으로 분리
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    private Long userId; // 메인 보호자 ID
    private String name;
    private SpeciesType species;

    // ✅ 분리된 품종 필드 적용
    private String breedCode; // 예: "MALTESE", "ETC"
    private String breedName; // 예: "말티즈", "말티푸(직접입력)"

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