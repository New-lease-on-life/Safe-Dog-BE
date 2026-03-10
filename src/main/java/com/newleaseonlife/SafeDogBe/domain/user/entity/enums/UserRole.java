package com.newleaseonlife.SafeDogBe.domain.user.entity.enums;

/** 회원 권한. Spring Security 권한 체크 및 API 접근 제어에 사용 */
public enum UserRole {
    /** 일반 회원 */
    USER,
    /** 관리자 */
    ADMIN
}
