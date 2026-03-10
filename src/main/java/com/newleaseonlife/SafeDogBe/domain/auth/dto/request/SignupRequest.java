package com.newleaseonlife.SafeDogBe.domain.auth.dto.request;

import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 회원가입 요청. POST /api/auth/signup body.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    /** 이메일. 로그인 ID로 사용 */
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    /** 비밀번호. 평문 전달, 서버에서 해시 저장 */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    /** 닉네임. 서비스 내 표시명 */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50)
    private String nickname;

    /** 생년월일 (선택) */
    private LocalDate birthDate;

    /** 약관 동의 목록. 필수 약관 포함 여부는 서버에서 검증 */
    @NotEmpty(message = "약관 동의 목록은 필수입니다.")
    @Valid
    private List<TermAgreementRequest> terms;
}
