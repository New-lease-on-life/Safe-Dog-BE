package com.newleaseonlife.SafeDogBe.domain.term.dto.response;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

import java.time.LocalDateTime;

public record UserTermResponse(
        Long termId,
        TermType termType,
        boolean required,
        boolean agreed,
        LocalDateTime agreedAt
) {
    public static UserTermResponse from(UserTerm userTerm) {
        return new UserTermResponse(
                userTerm.getTerm().getId(),
                userTerm.getTerm().getType(),
                userTerm.getTerm().isRequired(),
                userTerm.isAgreed(),
                userTerm.getAgreedAt()
        );
    }
}
