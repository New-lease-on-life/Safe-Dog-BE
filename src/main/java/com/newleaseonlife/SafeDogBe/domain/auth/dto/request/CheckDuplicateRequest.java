package com.newleaseonlife.SafeDogBe.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckDuplicateRequest(
        @NotBlank(message = "전화번호는 필수입니다")
        String phone,

        @NotBlank(message = "이름은 필수입니다")
        String name
) {
}
