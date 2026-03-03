package com.newleaseonlife.SafeDogBe.domain.term.dto.response;

import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

public record TermResponse(
        Long id,
        TermType type,
        boolean required
) {
    public static TermResponse from(Term term) {
        return new TermResponse(term.getId(), term.getType(), term.isRequired());
    }
}
