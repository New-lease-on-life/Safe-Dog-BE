package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 홈 화면 API 전용 비즈니스 오류 코드 */
@AllArgsConstructor
@Getter
public enum HomeErrorCode implements ApiCode {

    /** 선택/조회 대상 반려동물에 대한 보호자 권한 없음 */
    HOME_PET_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "홈에서 해당 반려동물에 접근할 권한이 없습니다."),

    /** 기본 반려동물을 결정할 수 없음(데이터 불일치 등) */
    HOME_PET_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "홈에서 조회할 반려동물을 찾을 수 없습니다."),

    /** 홈 API 처리 중 회원 엔티티 미존재 */
    HOME_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "홈 화면 조회에 필요한 회원 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
