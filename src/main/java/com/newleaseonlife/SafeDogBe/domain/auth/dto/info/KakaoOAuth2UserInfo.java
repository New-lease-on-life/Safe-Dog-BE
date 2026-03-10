package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.time.LocalDate;
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
    public LocalDate getBirthDate() {
        if (kakaoAccount == null) return null;
        String birthyear = (String) kakaoAccount.get("birthyear");
        String birthday = (String) kakaoAccount.get("birthday");
        if (birthyear == null || birthday == null || birthday.length() != 4) return null;
        try {
            int y = Integer.parseInt(birthyear);
            int m = Integer.parseInt(birthday.substring(0, 2));
            int d = Integer.parseInt(birthday.substring(2, 4));
            return LocalDate.of(y, m, d);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
