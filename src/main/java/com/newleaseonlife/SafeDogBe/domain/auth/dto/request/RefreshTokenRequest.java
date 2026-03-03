package com.newleaseonlife.SafeDogBe.domain.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    // nullable: 쿠키로 전달되는 경우 요청 바디 없이도 동작함
    private String refreshToken;
}
