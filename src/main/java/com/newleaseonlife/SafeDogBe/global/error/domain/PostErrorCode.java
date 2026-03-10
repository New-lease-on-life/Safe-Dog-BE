package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/** 게시글 도메인 오류 코드 (미존재, 접근 거부, 카테고리 미존재). */
@AllArgsConstructor
@Getter
public enum PostErrorCode implements ApiCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 게시글입니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "해당 게시글에 접근 권한이 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 카테고리입니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
