package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.time.LocalDate;
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
    public LocalDate getBirthDate() {
        String birthyear = (String) response.get("birthyear");
        String birthday = (String) response.get("birthday");
        if (birthyear == null || birthday == null) return null;
        try {
            int y = Integer.parseInt(birthyear);
            String[] parts = birthday.split("-");
            if (parts.length != 2) return null;
            int m = Integer.parseInt(parts[0]);
            int d = Integer.parseInt(parts[1]);
            return LocalDate.of(y, m, d);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getPhoneNumber() {
        // 네이버는 전화번호를 "mobile" 이라는 키값으로 제공합니다.
        if (response == null) return null;
        return (String) response.get("mobile");
    }
}
