package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** 홈 반려동물 목록 조회 시 개별 반려동물 항목 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePetSummaryResponse {

    private Long id;
    private String name;
    private String profileImageUrl;
    private LocalDate birthDate;
    private boolean isBirthDateUnknown;
    private SpeciesType species;

    /** OWNER: 직접 등록, SHARED: 공유받은 반려동물 */
    private String registrationType;

    /** 이 반려동물에 대한 현재 사용자의 역할 (OWNER / CAREGIVER) */
    private String role;
}
