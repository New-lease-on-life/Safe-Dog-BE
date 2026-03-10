package com.newleaseonlife.SafeDogBe.domain.term.converter;

import com.newleaseonlife.SafeDogBe.domain.term.dto.response.TermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.UserTermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;

import org.springframework.stereotype.Component;

import java.util.List;

/** Term·UserTerm 엔티티를 응답 DTO로 변환. */
@Component
public class TermConverter {

    /** Term → TermResponse */
    public TermResponse toTermResponse(Term term) {
        return new TermResponse(
                term.getId(),
                term.getType(),
                term.isRequired()
        );
    }

    /** Term 목록 → TermResponse 목록 */
    public List<TermResponse> toTermResponseList(List<Term> terms) {
        return terms.stream()
                .map(this::toTermResponse)
                .toList();
    }

    /** UserTerm → UserTermResponse */
    public UserTermResponse toUserTermResponse(UserTerm userTerm) {
        return new UserTermResponse(
                userTerm.getTerm().getId(),
                userTerm.getTerm().getType(),
                userTerm.getTerm().isRequired(),
                userTerm.isAgreed(),
                userTerm.getAgreedAt()
        );
    }

    /** UserTerm 목록 → UserTermResponse 목록 */
    public List<UserTermResponse> toUserTermResponseList(List<UserTerm> userTerms) {
        return userTerms.stream()
                .map(this::toUserTermResponse)
                .toList();
    }
}
