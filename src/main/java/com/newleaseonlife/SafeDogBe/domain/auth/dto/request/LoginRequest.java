package com.newleaseonlife.SafeDogBe.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로컬 로그인 요청. POST /api/auth/login body.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /** 이메일 */
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    /** 비밀번호 (평문) */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
