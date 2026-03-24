package com.newleaseonlife.SafeDogBe.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    /** 실명. 1자 이상 20자 이하. 공백/이모지 금지 */
    @Size(min = 1, max = 20, message = "이름은 1자 이상 20자 이하이어야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "이름은 한글, 영문, 숫자, 마침표, 밑줄, 하이픈만 사용 가능합니다")
    String name,

    /** 닉네임. 2자 이상 12자 이하. 중복 불가 */
    @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하이어야 합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글, 영문, 숫자, 마침표, 밑줄, 하이픈만 사용 가능합니다")
    String nickname,

    /** 프로필 이미지 URL */
    String profileImageUrl
) {}