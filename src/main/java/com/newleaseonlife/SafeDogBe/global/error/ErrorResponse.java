package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Builder;
import lombok.Getter;

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

    public static ErrorResponse of(int status, int code, String message) {
        return new ErrorResponse(status, code, message);
    }
}
