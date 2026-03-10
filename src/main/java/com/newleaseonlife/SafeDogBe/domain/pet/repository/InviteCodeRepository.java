package com.newleaseonlife.SafeDogBe.domain.pet.repository;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.InviteCode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 초대 코드 영속화 */
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    /** 코드 문자열로 초대 코드 조회. 초대 링크 진입·검증 시 사용 */
    Optional<InviteCode> findByCode(String code);
}
