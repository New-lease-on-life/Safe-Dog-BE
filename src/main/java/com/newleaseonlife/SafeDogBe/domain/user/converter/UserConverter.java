package com.newleaseonlife.SafeDogBe.domain.user.converter;

import com.newleaseonlife.SafeDogBe.domain.user.dto.response.UserResponse;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import org.springframework.stereotype.Component;

/**
 * User 엔티티 → UserResponse 변환. API 응답용 DTO 생성 시 사용.
 */
@Component
public class UserConverter {

    /** 엔티티를 응답 DTO로 변환. password, status 등은 노출하지 않음 */
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .providerType(user.getProviderType())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .isOnboardingCompleted(user.isOnboardingCompleted())
                .lastSelectedPetId(user.getLastSelectedPetId())
                .build();
    }
}
