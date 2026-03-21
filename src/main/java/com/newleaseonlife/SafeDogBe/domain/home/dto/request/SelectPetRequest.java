package com.newleaseonlife.SafeDogBe.domain.home.dto.request;

import jakarta.validation.constraints.NotNull;

public record SelectPetRequest(
        @NotNull(message = "반려동물 ID는 필수입니다.") Long petId
) {}
