package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {

    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다."),
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, 400, "필수 헤더가 누락되었습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 에러가 발생했습니다."),

    // Concurrency & File (앞서 나온 내용들)
    FILE_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, 413, "10MB 이하의 이미지만 등록 가능합니다."),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, 409, "동시에 저장 요청이 발생했습니다.");

    private final HttpStatus httpStatus; // ResponseEntity에서 사용
    private final Integer code;          // JSON 바디에 표시될 숫자 코드
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
