package com.newleaseonlife.SafeDogBe.domain.term.repository;

import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findAllByRequired(boolean required);
}
