package com.newleaseonlife.SafeDogBe.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원 프로필 수정 요청. PATCH /api/users/me body.
 * null 필드는 변경하지 않음(부분 수정).
 */
public record UserUpdateRequest(
        /** 실명. 50자 이하 */
        @Size(max = 50, message = "이름은 50자 이하이어야 합니다")
        String name,

        /** 닉네임. 한글·영문·숫자·마침표·밑줄·하이픈만 허용, 서비스에서 중복 검사 */
        @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하이어야 합니다")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9._-]+$", message = "닉네임은 한글, 영문, 숫자, 마침표, 밑줄, 하이픈만 사용 가능합니다")
        String nickname,

        /** 프로필 이미지 URL. TEXT 컬럼으로 긴 URL 허용 */
        String profileImageUrl
) {}
