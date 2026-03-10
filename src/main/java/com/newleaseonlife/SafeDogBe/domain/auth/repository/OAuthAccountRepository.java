package com.newleaseonlife.SafeDogBe.domain.auth.repository;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.OAuthAccount;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.OAuthProvider;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** OAuth 계정 연동(oauth_accounts) 영속화 */
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    /** provider + providerId로 조회. 소셜 로그인 시 기존 계정 확인용 */
    Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
