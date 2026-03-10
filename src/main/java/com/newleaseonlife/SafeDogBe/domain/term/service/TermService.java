package com.newleaseonlife.SafeDogBe.domain.term.service;

import com.newleaseonlife.SafeDogBe.domain.term.converter.TermConverter;
import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementListRequest;
import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.TermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.dto.response.UserTermResponse;
import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.repository.TermRepository;
import com.newleaseonlife.SafeDogBe.domain.term.repository.UserTermRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.TermErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 약관 도메인 서비스. 전체 약관 목록 조회, 회원별 동의 현황 조회, 약관 일괄 동의 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final UserRepository userRepository;
    private final TermConverter termConverter;

    /** 전체 약관 목록 조회 (관리용·회원가입 시 약관 노출용) */
    public List<TermResponse> getAllTerms() {
        log.debug("[TermService] getAllTerms");
        return termConverter.toTermResponseList(termRepository.findAll());
    }

    /** 해당 회원의 약관별 동의 현황 조회 */
    public List<UserTermResponse> getUserTerms(Long userId) {
        log.debug("[TermService] getUserTerms userId={}", userId);
        List<UserTerm> userTerms = userTermRepository.findAllByUserId(userId);
        return termConverter.toUserTermResponseList(userTerms);
    }

    /** 약관 일괄 동의. 필수 약관 미동의 시 REQUIRED_TERM_NOT_AGREED. 기존 UserTerm은 update, 없으면 생성 */
    @Transactional
    public List<UserTermResponse> agreeTerms(Long userId, TermAgreementListRequest request) {
        log.info("[TermService] agreeTerms userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        List<TermAgreementRequest> termRequests = request.terms();
        Map<Long, Boolean> agreementMap = termRequests.stream()
                .collect(Collectors.toMap(TermAgreementRequest::termId, TermAgreementRequest::agreed));

        List<Term> terms = termRepository.findAllById(agreementMap.keySet());
        if (terms.size() != agreementMap.size()) {
            throw new BusinessException(TermErrorCode.TERM_NOT_FOUND);
        }

        List<Term> requiredTerms = termRepository.findAllByRequired(true);
        for (Term required : requiredTerms) {
            Boolean agreed = agreementMap.get(required.getId());
            if (agreed == null || !agreed) {
                log.warn("[TermService] 필수 약관 미동의 termId={}", required.getId());
                throw new BusinessException(TermErrorCode.REQUIRED_TERM_NOT_AGREED);
            }
        }

        List<UserTerm> existingUserTerms = userTermRepository.findAllByUserId(userId);
        Map<Long, UserTerm> existingMap = existingUserTerms.stream()
                .collect(Collectors.toMap(ut -> ut.getTerm().getId(), ut -> ut));

        List<UserTerm> toSave = terms.stream()
                .map(term -> {
                    boolean agreed = agreementMap.get(term.getId());
                    UserTerm existing = existingMap.get(term.getId());
                    if (existing != null) {
                        existing.updateAgreed(agreed);
                        return existing;
                    }
                    return UserTerm.builder()
                            .user(user)
                            .term(term)
                            .agreed(agreed)
                            .build();
                })
                .toList();

        userTermRepository.saveAll(toSave);
        log.info("[TermService] agreeTerms 완료 userId={}, count={}", userId, toSave.size());
        return termConverter.toUserTermResponseList(userTermRepository.findAllByUserId(userId));
    }
}
