package com.newleaseonlife.SafeDogBe.domain.auth.service;

import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.NaverOAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.dto.info.OAuth2UserInfo;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.OAuthAccount;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.OAuthProvider;
import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.auth.repository.OAuthAccountRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.domain.user.service.UserService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. 지원하지 않는 플랫폼 사전 차단 (Early Return)
        // 두 번째 코드의 의도를 반영하여, 불필요한 네트워크 요청(super.loadUser) 전에 예외를 발생시킵니다.
        if (!"naver".equalsIgnoreCase(registrationId)) {
            log.warn("[CustomOAuth2UserService] 지원하지 않는 소셜 로그인 시도: {}", registrationId);
            throw new OAuth2AuthenticationException("UNSUPPORTED_PROVIDER|지원하지 않는 소셜 플랫폼입니다. ID: " + registrationId);
        }

        // 2. 부모 클래스(DefaultOAuth2UserService)를 통해 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 3. 벤더별 응답 속성 매핑 (현재는 네이버만 지원하지만 확장을 고려한 구조)
        OAuth2UserInfo oAuth2UserInfo = createOAuth2UserInfo(registrationId,
            oAuth2User.getAttributes());

        // 4. 유저 저장 또는 업데이트 (로그인 처리)
        User user = saveOrUpdate(oAuth2UserInfo);

        log.info("[CustomOAuth2UserService] loadUser 완료 userId={}, provider={}", user.getId(),
            registrationId);
        return new CustomPrincipal(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo createOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default ->
                throw new OAuth2AuthenticationException("UNSUPPORTED_PROVIDER|지원하지 않는 소셜 플랫폼입니다. ID: " + registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        OAuthProvider provider = OAuthProvider.valueOf(userInfo.getProvider().toUpperCase());

        // 1. 동일 provider + providerId로 기존 계정 조회 → 기존 회원이면 로그인
        User user = oauthAccountRepository.findByProviderAndProviderId(provider,
                userInfo.getProviderId())
            .map(OAuthAccount::getUser)
            .map(u -> {
                log.debug("[CustomOAuth2UserService] 기존 OAuth 계정 로그인 userId={}", u.getId());
                return u;
            })
            .orElseGet(() -> resolveNewSocialUser(userInfo, provider));

        // 2. 탈퇴 회원 처리: 소셜 재로그인 시 30일 내 자동 복구, 초과 시 차단
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            restoreWithdrawnOrThrow(user);
        }

        user.updateLastLogin(provider.name());
        return user;
    }

    /**
     * 탈퇴 소셜 회원 재로그인 처리. 소셜 유저는 비밀번호가 없어 {@code POST /api/users/restore} 이메일+비밀번호 경로를 쓸 수 없으므로 소셜 재로그인
     * 자체가 복구 수단이다.
     * <ul>
     *   <li>탈퇴 후 30일 이내 → 계정 자동 복구(ACTIVE 전환) 후 로그인 진행</li>
     *   <li>탈퇴 후 30일 초과 → {@code OAuth2AuthenticationException} 발생 → FE 안내</li>
     * </ul>
     */
    private void restoreWithdrawnOrThrow(User user) {
        LocalDateTime withdrawnAt = user.getWithdrawnAt();
        if (withdrawnAt == null) {
            log.warn("[CustomOAuth2UserService] 탈퇴 시각 누락 userId={}", user.getId());
            throw new OAuth2AuthenticationException("WITHDRAWN_ACCOUNT|탈퇴된 계정입니다. 고객센터에 문의해 주세요.");
        }

        long days = ChronoUnit.DAYS.between(withdrawnAt, LocalDateTime.now());
        if (days > UserService.RESTORE_AVAILABLE_DAYS) {
            log.warn("[CustomOAuth2UserService] 복구 기간 만료 userId={}, days={}", user.getId(), days);
            throw new OAuth2AuthenticationException(
                "ACCOUNT_RESTORE_EXPIRED|탈퇴 후 30일이 경과하여 계정을 복구할 수 없습니다."
            );
        }
        log.info("[CustomOAuth2UserService] 탈퇴 소셜 회원 자동 복구 userId={}, withdrawnAt={}", user.getId(),
            withdrawnAt);
        user.restore(); // 엔티티 내부에 상태 변경(ACTIVE) 로직이 있다고 가정
    }

    /**
     * 신규 소셜 로그인 처리. 이메일이 일치하는 기존 계정이 있으면 자동 연결(계정 통합). 없으면 신규 가입. 단, 동일 전화번호+이름 계정이 있으면 분기 오류 발생.
     */
    private User resolveNewSocialUser(OAuth2UserInfo userInfo, OAuthProvider provider) {
        String email = userInfo.getEmail();

        // 1. 이메일 기반 기존 계정 조회 → 같은 이메일이 있으면 소셜 계정 자동 연결
        if (email != null && !email.isBlank()) {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                User existing = existingByEmail.get();
                log.info("[CustomOAuth2UserService] 이메일 일치 → 기존 계정에 소셜 연결 userId={}, provider={}",
                    existing.getId(), provider);
                linkSocialAccount(existing, provider, userInfo.getProviderId());
                return existing;
            }
        }
        // 2. 14세 미만 가입 차단
        LocalDate birthDate = userInfo.getBirthDate();
        if (birthDate != null) {
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 14) {
                log.warn("[CustomOAuth2UserService] 신규 가입 차단 - 만 14세 미만 provider={}, birthDate={}", provider, birthDate);
                throw new OAuth2AuthenticationException("UNDER_AGE|만 14세 이상만 가입할 수 있습니다.");
            }
        }
