package com.newleaseonlife.SafeDogBe.domain.auth.dto.info;

import java.util.Map;

public interface OAuth2UserInfo {

    String getProviderId();

    String getProvider();

    String getEmail();

    String getName();

    Integer getAge();

    Map<String, Object> getAttributes();
}
