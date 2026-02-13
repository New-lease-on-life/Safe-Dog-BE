package com.newleaseonlife.SafeDogBe.domain.auth.service;

import com.newleaseonlife.SafeDogBe.domain.auth.converter.AuthConverter;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.LoginRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.RefreshTokenRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.request.SignupRequest;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.response.TokenResponse;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.RefreshToken;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.RefreshTokenRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.global.exception.BusinessException;
import com.newleaseonlife.SafeDogBe.global.exception.domain.AuthErrorCode;
import com.newleaseonlife.SafeDogBe.global.exception.domain.UserErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.JwtTokenProvider;
import com.newleaseonlife.SafeDogBe.domain.user.entity.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int REFRESH_TOKEN_VALID_DAYS = 14;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthConverter authConverter;
    private final PasswordEncoder passwordEncoder;

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
                .age(request.getAge())
                .status(UserStatus.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
        userRepository.save(user);
        log.info("[AuthService] signup 완료 userId={}", user.getId());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("[AuthService] login email={}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[AuthService] login 실패 - 사용자 없음 email={}", request.getEmail());
                    return new BusinessException(AuthErrorCode.LOGIN_FAILED);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[AuthService] login 실패 - 비밀번호 불일치 userId={}", user.getId());
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }

        return issueTokenResponse(user);
    }

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

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.delete(storedToken);
        return issueTokenResponse(user);
    }

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
