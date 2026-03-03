package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {

    List<UserTerm> findAllByUserId(Long userId);

    // 필수 약관을 모두 동의했는지 확인
    @Query("SELECT COUNT(t) = 0 FROM Term t WHERE t.required = true " +
           "AND t.id NOT IN (SELECT ut.term.id FROM UserTerm ut WHERE ut.user.id = :userId AND ut.agreed = true)")
    boolean hasAgreedAllRequiredTerms(@Param("userId") Long userId);
}
