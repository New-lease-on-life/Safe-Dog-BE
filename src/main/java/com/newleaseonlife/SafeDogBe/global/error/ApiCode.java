package com.newleaseonlife.SafeDogBe.global.error;

import org.springframework.http.HttpStatus;

public interface ApiCode {

    HttpStatus getHttpStatus();

    Integer getCode();

    String getMessage();
}
