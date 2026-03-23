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

/**
 * 3월 18일 수정 ✅ 추가/변경: weight, registrationNumber, isBirthDateUnknown, hasAllergy, allergyDescription, diseases 5개 제한
 * 3월 23일 수정 ✅ 변경: breed 단일 필드 -> breedCode, breedName 분리 (바텀 시트 기획 반영)
 * ✅ 버그 수정: Enum 타입(SpeciesType)에 잘못 적용된 @Size 어노테이션 제거
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 1, max = 20, message = "이름은 1~20자 이내여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣._-]+$", message = "이름은 한글, 영문, 숫자, 특수문자(._-)만 가능합니다.")
    private String name;

    // Enum 타입에는 @Size를 사용할 수 없으므로 제거했습니다. (사용 시 500 에러 발생)
    private SpeciesType species;

    // ✅ 분리된 품종 필드
    @Size(max = 50, message = "품종 코드는 50자 이내여야 합니다.")
    private String breedCode; // 예: "MALTESE", "ETC"

    @Size(max = 100, message = "품종 이름은 100자 이내여야 합니다.")
    private String breedName; // 예: "말티즈", "말티푸(직접입력)"

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