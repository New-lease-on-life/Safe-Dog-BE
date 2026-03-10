package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

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

    /** 프로필 이미지 URL. Pet 엔티티가 TEXT 타입이므로 길이 제한 없음 */
    private String profileImageUrl;

    /**
     * 질병 목록 (선택). 입력 시 해당 질병에 맞는 케어 템플릿이 자동 생성됨.
     * 예: [DIABETES, ARTHRITIS]
     */
    private Set<PetDisease> diseases;
}
