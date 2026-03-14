package com.newleaseonlife.SafeDogBe.domain.auth.service;

import com.newleaseonlife.SafeDogBe.domain.auth.converter.AuthConverter;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SignupRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SocialLinkRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.DeviceLoginProviderResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.OAuthAccount;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.RefreshToken;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.UserDevice;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.OAuthProvider;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.OAuthAccountRepository;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.RefreshTokenRepository;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.UserDeviceRepository;
import com.newleaseonlife.SafeDogBe.domain.auth.service.dto.SocialSignupCompleteRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.service.InviteCodeService;
import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementListRequest;
import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;
import com.newleaseonlife.SafeDogBe.domain.term.entity.Term;
import com.newleaseonlife.SafeDogBe.domain.term.entity.UserTerm;
import com.newleaseonlife.SafeDogBe.domain.term.service.TermService;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.TermErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스. 회원가입·로그인·토큰 갱신·로그아웃 처리. 로컬(이메일+비밀번호) 로그인과 소셜(OAuth2) 로그인 공통으로 토큰 발급.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Refresh Token DB 보관 기간(일).
     * JwtProperties.refreshTokenExpiration·CookieUtils.REFRESH_TOKEN_MAX_AGE_SECONDS와 맞춰야 함
     */
    private static final int REFRESH_TOKEN_VALID_DAYS = 90;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthConverter authConverter;
    private final TermService termService;
    private final InviteCodeService inviteCodeService;



    /**
     * AuthService.java (가입 완료 API 추가)
     * TermService를 주입받아 약관을 처리하고, 상태를 변경하며, 초대코드가 있다면 처리합니다.
     * 소셜 가입 후 온보딩(약관 동의 + 초대코드) 완료 처리
     */
    @Transactional
    public void completeSocialSignup(Long userId, SocialSignupCompleteRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BusinessException(AuthErrorCode.ALREADY_ACTIVE_USER);
        }

        // 1. 약관 동의 처리 (TermService의 로직 재사용 또는 호출)
        termService.agreeTerms(userId, new TermAgreementListRequest(request.terms()));

        // 2. 초대 코드가 존재하면 Joint Guardian(공동 보호자) 매핑 로직 실행
        if (StringUtils.hasText(request.inviteCode())) {
            log.info("[AuthService] 초대 코드 처리 시작: {}", request.inviteCode());
            // ✅ 기존에 만들어둔 InviteCodeService의 로직을 그대로 재사용하여 연결!
            inviteCodeService.joinByInviteCode(request.inviteCode(), userId);
        }

        // 3. 모든 필수 절차가 끝났으므로 ACTIVE 상태로 전환
        user.activate(); // user.status = UserStatus.ACTIVE;
        log.info("[AuthService] 정식 회원 전환 완료 userId={}", userId);
    }


    /**
     * Access/Refresh Token 발급 + DB에 Refresh Token 저장(기존 토큰 삭제 후 재저장)
     */
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
     * Refresh Token으로 Access/Refresh Token 재발급. JWT 서명·만료 검증 → DB 존재 확인 → 만료 여부 확인 → 재발급.
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

    /**
     * 소셜 계정 연결. 이미 로그인된 사용자가 추가 소셜 계정을 연결. 동일 provider+providerId가 이미 존재하면 SOCIAL_ALREADY_LINKED 예외.
     */
    @Transactional
    public void linkSocialAccount(Long userId, SocialLinkRequest request) {
        log.info("[AuthService] linkSocialAccount userId={}, provider={}", userId,
            request.provider());
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        OAuthProvider provider = request.provider();
        if (oAuthAccountRepository.findByProviderAndProviderId(provider, request.providerId())
            .isPresent()) {
            log.warn("[AuthService] linkSocialAccount 실패 - 이미 연결된 소셜 provider={}, providerId={}",
                provider, request.providerId());
            throw new BusinessException(AuthErrorCode.SOCIAL_ALREADY_LINKED);
        }

        OAuthAccount account = OAuthAccount.builder()
            .user(user)
            .provider(provider)
            .providerId(request.providerId())
            .build();
        oAuthAccountRepository.save(account);
        log.info("[AuthService] linkSocialAccount 완료 userId={}, provider={}", userId, provider);
    }

    /**
     * 기기별 마지막 로그인 소셜 타입 기록. 로그인 성공 후 FE가 별도 호출. 기존 기기 기록이 있으면 갱신, 없으면 신규 생성.
     */
    @Transactional
    public DeviceLoginProviderResponse registerDeviceLogin(String deviceId, String provider) {
        log.info("[AuthService] registerDeviceLogin deviceId={}, provider={}", deviceId, provider);
        UserDevice device = userDeviceRepository.findByDeviceId(deviceId)
            .map(d -> {
                d.updateLoginInfo(provider);
                return d;
            })
            .orElseGet(() -> userDeviceRepository.save(
                UserDevice.builder().deviceId(deviceId).lastLoginProvider(provider).build()
            ));
        return new DeviceLoginProviderResponse(device.getDeviceId(), device.getLastLoginProvider(),
            device.getLastLoginAt());
    }

    /**
     * 기기별 마지막 로그인 소셜 타입 조회. 비인증 공개 API. 등록된 기기 없으면 null 반환.
     */
    public DeviceLoginProviderResponse getDeviceLoginProvider(String deviceId) {
        return userDeviceRepository.findByDeviceId(deviceId)
            .map(d -> new DeviceLoginProviderResponse(d.getDeviceId(), d.getLastLoginProvider(),
                d.getLastLoginAt()))
            .orElse(null);
    }

    /**
     * 로그아웃. DB에서 Refresh Token 삭제. 쿠키 삭제는 컨트롤러에서 수행
     */
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
