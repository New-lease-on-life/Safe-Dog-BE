package com.newleaseonlife.SafeDogBe.global.exception;

public interface ApiCode {

    Integer getHttpStatus();

    Integer getCode();

    String getMessage();
}
