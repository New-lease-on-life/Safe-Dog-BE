package com.newleaseonlife.SafeDogBe.global.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final Integer code;
    private final String message;
}
