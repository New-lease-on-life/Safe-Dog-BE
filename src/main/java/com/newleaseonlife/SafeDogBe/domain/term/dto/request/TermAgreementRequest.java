package com.newleaseonlife.SafeDogBe.domain.term.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 약관 1건 동의 요청. POST /api/terms/agree body 내 terms[] 요소.
 *
 * @param termId 대상 약관 ID
 * @param agreed 동의 여부
 */
public record TermAgreementRequest(
        @NotNull(message = "약관 ID는 필수입니다")
        Long termId,

        boolean agreed
) {}
