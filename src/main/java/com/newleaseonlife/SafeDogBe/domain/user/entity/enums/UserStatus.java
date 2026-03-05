package com.newleaseonlife.SafeDogBe.domain.user.entity.enums;

/** 회원 계정 상태. 로그인 허용 여부 및 휴면/탈퇴 처리에 사용 */
public enum UserStatus {
    /** 정상. 로그인 가능 */
    ACTIVE,
    /** 휴면. lastLoginAt 기준 일정 기간 미접속 시 전환, 로그인 시 복구 가능 */
    INACTIVE,
    /** 탈퇴. 로그인 불가, Soft Delete */
    WITHDRAWN
}
