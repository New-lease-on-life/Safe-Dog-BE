package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 공통 에러 코드. (deprecation 경고: Spring HttpStatus 등 외부 API 사용으로 인한 ADVICE 수준) */
@SuppressWarnings("deprecation")
@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다."),
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, 400, "필수 헤더가 누락되었습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 에러가 발생했습니다."),
    FILE_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, 413, "10MB 이하의 이미지만 등록 가능합니다."),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, 409, "동시에 저장 요청이 발생했습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
