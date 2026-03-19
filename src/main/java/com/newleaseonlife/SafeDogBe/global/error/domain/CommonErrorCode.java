package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 3월 19일 수정
 * 공통 에러 코드. (deprecation 경고: Spring HttpStatus 등 외부 API 사용으로 인한 ADVICE 수준) */
@SuppressWarnings("deprecation")
@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {
    // 기존 에러 코드
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "잘못된 요청입니다."),
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST, 400, "필수 헤더가 누락되었습니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 에러가 발생했습니다."),
    FILE_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, 413, "10MB 이하의 이미지만 등록 가능합니다."),
    OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, 409, "동시에 저장 요청이 발생했습니다."),

    // 신규 에러 코드 (2026.03 기획서 반영)
    NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, 401, "로그인이 필요합니다."),
    NO_PERMISSION(HttpStatus.FORBIDDEN, 403, "관리자만 접근이 가능합니다."),
    PET_NOT_REGISTERED(HttpStatus.BAD_REQUEST, 400, "등록된 반려동물이 없습니다."),
    PET_NOTE_NOT_FOUND(HttpStatus.BAD_REQUEST, 400, "등록된 반려노트가 없습니다."),
    SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 500, "저장에 실패하였습니다. 다시 시도해 주세요."),
    CONCURRENT_EDIT_DETECTED(HttpStatus.CONFLICT, 409, "방금 다른 보호자가 체크리스트를 수정했어요. 마지막으로 수정한 체크리스트로 화면이 변경됩니다."),
    INVALID_FORM_DATA(HttpStatus.BAD_REQUEST, 400, "필수 항목을 입력해주세요.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
