package com.newleaseonlife.SafeDogBe.global.error;

import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ApiCode code = e.getCode();
        log.warn("[BusinessException] code={}, message={}", code.getCode(), e.resolvedMessage());
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(
                        code.getHttpStatus().value(),
                        code.getCode(),
                        e.resolvedMessage()
                ));
    }

    // @RequestBody + @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "잘못된 요청입니다.")
                .orElse(CommonErrorCode.BAD_REQUEST.getMessage());
        log.warn("[ValidationException] message={}", message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message));
    }

    // @Validated + @RequestParam 검증 실패 (UserController.checkNickname 등)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage() != null ? v.getMessage() : "잘못된 요청입니다.")
                .orElse(CommonErrorCode.BAD_REQUEST.getMessage());
        log.warn("[ConstraintViolationException] message={}", message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message));
    }

    // JPA 낙관적 락 충돌
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        log.error("[OptimisticLockException] 동시 수정 충돌 발생", e);
        ApiCode code = CommonErrorCode.OPTIMISTIC_LOCK_CONFLICT;
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage()));
    }

    // 파일 업로드 용량 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.warn("[MaxUploadSizeExceededException] 파일 용량 초과");
        ApiCode code = CommonErrorCode.FILE_SIZE_EXCEED;
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e) {
        log.error("[UnhandledException] 예상치 못한 서버 에러", e);
        ApiCode code = CommonErrorCode.SERVER_ERROR;
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage()));
    }
}
