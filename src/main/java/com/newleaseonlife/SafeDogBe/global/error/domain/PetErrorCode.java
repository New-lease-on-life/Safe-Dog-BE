package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 반려동물 도메인 오류 코드 (미존재, 접근 거부, 보호자 중복·미존재 등). */
@AllArgsConstructor
@Getter
public enum PetErrorCode implements ApiCode {

    PET_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 반려동물입니다."),
    PET_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "해당 반려동물에 접근 권한이 없습니다."),
    /** 동일 반려동물에 이미 등록된 보호자 */
    PET_GUARDIAN_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 해당 반려동물의 보호자로 등록된 사용자입니다."),
    /** 보호자 연결 없음(삭제 대상 없음) */
    PET_GUARDIAN_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "등록된 보호자 정보를 찾을 수 없습니다."),
    /** 존재하지 않는 초대 코드 */
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "유효하지 않은 초대 코드입니다."),
    /** 만료되었거나 이미 사용된 초대 코드 */
    INVITE_CODE_EXPIRED_OR_USED(HttpStatus.BAD_REQUEST, 400, "만료되었거나 이미 사용된 초대 코드입니다."),

    DUPLICATE_PET_NAME(HttpStatus.CONFLICT, 409, "이미 동일한 이름의 반려동물이 등록되어 있습니다."),
    DUPLICATE_REGISTRATION_NUMBER(HttpStatus.CONFLICT, 409, "이미 등록된 반려동물 등록번호입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
