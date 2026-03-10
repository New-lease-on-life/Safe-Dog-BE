package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호자 추가 요청. POST /api/pets/{petId}/guardians body.
 * 반려동물 소유자만 호출 가능.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianAddRequest {

    /** 보호자로 등록할 회원 ID */
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long userId;

    /** 역할. OWNER(소유자) 또는 CAREGIVER(돌봄이) */
    @NotNull(message = "역할은 필수입니다.")
    private PetGuardianRole role;
}
