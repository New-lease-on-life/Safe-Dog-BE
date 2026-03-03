package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    // RuntimeException의 기본 메시지로 ErrorCode의 메시지를 전달하여
    // 서버 로그(콘솔)에서도 에러 원인을 쉽게 파악할 수 있도록 합니다.
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}