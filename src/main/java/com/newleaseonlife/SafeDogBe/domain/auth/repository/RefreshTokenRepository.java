package com.newleaseonlife.SafeDogBe.domain.auth.repository;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Refresh Token 영속화. 토큰 문자열·userId 기준 조회·삭제 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** 토큰 문자열로 조회. 갱신·로그아웃 시 사용 */
    Optional<RefreshToken> findByToken(String token);

    /** userId의 기존 토큰 삭제. 새 토큰 발급 전 호출 */
    void deleteByUserId(Long userId);
}
