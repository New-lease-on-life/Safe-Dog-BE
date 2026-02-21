package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.util.Map;

@SuppressWarnings("unchecked")
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getName() {
        if (profile != null && profile.get("nickname") != null) {
            return (String) profile.get("nickname");
        }
        return "Unknown_Kakao";
    }

    @Override
    public Integer getAge() {
        if (kakaoAccount == null) {
            return null;
        }
        String ageRange = (String) kakaoAccount.get("age_range");
        if (ageRange == null) {
            return null;
        }
        try {
            return Integer.parseInt(ageRange.split("~")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
