package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum TermErrorCode implements ApiCode {

    TERM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "존재하지 않는 약관입니다."),
    REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST.value(), 400, "필수 약관에 동의해야 합니다."),
    ALREADY_AGREED(HttpStatus.CONFLICT.value(), 409, "이미 동의한 약관입니다.");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
