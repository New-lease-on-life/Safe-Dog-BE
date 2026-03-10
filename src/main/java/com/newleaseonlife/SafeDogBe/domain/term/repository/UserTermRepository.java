package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 회원-약관 동의(user_terms) 영속화. */
public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    /** 해당 회원의 약관별 동의 목록 */
    List<UserTerm> findAllByUserId(Long userId);
}
