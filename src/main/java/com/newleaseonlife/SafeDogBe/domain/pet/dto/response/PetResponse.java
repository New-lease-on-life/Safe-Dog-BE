package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 반려동물 응답. 조회·등록·수정 API 응답에 사용.
 * 보호자 목록은 GET /api/pets/{petId}/guardians 로 별도 조회.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {

    private Long id;
    /** 메인 보호자(소유자) ID */
    private Long userId;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private Gender gender;
    /** 중성화 여부 */
    private boolean isNeutered;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
