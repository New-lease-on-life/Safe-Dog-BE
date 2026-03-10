package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

/** 반려동물 보호자 역할. pet_guardian 테이블 role 컬럼 */
public enum PetGuardianRole {
    /** 소유자(메인 보호자) */
    OWNER,
    /** 돌봄이(공동 보호자) */
    CAREGIVER
}
