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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 회원 엔티티.
 * 로컬/소셜 로그인 공통 프로필, 상태(ACTIVE/INACTIVE/WITHDRAWN), 온보딩·로그인 추적 필드를 가짐.
 * updatedAt은 JPA Auditing으로 자동 갱신되므로 비즈니스 메서드에서 수동 설정하지 않음.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = {"nickname"})
        },
        indexes = {
                @Index(name = "idx_users_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 (소셜 전용 가입 시 null 가능) */
    @Column(unique = true, length = 100)
    private String email;

    /** 비밀번호 해시. 평문 저장 금지. 소셜 전용 시 null */
    @Column(length = 255)
    private String password;

    /** 닉네임. 서비스 내 표시명, 중복 불가 */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    /** 실명 (선택) */
    @Column(length = 50)
    private String name;

    /** 연락처 (선택) */
    @Column(length = 20)
    private String phone;

    /** 생년월일 (선택) */
    private LocalDate birthDate;

    /** 프로필 이미지 URL. 긴 URL 대비 TEXT 사용 */
    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    /** 계정 상태. ACTIVE / INACTIVE(휴면) / WITHDRAWN(탈퇴) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /** 권한. USER / ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /** 가입 경로. LOCAL / GOOGLE / NAVER / KAKAO */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProviderType providerType;

    /** 마지막 로그인 시각. 휴면 배치·UX용 */
    private LocalDateTime lastLoginAt;

    /** 마지막 로그인 시 사용한 provider (GOOGLE, NAVER, KAKAO 등) */
    @Column(length = 20)
    private String lastLoginProvider;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /** 온보딩(최초 설정) 완료 여부. true 시 온보딩 화면 스킵 */
    @Column(nullable = false)
    private boolean isOnboardingCompleted = false;

    /** 탈퇴 요청 시각. Soft Delete 시점 기준 30일 내 복구 가능, 1년간 기록 보관 */
    private LocalDateTime withdrawnAt;

    @Builder
    public User(String email, String password, String nickname, String name, String phone,
                LocalDate birthDate, String profileImageUrl,
                UserStatus status, UserRole role, ProviderType providerType) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.role = role != null ? role : UserRole.USER;
        this.providerType = providerType;
    }

    /** 닉네임 변경. 중복 검사는 서비스 계층에서 수행 */
    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /** 프로필(이름, 닉네임, 프로필 이미지) 일괄 수정. null 필드는 기존 값 유지 */
    public void updateProfile(String name, String nickname, String profileImageUrl) {
        if (name != null) this.name = name;
        if (nickname != null) this.nickname = nickname;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    /** 추가 정보(이름, 연락처, 생년월일) 수정. 본인인증 등에서 사용 */
    public void updateAdditionalInfo(String name, String phone, LocalDate birthDate) {
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    /** 온보딩 완료 처리 (최초 로그인 후 한 번만 호출) */
    public void completeOnboarding() {
        this.isOnboardingCompleted = true;
    }

    /** 탈퇴 처리 (Soft Delete). status = WITHDRAWN, withdrawnAt 기록. 30일 내 복구 가능, 기록은 1년 보관 */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /** 탈퇴 복구. 30일 이내에만 유효(호출 전 서비스 계층에서 기간 검사). status = ACTIVE, withdrawnAt 초기화 */
    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.withdrawnAt = null;
    }

    /** 로그인 성공 시 호출하여 마지막 로그인 정보 갱신 */
    public void updateLastLogin(String provider) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginProvider = provider;
    }
}
