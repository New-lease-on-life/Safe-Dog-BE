package com.newleaseonlife.SafeDogBe.domain.auth.service;

import com.newleaseonlife.SafeDogBe.domain.auth.converter.AuthConverter;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.LoginRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SignupRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.RefreshToken;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.RefreshTokenRepository;
import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;
import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.repository.TermRepository;
import com.newleaseonlife.SafeDogBe.domain.term.repository.UserTermRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.TermErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 인증 서비스. 회원가입·로그인·토큰 갱신·로그아웃 처리.
 * 로컬(이메일+비밀번호) 로그인과 소셜(OAuth2) 로그인 공통으로 토큰 발급.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Refresh Token DB 보관 기간(일). JwtProperties.refreshTokenExpiration과 맞춰야 함 */
    private static final int REFRESH_TOKEN_VALID_DAYS = 7;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthConverter authConverter;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입. 이메일·닉네임 중복 검사 → User 저장 → 약관 동의 저장 */
    @Transactional
    public void signup(SignupRequest request) {
        log.info("[AuthService] signup email={}, nickname={}", request.getEmail(), request.getNickname());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AuthService] signup 실패 - 이메일 중복 email={}", request.getEmail());
            throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATION);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            log.warn("[AuthService] signup 실패 - 닉네임 중복 nickname={}", request.getNickname());
            throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATION);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .birthDate(request.getBirthDate())
                .status(UserStatus.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
        userRepository.save(user);

        saveTermAgreements(user, request.getTerms());
        log.info("[AuthService] signup 완료 userId={}", user.getId());
    }

    /** 필수 약관 동의 여부 검사 후 UserTerm 일괄 저장 */
    private void saveTermAgreements(User user, List<TermAgreementRequest> termRequests) {
        if (termRequests == null || termRequests.isEmpty()) {
            return;
        }
        Map<Long, Boolean> agreementMap = termRequests.stream()
                .collect(Collectors.toMap(TermAgreementRequest::termId, TermAgreementRequest::agreed));

        List<Term> requiredTerms = termRepository.findAllByRequired(true);
        for (Term required : requiredTerms) {
            Boolean agreed = agreementMap.get(required.getId());
            if (agreed == null || !agreed) {
                log.warn("[AuthService] 필수 약관 미동의 termId={}", required.getId());
                throw new BusinessException(TermErrorCode.REQUIRED_TERM_NOT_AGREED);
            }
        }

        List<Term> allTerms = termRepository.findAllById(agreementMap.keySet());
        if (allTerms.size() != agreementMap.size()) {
            throw new BusinessException(TermErrorCode.TERM_NOT_FOUND);
        }

        List<UserTerm> userTerms = allTerms.stream()
                .map(term -> UserTerm.builder()
                        .user(user)
                        .term(term)
                        .agreed(agreementMap.get(term.getId()))
                        .build())
                .toList();
        userTermRepository.saveAll(userTerms);
        log.info("[AuthService] 약관 동의 저장 완료 userId={}, count={}", user.getId(), userTerms.size());
    }

    /**
     * 로컬 로그인. 이메일로 유저 조회 → 비밀번호 검증 → 토큰 발급.
     * 소셜 전용 계정(password=null)은 LOGIN_FAILED 처리.
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("[AuthService] login email={}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[AuthService] login 실패 - 사용자 없음 email={}", request.getEmail());
                    return new BusinessException(AuthErrorCode.LOGIN_FAILED);
                });

        // 소셜 전용 계정(password null)이 로컬 로그인 시도 → NPE 방지 + 명시적 실패
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.warn("[AuthService] login 실패 - 소셜 전용 계정 userId={}", user.getId());
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[AuthService] login 실패 - 비밀번호 불일치 userId={}", user.getId());
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        user.updateLastLogin("LOCAL");
        return issueTokenResponse(user);
    }

    /** Access/Refresh Token 발급 + DB에 Refresh Token 저장(기존 토큰 삭제 후 재저장) */
    @Transactional
    public TokenResponse issueTokenResponse(User user) {
        log.debug("[AuthService] issueTokenResponse userId={}", user.getId());
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        refreshTokenRepository.deleteByUserId(user.getId());
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .createdAt(now)
                .expiredAt(now.plusDays(REFRESH_TOKEN_VALID_DAYS))
                .build());

        Long accessTokenExpiresIn = jwtTokenProvider.getAccessTokenExpirationMs();
        return authConverter.toTokenResponse(accessToken, refreshToken, accessTokenExpiresIn);
    }

    /**
     * Refresh Token으로 Access/Refresh Token 재발급.
     * JWT 서명·만료 검증 → DB 존재 확인 → 만료 여부 확인 → 재발급.
     */
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        log.info("[AuthService] refresh 요청");
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            log.warn("[AuthService] refresh 실패 - 토큰 유효성 실패");
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("[AuthService] refresh 실패 - 저장된 토큰 없음");
                    return new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });

        // DB에 저장된 토큰이 만료됐으면 재발급 거부
        if (storedToken.isExpired(LocalDateTime.now())) {
            log.warn("[AuthService] refresh 실패 - DB 토큰 만료 userId={}", storedToken.getUserId());
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(storedToken);
        return issueTokenResponse(user);
    }

    /** 로그아웃. DB에서 Refresh Token 삭제. 쿠키 삭제는 컨트롤러에서 수행 */
    @Transactional
    public void logout(String refreshToken) {
        log.info("[AuthService] logout 요청");
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("[AuthService] logout 실패 - 유효하지 않은 refreshToken");
                    return new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });
        refreshTokenRepository.delete(storedToken);
        log.info("[AuthService] logout 완료 userId={}", storedToken.getUserId());
    }
}
