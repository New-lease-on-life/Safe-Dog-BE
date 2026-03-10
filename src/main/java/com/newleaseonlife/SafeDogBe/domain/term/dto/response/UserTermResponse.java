package com.newleaseonlife.SafeDogBe.domain.term.dto.response;

import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

import java.time.LocalDateTime;

/**
 * 회원별 약관 동의 현황 응답. GET /api/terms/my, POST /api/terms/agree 에서 사용.
 *
 * @param termId    약관 ID
 * @param termType  약관 종류
 * @param required  필수 동의 여부
 * @param agreed    해당 회원의 동의 여부
 * @param agreedAt  동의 시각 (미동의 시 null)
 */
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
