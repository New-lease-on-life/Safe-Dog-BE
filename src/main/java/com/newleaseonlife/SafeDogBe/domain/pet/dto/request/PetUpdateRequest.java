package com.newleaseonlife.SafeDogBe.domain.pet.dto.request;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** * 반려동물 수정 요청. null 필드는 변경하지 않음(부분 수정).
 * * 3월 18일 수정 ✅ 추가: weight, isWeightUnknown, registrationNumber, isBirthDateUnknown, hasAllergy, allergyDescription, diseases
 * 3월 23일 수정 ✅ 수정: breed 단일 필드를 breedCode와 breedName으로 분리, Enum 검증 버그 수정
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetUpdateRequest {

    @Size(min = 1, max = 20)
    private String name;

    // 🚨 백엔드 버그 수정: @Size는 String, Collection, Array에만 적용 가능합니다.
    // Enum에 사용하면 유효성 검사 시 UnexpectedTypeException이 발생하여 500 에러가 터집니다.
    private SpeciesType species;

    // ✅ 분리된 품종 필드 적용
    @Schema(description = "품종 식별 코드 (예: MALTESE, ETC)", example = "ETC")
    @Size(max = 50, message = "품종 코드는 50자를 초과할 수 없습니다.")
    private String breedCode;

    @Schema(description = "화면 노출용 품종 이름 또는 직접 입력값", example = "말티푸")
    @Size(max = 100, message = "품종 이름은 100자를 초과할 수 없습니다.")
    private String breedName;

    private LocalDate birthDate;
    private Boolean isBirthDateUnknown;

    private Gender gender;
    private Boolean isNeutered;

    private BigDecimal weight;
    private Boolean isWeightUnknown;

    @Pattern(regexp = "^[0-9]{0,15}$", message = "동물등록번호는 숫자 15자리 이내여야 합니다.")
    private String registrationNumber;

    private Boolean hasAllergy;
    private String allergyDescription;

    private String profileImageUrl;

    @Size(max = 5, message = "질병은 최대 5개까지 선택 가능합니다.")
    private Set<PetDisease> diseases;
}