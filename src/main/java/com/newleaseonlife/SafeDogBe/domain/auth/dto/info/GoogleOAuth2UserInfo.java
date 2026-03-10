package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.time.LocalDate;
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
    public LocalDate getBirthDate() {
        // Google 표준 OAuth2(email, profile scope)는 생년월일을 제공하지 않음.
        // 생년월일이 필요하면 Google People API(별도 scope: https://www.googleapis.com/auth/user.birthday.read) 연동 필요.
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
