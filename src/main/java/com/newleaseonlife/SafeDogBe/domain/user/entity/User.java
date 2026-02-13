package com.newleaseonlife.SafeDogBe.domain.user.entity;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SuppressWarnings({"UnusedAssignment", "unused"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 10)
    private Integer age;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProviderType providerType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public User(String email, String password, String nickname, Integer age, String profileImageUrl,
                UserStatus status, UserRole role, ProviderType providerType) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.age = age;
        this.profileImageUrl = profileImageUrl;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.providerType = providerType;
    }

    @SuppressWarnings("unused")
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
        this.updatedAt = LocalDateTime.now();
    }

    @SuppressWarnings("unused")
    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}
