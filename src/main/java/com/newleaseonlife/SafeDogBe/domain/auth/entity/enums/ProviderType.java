package com.newleaseonlife.SafeDogBe.domain.auth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 회원 가입 경로. User.providerType 컬럼에 저장. description은 한국어 표시명으로 응답 메시지에 사용 */
@Getter
@RequiredArgsConstructor
public enum ProviderType {
    /** 이메일+비밀번호 로컬 가입 */
    LOCAL("이메일"),
    /** Google 소셜 로그인 */
    GOOGLE("구글"),
    /** Naver 소셜 로그인 */
    NAVER("네이버"),
    /** Kakao 소셜 로그인 */
    KAKAO("카카오");

    /** 사용자 노출용 한국어 표시명 */
    private final String description;
}
