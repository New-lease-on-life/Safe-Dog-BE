package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 회원-약관 동의(user_terms) 영속화. */
public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    /** 해당 회원의 약관별 동의 목록 */
    List<UserTerm> findAllByUserId(Long userId);

    /** 특정 회원의 특정 약관(TermType) 동의 여부 단건 조회 */
    Optional<UserTerm> findByUser_IdAndTerm_Type(Long userId, TermType termType);
}
