package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode implements ApiCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 유저입니다."),
    NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST, 400, "이미 사용 중인 닉네임입니다."),
    ALREADY_REGISTERED_PHONE_NAME(HttpStatus.CONFLICT, 409, "이미 해당 전화번호와 이름으로 가입된 계정이 있어요.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
