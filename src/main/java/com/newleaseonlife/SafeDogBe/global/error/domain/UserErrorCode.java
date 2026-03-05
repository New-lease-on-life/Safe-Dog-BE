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
    ALREADY_REGISTERED_PHONE_NAME(HttpStatus.CONFLICT, 409, "이미 해당 전화번호와 이름으로 가입된 계정이 있어요."),
    /** 탈퇴 후 30일 초과 시 복구 불가 */
    RESTORE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, 400, "탈퇴 후 30일이 지나 복구할 수 없습니다."),
    /** 복구 불가 상태(이미 정상 계정이거나 탈퇴 전이 아님) */
    CANNOT_RESTORE(HttpStatus.BAD_REQUEST, 400, "복구할 수 있는 상태가 아닙니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