// 3. 전화번호+이름으로 다른 소셜 계정이 있으면 예외 발생 (FE 연동용)
        String name = userInfo.getName();
        String phone = userInfo.getPhoneNumber();

        if (name != null && phone != null) {
            userRepository.findByPhoneAndName(phone, name).ifPresent(existing -> {
                if (existing.getProviderType() != null && !existing.getProviderType().name()
                    .equals(provider.name())) {
                    log.warn(
                        "[CustomOAuth2UserService] 동일 이름/번호 타 소셜 계정 감지 name={}, phone={}, existingProvider={}, newProvider={}",
                        name, phone, existing.getProviderType(), provider);
                    String existingDesc = existing.getProviderType()
                        .getDescription(); // ProviderType에 getDescription()이 있다고 가정
                    throw new OAuth2AuthenticationException(
                        "DUPLICATE_ACCOUNT:" + existing.getProviderType().name()
                            + "|" + existingDesc + "로 이미 가입된 계정이 있어요."
                    );
                }
            });
        }

        // 4. 신규 가입
        return createUserAndOAuthAccount(userInfo, provider, birthDate, email, phone);
    }

    /**
     * 기존 User에 새 소셜 OAuthAccount 연결 (계정 통합)
     */
    private void linkSocialAccount(User user, OAuthProvider provider, String providerId) {
        OAuthAccount newAccount = OAuthAccount.builder()
            .user(user)
            .provider(provider)
            .providerId(providerId)
            .build();
        oauthAccountRepository.save(newAccount);
    }

    private User createUserAndOAuthAccount(OAuth2UserInfo userInfo, OAuthProvider provider,
        LocalDate birthDate, String email, String phone) {

        if (email == null || email.isBlank()) {
            email = userInfo.getProvider() + "_" + userInfo.getProviderId() + "@safedog.oauth";
        }

        String nickname = (userInfo.getName() != null ? userInfo.getName() : "user")
            + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);

        User newUser = User.builder()
            .email(email)
            .nickname(nickname)
            .name(userInfo.getName())
            .phone(phone)
            .birthDate(birthDate)
            .status(UserStatus.PENDING) // 소셜 가입 직후 추가 정보 입력 대기 상태로 가정
            .providerType(ProviderType.valueOf(userInfo.getProvider().toUpperCase()))
            .build();
        userRepository.save(newUser);

        OAuthAccount newAccount = OAuthAccount.builder()
            .user(newUser)
            .provider(provider)
            .providerId(userInfo.getProviderId())
            .build();
        oauthAccountRepository.save(newAccount);

        return newUser;
    }
}
