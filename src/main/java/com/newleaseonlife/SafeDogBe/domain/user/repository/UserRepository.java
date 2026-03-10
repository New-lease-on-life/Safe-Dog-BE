package com.newleaseonlife.SafeDogBe.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

/**
 * 회원 영속화. 이메일/닉네임/전화번호+이름 기준 조회·중복 검사 제공.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일로 회원 조회 (로그인·소셜 연동 시 사용) */
    Optional<User> findByEmail(String email);

    /** 이메일 존재 여부 */
    boolean existsByEmail(String email);

    /** 닉네임 중복 여부. 프로필 수정 시 검사 */
    boolean existsByNickname(String nickname);

    /** 동일 전화번호+이름 조합 존재 여부. 본인인증 중복 가입 방지용 */
    boolean existsByPhoneAndName(String phone, String name);

    /** 전화번호+이름으로 회원 조회. 중복 응답에 기존 소셜 타입 포함 시 사용 */
    java.util.Optional<User> findByPhoneAndName(String phone, String name);

    /** 이름으로 첫 번째 회원 조회. 소셜 로그인 시 동일 이름 기존 계정 감지용 (정확도 낮아 보조 수단으로만 사용) */
    java.util.Optional<User> findFirstByName(String name);
}
