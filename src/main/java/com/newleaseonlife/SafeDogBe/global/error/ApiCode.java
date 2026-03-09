package com.newleaseonlife.SafeDogBe.global.error;

import org.springframework.http.HttpStatus;

/** API 오류 코드 공통 규약. HTTP 상태, 숫자 코드, 클라이언트 메시지를 제공한다. */
public interface ApiCode {

    HttpStatus getHttpStatus();

    Integer getCode();

    String getMessage();
}
