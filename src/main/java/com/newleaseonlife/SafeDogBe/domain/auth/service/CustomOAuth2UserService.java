package com.newleaseonlife.SafeDogBe.domain.auth.service;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.GoogleOAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.KakaoOAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.NaverOAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.OAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.OAuthAccount;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.OAuthProvider;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.OAuthAccountRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("[CustomOAuth2UserService] loadUser registrationId={}", registrationId);

        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2UserInfo = createOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        User user = saveOrUpdate(oAuth2UserInfo);

        log.info("[CustomOAuth2UserService] loadUser 완료 userId={}, provider={}", user.getId(), registrationId);
        return new CustomPrincipal(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo createOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다. ID: " + registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        OAuthProvider provider = OAuthProvider.valueOf(userInfo.getProvider().toUpperCase());

        User user = oauthAccountRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .map(OAuthAccount::getUser)
                .map(u -> {
                    log.debug("[CustomOAuth2UserService] 기존 OAuth 계정 로그인 userId={}", u.getId());
                    return u;
                })
                .orElseGet(() -> createUserAndOAuthAccount(userInfo, provider));

        user.updateLastLogin(provider.name());
        return user;
    }

    private User createUserAndOAuthAccount(OAuth2UserInfo userInfo, OAuthProvider provider) {
        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            email = userInfo.getProvider() + "_" + userInfo.getProviderId() + "@safedog.oauth";
        }
        String nickname = userInfo.getName() + "_" + UUID.randomUUID().toString().substring(0, 6);

        User newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .name(userInfo.getName())
                .status(UserStatus.ACTIVE)
                .providerType(ProviderType.valueOf(userInfo.getProvider().toUpperCase()))
                .build();
        userRepository.save(newUser);

        OAuthAccount newAccount = OAuthAccount.builder()
                .user(newUser)
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .createdAt(LocalDateTime.now())
                .build();
        oauthAccountRepository.save(newAccount);
        log.info("[CustomOAuth2UserService] 신규 OAuth 가입 완료 userId={}, provider={}", newUser.getId(), provider);

        return newUser;
    }
}
