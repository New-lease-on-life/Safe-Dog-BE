package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** 수정 3월 18일
 * 반려동물 등록 요청.
 *
 * ✅ 추가: weight, isWeightUnknown
 * ✅ 추가: registrationNumber (동물등록번호 15자리)
 * ✅ 추가: isBirthDateUnknown
 * ✅ 추가: hasAllergy, allergyDescription
 * ✅ 변경: diseases 최대 5개 제한 (기획서 step3)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 1, max = 20, message = "이름은 1~20자 이내여야 합니다.")
    private String name;

    @Size(max = 50)
    private SpeciesType species;

    @Size(max = 100)
    private String breed;

    /** 출생일 (isBirthDateUnknown=true이면 null 허용) */
    private LocalDate birthDate;

    /** 출생일 모름 체크박스. true이면 birthDate 무시 */
    private boolean isBirthDateUnknown = false;

    private Gender gender;

    private Boolean isNeutered;

    /** 체중 (kg). isWeightUnknown=true이면 null 허용 */
    private BigDecimal weight;

    /** 체중 모름 체크박스 */
    private boolean isWeightUnknown = false;

    /**
     * 동물등록번호. 숫자만, 최대 15자리. 선택.
     */
    @Pattern(regexp = "^[0-9]{0,15}$", message = "동물등록번호는 숫자 15자리 이내여야 합니다.")
    private String registrationNumber;

    /** 알레르기 여부 (있어요/없어요). null이면 미응답 */
    private Boolean hasAllergy;

    /** 알레르기 직접 입력 내용. hasAllergy=true일 때 사용 */
    private String allergyDescription;

    private String profileImageUrl;

    /**
     * 질병 목록. 최대 5개.
     * 기획서: 심장병, 신장질환, 암, 안과질환, 쿠싱 증후군, 관절염
     */
    @Size(max = 5, message = "질병은 최대 5개까지 선택 가능합니다.")
    private Set<PetDisease> diseases;
}