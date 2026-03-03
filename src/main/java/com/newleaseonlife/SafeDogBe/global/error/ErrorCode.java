package com.newleaseonlife.SafeDogBe.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum ErrorCode implements ApiCode { // 인터페이스 구현 명시

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "필수 입력 값을 작성해주세요."),
  FILE_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, "C002", "10MB 이하의 이미지만 등록 가능합니다."),
  SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 에러가 발생했습니다."),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "C004", "잘못된 요청입니다."),

  // ... 생략 ...

  OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "L001", "동시에 저장 요청이 발생했습니다.");

  private final HttpStatus status; // 필드명을 인터페이스와 맞춤
  private final String code;
  private final String message;

  @Override
  public HttpStatus getHttpStatus() {
    return this.status;
  }
}