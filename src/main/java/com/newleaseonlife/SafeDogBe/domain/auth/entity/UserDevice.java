package com.newleaseonlife.SafeDogBe.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/**
 * 기기별 최근 로그인 소셜 타입 저장.
 * FE가 deviceId(앱 UUID 또는 브라우저 fingerprint)를 전달하면 마지막 사용 소셜을 기록.
 * 다음 접속 시 FE 로그인 화면에 "카카오로 로그인" 툴팁 등에 사용.
 */
@Entity
@Table(
        name = "user_devices",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_devices_device_id", columnNames = {"device_id"}),
        indexes = @Index(name = "idx_user_devices_device_id", columnList = "device_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FE에서 생성·관리하는 기기 고유 식별자.
     * 앱: 설치 시 생성 UUID / 웹: localStorage UUID
     */
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    /**
     * 해당 기기에서 마지막으로 사용한 소셜 로그인 타입.
     * LOCAL / GOOGLE / NAVER / KAKAO
     */
    @Column(nullable = false, length = 20)
    private String lastLoginProvider;

    /** 마지막 로그인 일시 */
    @Column(nullable = false)
    private LocalDateTime lastLoginAt;

    @Builder
    public UserDevice(String deviceId, String lastLoginProvider) {
        this.deviceId = deviceId;
        this.lastLoginProvider = lastLoginProvider;
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 로그인 시 소셜 타입·시각 갱신 */
    public void updateLoginInfo(String provider) {
        this.lastLoginProvider = provider;
        this.lastLoginAt = LocalDateTime.now();
    }
}
