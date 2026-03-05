package com.newleaseonlife.SafeDogBe.domain.user.dto.response;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String nickname;
    private String name;
    private LocalDate birthDate;
    private ProviderType providerType;
    private String profileImageUrl;
    private UserRole role;
    private boolean isOnboardingCompleted;
}
