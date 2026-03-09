package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Getter;

/**
 * 도메인/서비스 계층 비즈니스 예외. ApiCode(HTTP 상태·코드·메시지)와 선택적 상세 메시지를 담는다.
 * GlobalExceptionHandler에서 잡아 ErrorResponse로 변환해 반환한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ApiCode code;
    private final String detailMessage;

    public BusinessException(ApiCode code) {
        super(code.getMessage());
        this.code = code;
        this.detailMessage = null;
    }

    public BusinessException(ApiCode code, String detailMessage) {
        super(code.getMessage());
        this.code = code;
        this.detailMessage = detailMessage;
    }

    /** 클라이언트에 노출할 메시지. detailMessage가 있으면 사용, 없으면 code.getMessage(). */
    public String resolvedMessage() {
        return (detailMessage == null || detailMessage.isBlank())
                ? code.getMessage()
                : detailMessage;
    }
}
