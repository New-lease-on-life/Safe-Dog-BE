package com.newleaseonlife.SafeDogBe.global.config;

import com.newleaseonlife.SafeDogBe.domain.auth.service.CustomOAuth2UserService;
import com.newleaseonlife.SafeDogBe.global.security.JwtAuthenticationFilter;
import com.newleaseonlife.SafeDogBe.global.security.OAuth2LoginFailureHandler;
import com.newleaseonlife.SafeDogBe.global.security.OAuth2LoginSuccessHandler;

import com.newleaseonlife.SafeDogBe.global.security.PendingUserCheckFilter;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 전역 설정.
 * JWT 필터, OAuth2 로그인, CORS, 인가 경로(permitAll/authenticated)를 구성한다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final PendingUserCheckFilter pendingUserCheckFilter; // ✅ 필터 이름 변경 적용

    /** 허용 CORS Origin 목록. 환경별 application.yaml의 app.cors.allowed-origins에서 주입. */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /** CORS 설정. 쿠키 인증 허용, app.cors.allowed-origins 기반 Origin 허용. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.addAllowedOriginPattern("*"); //테스트용 추후에 지우기
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Stateless JWT + OAuth2 로그인, 인가 규칙, JWT 필터 위치 정의. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/refresh", "/api/auth/logout",
                                "/api/auth/check-duplicate", "/api/auth/devices/**",
                                "/api/auth/test-login", //테스트 계정 로그인용
                                "/login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/terms", "/api/users/check-nickname").permitAll()
                        .requestMatchers("/api/invites/*/join").authenticated() // 참여는 인증 필요
                        .requestMatchers("/api/invites/**").permitAll() // 초대 정보 조회는 비인증 허용
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll() // 헬스 체크, 프로메테우스
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 1번: JWT 토큰으로 인증 (이 필터는 PENDING과 ACTIVE 유저 모두 통과시킴)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 2번: PENDING 유저가 메인 API에 접근하는지 감시 및 차단
                .addFilterAfter(pendingUserCheckFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
