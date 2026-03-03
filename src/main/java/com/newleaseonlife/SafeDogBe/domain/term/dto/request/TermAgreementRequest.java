package com.newleaseonlife.SafeDogBe.domain.term.dto.request;

import jakarta.validation.constraints.NotNull;

public record TermAgreementRequest(
        @NotNull(message = "약관 ID는 필수입니다")
        Long termId,

        boolean agreed
) {}
