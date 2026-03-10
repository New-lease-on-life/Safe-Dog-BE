package com.newleaseonlife.SafeDogBe.domain.term.dto.response;

import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

/**
 * 약관 목록 응답. GET /api/terms 에서 사용.
 *
 * @param id       약관 PK
 * @param type     약관 종류 (SERVICE, PRIVACY, MARKETING)
 * @param required 필수 동의 여부
 */
public record TermResponse(
        Long id,
        TermType type,
        boolean required
) {
    public static TermResponse from(Term term) {
        return new TermResponse(term.getId(), term.getType(), term.isRequired());
    }
}
