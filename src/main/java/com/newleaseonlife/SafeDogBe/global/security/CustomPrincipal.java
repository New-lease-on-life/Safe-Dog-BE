package com.newleaseonlife.SafeDogBe.global.security;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring Security 인증 주체. OAuth2User 구현체로, 도메인 User와 OAuth2 attributes를 함께 보관한다.
 * JWT 필터·OAuth2 성공 핸들러에서 사용. getAuthorities()는 user.role 기반 ROLE_* 반환.
 */
public class CustomPrincipal implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    /** @param attributes null이면 빈 Map으로 저장 */
    public CustomPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /** OAuth2User 식별자. 이 프로젝트에서는 이메일 사용. */
    @Override
    public String getName() {
        return user.getEmail();
    }
}
