package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.time.LocalDate;
import java.util.Map;

public interface OAuth2UserInfo {

    String getProviderId();

    String getProvider();

    String getEmail();

    String getName();

    String getPhoneNumber();

    /** 소셜에서 제공하는 생년월일. 없으면 null. 14세 미만 가입 차단 검증에 사용 */
    LocalDate getBirthDate();

    Map<String, Object> getAttributes();
}
