package com.newleaseonlife.SafeDogBe.domain.term.converter;

import com.newleaseonlife.SafeDogBe.domain.term.dto.response.TermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.UserTermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TermConverter {

    public TermResponse toTermResponse(Term term) {
        return new TermResponse(
                term.getId(),
                term.getType(),
                term.isRequired()
        );
    }

    public List<TermResponse> toTermResponseList(List<Term> terms) {
        return terms.stream()
                .map(this::toTermResponse)
                .toList();
    }

    public UserTermResponse toUserTermResponse(UserTerm userTerm) {
        return new UserTermResponse(
                userTerm.getTerm().getId(),
                userTerm.getTerm().getType(),
                userTerm.getTerm().isRequired(),
                userTerm.isAgreed(),
                userTerm.getAgreedAt()
        );
    }

    public List<UserTermResponse> toUserTermResponseList(List<UserTerm> userTerms) {
        return userTerms.stream()
                .map(this::toUserTermResponse)
                .toList();
    }
}
