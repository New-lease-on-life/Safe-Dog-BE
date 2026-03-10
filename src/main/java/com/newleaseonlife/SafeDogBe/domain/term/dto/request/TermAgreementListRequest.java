package com.newleaseonlife.SafeDogBe.domain.term.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 약관 일괄 동의 요청. POST /api/terms/agree body.
 *
 * @param terms 약관별 동의 요청 목록 (비어있을 수 없음)
 */
public record TermAgreementListRequest(
        @NotEmpty(message = "약관 동의 목록은 비어있을 수 없습니다")
        @Valid
        List<TermAgreementRequest> terms
) {}
