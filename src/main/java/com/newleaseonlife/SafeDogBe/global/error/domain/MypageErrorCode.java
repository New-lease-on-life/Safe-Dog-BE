package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 마이페이지 API 전용 비즈니스 오류 코드 */
@AllArgsConstructor
@Getter
public enum MypageErrorCode implements ApiCode {

    /** GET /api/mypage 의 petScope 파라미터가 OWNER/SHARED 가 아님 */
    MYPAGE_INVALID_PET_SCOPE(HttpStatus.BAD_REQUEST, 400, "petScope는 OWNER 또는 SHARED만 입력할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
