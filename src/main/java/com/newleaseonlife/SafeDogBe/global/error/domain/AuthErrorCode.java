package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 인증 도메인 오류 코드 (이메일 중복, 로그인 실패, 리프레시 토큰 무효). */
@AllArgsConstructor
@Getter
public enum AuthErrorCode implements ApiCode {

    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, 400, "이미 존재하는 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 401, "아이디 또는 비밀번호가 잘못되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 리프레시 토큰입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
