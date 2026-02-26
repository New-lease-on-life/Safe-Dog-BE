package com.newleaseonlife.SafeDogBe.domain.user.service;

import com.newleaseonlife.SafeDogBe.domain.user.converter.UserConverter;
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
