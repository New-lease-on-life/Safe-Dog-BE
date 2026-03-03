package com.newleaseonlife.SafeDogBe.global.exception.domain;

import com.newleaseonlife.SafeDogBe.global.exception.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PetErrorCode implements ApiCode {

    PET_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "존재하지 않는 반려동물입니다."),
    PET_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), 403, "해당 반려동물에 접근 권한이 없습니다.");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
