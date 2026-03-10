package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호자 한 명 응답. GET /api/pets/{petId}/guardians 목록 항목.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetGuardianResponse {

    /** pet_guardian PK */
    private Long id;
    /** 보호자 회원 ID */
    private Long userId;
    /** 역할 (OWNER / CAREGIVER) */
    private PetGuardianRole role;
}
