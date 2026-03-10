package com.newleaseonlife.SafeDogBe.domain.petnote.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 반려노트 수정 요청. PATCH /api/pet-notes/{noteId} body.
 * null 필드는 변경하지 않음(부분 수정).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteUpdateRequest {

    /** 수정할 메모 내용 */
    private String content;

    /** 수정할 기록일. 날짜 변경 시 사용 */
    private LocalDate noteDate;
}
