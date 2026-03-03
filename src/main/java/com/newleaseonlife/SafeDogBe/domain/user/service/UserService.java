package com.newleaseonlife.SafeDogBe.domain.user.service;

import com.newleaseonlife.SafeDogBe.domain.user.converter.UserConverter;
import com.newleaseonlife.SafeDogBe.domain.user.dto.request.UserUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.exception.BusinessException;
import com.newleaseonlife.SafeDogBe.global.exception.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;

    public UserResponse findById(Long userId) {
        log.debug("[UserService] findById userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] findById 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        return userConverter.toResponse(user);
    }

    public UserResponse findByEmail(String email) {
        log.debug("[UserService] findByEmail email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[UserService] findByEmail 실패 - 사용자 없음 email={}", email);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });
        return userConverter.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        log.info("[UserService] updateProfile userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[UserService] updateProfile 실패 - 사용자 없음 userId={}", userId);
                    return new BusinessException(UserErrorCode.USER_NOT_FOUND);
                });

        if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.nickname())) {
                log.warn("[UserService] updateProfile 실패 - 닉네임 중복 nickname={}", request.nickname());
                throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATION);
            }
        }

        user.updateProfile(request.name(), request.nickname(), request.profileImageUrl());
        log.info("[UserService] updateProfile 완료 userId={}", userId);
        return userConverter.toResponse(user);
    }

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
}
