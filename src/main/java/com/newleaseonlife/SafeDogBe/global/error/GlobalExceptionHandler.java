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

/**
 * 전역 예외 처리. 비즈니스 예외·검증 실패·파일 용량·IllegalArgument·기타 예외를
 * HTTP 상태코드와 ErrorResponse 형식으로 일관되게 반환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 도메인 비즈니스 예외 (AuthErrorCode, UserErrorCode 등). */
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

    // 잘못된 인자 (예: S3 파일 검증 실패 등) → 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        String message = e.getMessage() != null ? e.getMessage() : CommonErrorCode.BAD_REQUEST.getMessage();
        log.warn("[IllegalArgumentException] message={}", message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message));
    }

    /** 그 외 미처리 예외 → 500 Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e) {
        log.error("[UnhandledException] 예상치 못한 서버 에러", e);
        ApiCode code = CommonErrorCode.SERVER_ERROR;
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage()));
    }
}
