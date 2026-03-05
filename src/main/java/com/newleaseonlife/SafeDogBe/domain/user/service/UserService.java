package com.newleaseonlife.SafeDogBe.domain.user.service;

import com.newleaseonlife.SafeDogBe.domain.user.converter.UserConverter;
import com.newleaseonlife.SafeDogBe.domain.user.dto.request.UserUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 회원 도메인 서비스. 프로필 조회·수정, 닉네임/전화번호+이름 중복 검사, 온보딩 완료·탈퇴·복구 처리.
 * 탈퇴 시 withdrawnAt 기록, 30일 내 복구 가능, 기록은 1년 보관 정책.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /** 탈퇴 후 복구 가능 일수. 이 기간이 지나면 복구 불가 */
    public static final int RESTORE_AVAILABLE_DAYS = 30;
    /** 탈퇴 회원 기록 보관 연수. 배치 등에서 withdrawnAt 기준 1년 경과 후 삭제/익명화 시 참고 */
    public static final int RECORD_RETENTION_YEARS = 1;

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;

    /** ID로 회원 조회. 없으면 USER_NOT_FOUND */
    public UserResponse findById(Long userId) {
        log.debug("[UserService] findById userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] findById 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        return userConverter.toResponse(user);
    }

    /** 이메일로 회원 조회. Auth 등 외부 도메인에서 사용 */
    public UserResponse findByEmail(String email) {
        log.debug("[UserService] findByEmail email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[UserService] findByEmail 실패 - 사용자 없음 email={}", email);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        return userConverter.toResponse(user);
    }

    /** 프로필 수정. 닉네임 변경 시 중복 검사 후 엔티티 반영 */
    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        log.info("[UserService] updateProfile userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] updateProfile 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });

        if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
            checkNicknameDuplicate(request.nickname());
        }

        user.updateProfile(request.name(), request.nickname(), request.profileImageUrl());
        log.info("[UserService] updateProfile 완료 userId={}", userId);
        return userConverter.toResponse(user);
    }

    /** 닉네임 중복 시 NICKNAME_DUPLICATION 예외. null/공백이면 검사 생략 */
    public void checkNicknameDuplicate(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return;
        }
        if (userRepository.existsByNickname(nickname.trim())) {
            log.info("[UserService] 닉네임 중복 감지 nickname={}", nickname);
            throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATION);
        }
    }

    /**
     * 전화번호 + 이름 기준 중복 여부 검사. 중복이면 BusinessException(ALREADY_REGISTERED_PHONE_NAME) 발생.
     */
    public void checkDuplicateByPhoneAndName(String phone, String name) {
        if (phone == null || phone.isBlank() || name == null || name.isBlank()) {
            return;
        }
        String p = phone.trim();
        String n = name.trim();
        if (userRepository.existsByPhoneAndName(p, n)) {
            log.info("[UserService] 전화번호+이름 중복 감지 phone={}, name={}", p, n);
            throw new BusinessException(UserErrorCode.ALREADY_REGISTERED_PHONE_NAME);
        }
    }

    /** 온보딩 완료 처리. isOnboardingCompleted = true 로 갱신 후 응답 DTO 반환 */
    @Transactional
    public UserResponse completeOnboarding(Long userId) {
        log.info("[UserService] completeOnboarding userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] completeOnboarding 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        user.completeOnboarding();
        log.info("[UserService] completeOnboarding 완료 userId={}", userId);
        return userConverter.toResponse(user);
    }

    /** 회원 탈퇴(Soft Delete). status = WITHDRAWN, withdrawnAt 기록. 30일 내 복구 가능, 기록 1년 보관 */
    @Transactional
    public void withdraw(Long userId) {
        log.info("[UserService] withdraw userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] withdraw 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        user.withdraw();
        log.info("[UserService] withdraw 완료 userId={}, withdrawnAt={}", userId, user.getWithdrawnAt());
    }

    /**
     * 탈퇴 복구(이메일 + 비밀번호). 로그인 불가 상태이므로 비인증 API에서 호출.
     * 탈퇴 후 30일 이내만 복구 가능. 소셜 전용 계정은 비밀번호가 없으므로 CANNOT_RESTORE.
     */
    @Transactional
    public UserResponse restore(String email, String rawPassword) {
        log.info("[UserService] restore 요청 email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[UserService] restore 실패 - 사용자 없음 email={}", email);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        if (user.getStatus() != UserStatus.WITHDRAWN || user.getWithdrawnAt() == null) {
            log.warn("[UserService] restore 불가 - 탈퇴 상태 아님 userId={}", user.getId());
            throw new BusinessException(UserErrorCode.CANNOT_RESTORE);
        }
        long daysSinceWithdraw = ChronoUnit.DAYS.between(user.getWithdrawnAt(), LocalDateTime.now());
        if (daysSinceWithdraw > RESTORE_AVAILABLE_DAYS) {
            log.warn("[UserService] restore 불가 - 복구 기간 만료 userId={}, days={}", user.getId(), daysSinceWithdraw);
            throw new BusinessException(UserErrorCode.RESTORE_PERIOD_EXPIRED);
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.warn("[UserService] restore 불가 - 소셜 전용 계정 userId={}", user.getId());
            throw new BusinessException(UserErrorCode.CANNOT_RESTORE);
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("[UserService] restore 실패 - 비밀번호 불일치 userId={}", user.getId());
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        user.restore();
        log.info("[UserService] restore 완료 userId={}", user.getId());
        return userConverter.toResponse(user);
    }

    /**
     * 탈퇴 복구(회원 ID). 이미 사용자가 식별된 경우(예: 소셜 로그인 콜백에서 WITHDRAWN 사용자 확인 후) 호출.
     * 30일 이내만 복구 가능.
     */
    @Transactional
    public UserResponse restore(Long userId) {
        log.info("[UserService] restore by userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.WITHDRAWN || user.getWithdrawnAt() == null) {
            throw new BusinessException(UserErrorCode.CANNOT_RESTORE);
        }
        long daysSinceWithdraw = ChronoUnit.DAYS.between(user.getWithdrawnAt(), LocalDateTime.now());
        if (daysSinceWithdraw > RESTORE_AVAILABLE_DAYS) {
            throw new BusinessException(UserErrorCode.RESTORE_PERIOD_EXPIRED);
        }
        user.restore();
        log.info("[UserService] restore 완료 userId={}", userId);
        return userConverter.toResponse(user);
    }
}
