package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import java.time.LocalDateTime;

/**
 * 초대 코드 생성 응답.
 *
 * @param code      초대 코드 문자열
 * @param expiredAt 만료 일시 (생성 후 7일)
 */
public record InviteCodeResponse(String code, LocalDateTime expiredAt) {
}
