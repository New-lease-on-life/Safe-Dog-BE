package com.newleaseonlife.SafeDogBe.domain.user.service;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
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
    private final PetGuardianRepository petGuardianRepository;

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

        // 닉네임 정규화: 영문 대문자 → 소문자, 앞뒤 공백 제거
        String normalizedNickname = request.nickname() != null ? request.nickname().trim().toLowerCase() : null;
        if (normalizedNickname != null && !normalizedNickname.equals(user.getNickname())) {
            checkNicknameDuplicate(normalizedNickname);
        }

        user.updateProfile(request.name(), normalizedNickname, request.profileImageUrl());
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
     * 전화번호 + 이름 기준 중복 여부 검사.
     * 중복이면 기존 계정의 소셜 타입을 포함한 메시지로 ALREADY_REGISTERED_PHONE_NAME 예외 발생.
     * 예: "카카오로 가입된 계정이 있어요. 카카오로 로그인해 주세요."
     */
    public void checkDuplicateByPhoneAndName(String phone, String name) {
        if (phone == null || phone.isBlank() || name == null || name.isBlank()) {
            return;
        }
        String p = phone.trim();
        String n = name.trim();
        userRepository.findByPhoneAndName(p, n).ifPresent(existing -> {
            log.info("[UserService] 전화번호+이름 중복 감지 phone={}, name={}", p, n);
            String providerDesc = existing.getProviderType() != null
                    ? existing.getProviderType().getDescription()
                    : "기존 계정";
            String detail = providerDesc + "로 가입된 계정이 있어요. " + providerDesc + "로 로그인해 주세요.";
            throw new BusinessException(UserErrorCode.ALREADY_REGISTERED_PHONE_NAME, detail);
        });
    }

    /** 마지막으로 선택한 반려동물 ID 갱신. 다음 접속 시 기본 반려동물로 내려줌. petId = null이면 선택 해제 */
    @Transactional
    public UserResponse updateLastSelectedPet(Long userId, Long petId) {
        log.info("[UserService] updateLastSelectedPet userId={}, petId={}", userId, petId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        user.updateLastSelectedPet(petId);
        return userConverter.toResponse(user);
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

    /** 회원 탈퇴(Soft Delete). status = WITHDRAWN, withdrawnAt 기록. 30일 내 복구 가능, 기록 1년 보관.
     *  OWNER 권한인 반려동물이 있으면 권한 위임 전까지 탈퇴 불가 */
    @Transactional
    public void withdraw(Long userId) {
        log.info("[UserService] withdraw userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] withdraw 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        if (petGuardianRepository.existsByUser_IdAndRole(userId, PetGuardianRole.OWNER)) {
            log.warn("[UserService] withdraw 실패 - OWNER 권한 위임 전 탈퇴 불가 userId={}", userId);
            throw new BusinessException(UserErrorCode.CANNOT_WITHDRAW_AS_OWNER);
        }
        user.withdraw();
        log.info("[UserService] withdraw 완료 userId={}, withdrawnAt={}", userId, user.getWithdrawnAt());
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
