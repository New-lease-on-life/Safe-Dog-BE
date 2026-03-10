package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 약관 도메인 오류 코드 (약관 미존재, 필수 약관 미동의, 이미 동의 등). */
@AllArgsConstructor
@Getter
public enum TermErrorCode implements ApiCode {

    TERM_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 약관입니다."),
    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, 400, "필수 약관에 동의해야 합니다."),
    ALREADY_AGREED(HttpStatus.CONFLICT, 409, "이미 동의한 약관입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
