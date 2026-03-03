package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {

  private final int status; // HTTP Status 숫자
  private final int code;   // 서비스 에러 코드 숫자
  private final String message;
  @Builder
  public ErrorResponse(int status, int code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  // 편의를 위한 정적 생성 메서드
  public static ErrorResponse of(int status, int code, String message) {
    return new ErrorResponse(status, code, message);
  }
}