package com.newleaseonlife.SafeDogBe.domain.petnote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 반려노트 응답. 조회·등록·수정 API 응답에 사용.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteResponse {

    /** 반려노트 PK */
    private Long id;
    /** 대상 반려동물 ID */
    private Long petId;
    /** 기록 대상일 */
    private LocalDate noteDate;
    /** 메모 내용 */
    private String content;
    /** 생성 일시 */
    private LocalDateTime createdAt;
    /** 수정 일시 */
    private LocalDateTime updatedAt;
}
