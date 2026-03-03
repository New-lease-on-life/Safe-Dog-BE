package com.newleaseonlife.SafeDogBe.domain.term.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TermAgreementListRequest(
        @NotEmpty(message = "약관 동의 목록은 비어있을 수 없습니다")
        @Valid
        List<TermAgreementRequest> terms
) {}
