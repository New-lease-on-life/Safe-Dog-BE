package com.newleaseonlife.SafeDogBe.domain.user.entity.enums;

/** 회원 계정 상태. 로그인 허용 여부 및 휴면/탈퇴 처리에 사용 */
public enum UserStatus {
    /** 정상. 로그인 가능 */
    ACTIVE,
    /** 휴면. lastLoginAt 기준 일정 기간 미접속 시 전환, 로그인 시 복구 가능 */
    INACTIVE,
    /** 탈퇴. 로그인 불가, Soft Delete */
    WITHDRAWN,
    /** 소셜 최초 로그인 시 유저를 PENDING 상태로 저장하고, 프론트에서 약관 동의 + 초대코드를 한 번의 API 요청으로 묶어 처리하여 ACTIVE 상태로 전환 */
    PENDING

}
