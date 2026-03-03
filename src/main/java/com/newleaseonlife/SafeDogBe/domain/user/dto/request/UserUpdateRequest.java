package com.newleaseonlife.SafeDogBe.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 50, message = "이름은 50자 이하이어야 합니다")
        String name,

        @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하이어야 합니다")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글, 영문, 숫자, 마침표, 밑줄, 하이픈만 사용 가능합니다")
        String nickname,

        String profileImageUrl
) {}
