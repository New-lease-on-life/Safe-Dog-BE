package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** 약관(terms) 영속화. */
public interface TermRepository extends JpaRepository<Term, Long> {

    /** 필수/선택 여부로 약관 목록 조회 */
    List<Term> findAllByRequired(boolean required);
}
