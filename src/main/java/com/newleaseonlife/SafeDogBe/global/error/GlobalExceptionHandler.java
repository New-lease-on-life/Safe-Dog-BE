package com.newleaseonlife.SafeDogBe.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 1. 우리가 직접 정의한 비즈니스 예외 처리
  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.builder()
        .status(errorCode.getStatus().value())
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  // 2. 동시성 충돌 (JPA 낙관적 락 예외) 자동 캐치
  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  protected ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
    ErrorCode errorCode = ErrorCode.OPTIMISTIC_LOCK_CONFLICT;
    ErrorResponse response = ErrorResponse.builder()
        .status(errorCode.getStatus().value())
        .code(errorCode.getCode())
        .message(errorCode.getMessage()) // "동시에 저장 요청이 발생했습니다..." 팝업 문구 전달
        .build();
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  // 3. 파일 용량 초과 예외 자동 캐치
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  protected ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    ErrorCode errorCode = ErrorCode.FILE_SIZE_EXCEED;
    ErrorResponse response = ErrorResponse.builder()
        .status(errorCode.getStatus().value())
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();
    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }
}