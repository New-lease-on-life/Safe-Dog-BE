package com.newleaseonlife.SafeDogBe.domain.petnote.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 반려노트 등록 요청. POST /api/pet-notes body.
 * petId에 해당하는 Pet 소유자만 생성 가능.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteCreateRequest {

    /** 대상 반려동물 ID. 소유자 검증 후 Pet 엔티티로 매핑 */
    @NotNull(message = "반려동물 ID는 필수입니다.")
    private Long petId;

    /** 기록 대상일 */
    @NotNull(message = "기록 날짜는 필수입니다.")
    private LocalDate noteDate;

    /** 메모 내용. 선택 */
    private String content;

    /** 연결할 체크리스트 ID. 선택 */
    private Long linkedChecklistId;
}
