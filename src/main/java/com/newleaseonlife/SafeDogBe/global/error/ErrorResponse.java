package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Builder;
import lombok.Getter;

/** API 오류 응답 DTO. status(HTTP 상태 코드), code(비즈니스 코드), message(클라이언트 메시지). */
@Getter
public class ErrorResponse {

    private final int status;
    private final int code;
    private final String message;

    @Builder
    public ErrorResponse(int status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    /** 편의 팩토리. GlobalExceptionHandler 등에서 사용. */
    public static ErrorResponse of(int status, int code, String message) {
        return new ErrorResponse(status, code, message);
    }
}
