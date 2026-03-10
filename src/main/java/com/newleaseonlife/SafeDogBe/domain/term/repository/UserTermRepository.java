package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** 회원-약관 동의(user_terms) 영속화. */
public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    /** 해당 회원의 약관별 동의 목록 */
    List<UserTerm> findAllByUserId(Long userId);

    /** 해당 회원이 필수 약관을 모두 동의했는지 여부. true면 모두 동의함 */
    @Query("SELECT COUNT(t) = 0 FROM Term t WHERE t.required = true " +
           "AND t.id NOT IN (SELECT ut.term.id FROM UserTerm ut WHERE ut.user.id = :userId AND ut.agreed = true)")
    boolean hasAgreedAllRequiredTerms(@Param("userId") Long userId);
}
