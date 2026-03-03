package com.newleaseonlife.SafeDogBe.global.error;

import org.springframework.http.HttpStatus;

public interface ApiCode {
    HttpStatus getHttpStatus(); // 응답 상태 결정용 (Enum 객체)
    Integer getCode();          // 응답 바디의 code 필드용 (숫자)
    String getMessage();        // 응답 바디의 message 필드용
}