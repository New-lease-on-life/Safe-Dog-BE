package com.newleaseonlife.SafeDogBe.global.error;

import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
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
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        ApiCode code = e.getCode();
        log.warn("[BusinessException] path={}, code={}, message={}", request.getRequestURI(), code.getCode(), e.resolvedMessage());

        return ResponseEntity
            .status(code.getHttpStatus())
            .body(ErrorResponse.of(
                code.getHttpStatus().value(),
                code.getCode(),
                e.resolvedMessage(),
                request.getRequestURI()
            ));
    }

    // @RequestBody + @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "잘못된 요청입니다.")
            .orElse(CommonErrorCode.BAD_REQUEST.getMessage());

        log.warn("[ValidationException] path={}, message={}", request.getRequestURI(), message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message, request.getRequestURI()));
    }

    // @Validated + @RequestParam 검증 실패 (UserController.checkNickname 등)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
            .findFirst()
            .map(v -> v.getMessage() != null ? v.getMessage() : "잘못된 요청입니다.")
            .orElse(CommonErrorCode.BAD_REQUEST.getMessage());

        log.warn("[ConstraintViolationException] path={}, message={}", request.getRequestURI(), message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message, request.getRequestURI()));
    }

    // JPA 낙관적 락 충돌
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        log.error("[OptimisticLockException] path={}, 동시 수정 충돌 발생", request.getRequestURI(), e);
        ApiCode code = CommonErrorCode.OPTIMISTIC_LOCK_CONFLICT;

        return ResponseEntity
            .status(code.getHttpStatus())
            .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage(), request.getRequestURI()));
    }

    // 파일 업로드 용량 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("[MaxUploadSizeExceededException] path={}, 파일 용량 초과", request.getRequestURI());
        ApiCode code = CommonErrorCode.FILE_SIZE_EXCEED;

        return ResponseEntity
            .status(code.getHttpStatus())
            .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage(), request.getRequestURI()));
    }

    // 잘못된 인자 (예: S3 파일 검증 실패 등) → 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        String message = e.getMessage() != null ? e.getMessage() : CommonErrorCode.BAD_REQUEST.getMessage();
        log.warn("[IllegalArgumentException] path={}, message={}", request.getRequestURI(), message);
        ApiCode code = CommonErrorCode.BAD_REQUEST;

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), code.getCode(), message, request.getRequestURI()));
    }
    /**
     * DB 제약 조건 위반 처리 (최후의 안전장치).
     * 서비스 로직에서 체크하지 못한 중복이나 Null 위반 등을 처리한다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("[DataIntegrityViolationException] path={}, message={}", request.getRequestURI(), e.getMessage());

        // 중복 키 에러인지 확인 (벤더마다 메시지가 다르지만 보통 "Duplicate" 키워드 포함)
        String message = "데이터 처리 중 충돌이 발생했습니다. 입력값을 확인해주세요.";
        if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
            message = "이미 존재하는 데이터(중복)입니다.";
        }

        ApiCode code = CommonErrorCode.BAD_REQUEST; // 혹은 409 CONFLICT 권장

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                code.getCode(),
                message,
                request.getRequestURI()
            ));
    }


    /** 그 외 미처리 예외 → 500 Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest request) {
        log.error("[UnhandledException] path={}, 예상치 못한 서버 에러", request.getRequestURI(), e);
        ApiCode code = CommonErrorCode.SERVER_ERROR;

        return ResponseEntity
            .status(code.getHttpStatus())
            .body(ErrorResponse.of(code.getHttpStatus().value(), code.getCode(), code.getMessage(), request.getRequestURI()));
    }
}