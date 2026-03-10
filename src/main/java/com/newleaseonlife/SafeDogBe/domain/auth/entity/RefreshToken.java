package com.newleaseonlife.SafeDogBe.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 저장 엔티티. 로그인/갱신 시 1건만 유지(기존 삭제 후 재저장).
 * expiredAt 기준으로 만료 여부를 판단하며, JWT 서명 검증과 별도로 DB 만료 확인 가능.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 토큰 소유자 회원 ID */
    @Column(nullable = false)
    private Long userId;

    /** Refresh Token 문자열 */
    @Column(nullable = false, length = 512)
    private String token;

    /** 만료 일시. isExpired() 로 확인 */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /** 발급 일시 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 현재 시각 기준 만료 여부 */
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiredAt);
    }
}
