package com.newleaseonlife.SafeDogBe.domain.mypage.dto.request;

import jakarta.validation.constraints.NotNull;

public record MypageMarketingConsentRequest(
        @NotNull Boolean agreed
) {}

