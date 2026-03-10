package com.newleaseonlife.SafeDogBe.domain.auth.entity.enums;

/** 회원 가입 경로. User.providerType 컬럼에 저장 */
public enum ProviderType {
    /** 이메일+비밀번호 로컬 가입 */
    LOCAL,
    /** Google 소셜 로그인 */
    GOOGLE,
    /** Naver 소셜 로그인 */
    NAVER,
    /** Kakao 소셜 로그인 */
    KAKAO
}
