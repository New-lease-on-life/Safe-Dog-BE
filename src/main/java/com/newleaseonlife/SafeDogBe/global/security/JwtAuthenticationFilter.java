package com.newleaseonlife.SafeDogBe.global.security;

import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Access Token 기반 인증 필터.
 * 쿠키(access_token) 또는 Authorization Bearer 헤더에서 토큰을 추출하고,
 * 유효하며 해당 유저가 ACTIVE 상태일 때만 SecurityContext에 인증 정보를 설정한다.
 * 탈퇴(WITHDRAWN)·휴면(INACTIVE) 유저는 인증되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /** 쿠키 또는 헤더에서 토큰 추출 → 유효 시 ACTIVE 유저만 인증 설정 → 체인 계속 */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateAccessToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromAccessToken(token);
            userRepository.findById(userId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING)
                    .ifPresent(user -> {
                        CustomPrincipal principal = new CustomPrincipal(user, Collections.emptyMap());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("[JwtAuthenticationFilter] 인증 성공 userId={}", userId);
                    });
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 Access Token 추출. 쿠키(웹) 우선, 없으면 Authorization Bearer 헤더(모바일) 사용.
     */
    private String resolveToken(HttpServletRequest request) {
        String fromCookie = CookieUtils.readCookie(request, CookieUtils.ACCESS_TOKEN_COOKIE);
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie;
        }
        String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(BEARER_PREFIX)) {
            return bearer.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
