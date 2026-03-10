package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

@SuppressWarnings("unchecked")
public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> response;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
        if (this.response == null) {
            // Spring Security OAuth2 흐름 내에서 올바르게 처리되도록 OAuth2AuthenticationException 사용
            throw new OAuth2AuthenticationException("Naver 응답에 response 데이터가 없습니다.");
        }
    }

    @Override
    public String getProviderId() {
        return (String) response.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) response.get("email");
    }

    @Override
    public String getName() {
        String name = (String) response.get("name");
        // Naver가 name을 제공하지 않는 경우 providerId 기반 닉네임으로 폴백
        return name != null ? name : getProviderId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
