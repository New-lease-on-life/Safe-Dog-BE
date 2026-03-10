package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        String name = (String) attributes.get("name");
        // Google이 name을 제공하지 않는 경우 sub(providerId) 기반 닉네임으로 폴백
        return name != null ? name : getProviderId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
