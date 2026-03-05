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
    private Long userId;
    private String name;
    private String species;
    private String breed;
    private LocalDate birthDate;
    private Gender gender;
    private boolean isNeutered;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
