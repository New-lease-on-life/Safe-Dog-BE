package com.newleaseonlife.SafeDogBe.global.error;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** API 오류 응답 DTO. status(HTTP 상태 코드), code(비즈니스 코드), message(클라이언트 메시지). */
@Getter
@Builder
public class ErrorResponse {
    private final int status;
    private final Integer code;
    private final String message;

    // ✅ 모니터링 및 추적을 위해 추가된 필드
    private final String path;
    private final LocalDateTime timestamp;

    public static ErrorResponse of(int status, Integer code, String message, String path) {
        return ErrorResponse.builder()
            .status(status)
            .code(code)
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now()) // 에러 객체 생성 시점의 시간 자동 할당
            .build();
    }

}
