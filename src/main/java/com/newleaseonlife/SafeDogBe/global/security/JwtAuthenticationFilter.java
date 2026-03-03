package com.newleaseonlife.SafeDogBe.global.security;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateAccessToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromAccessToken(token);
            userRepository.findById(userId).ifPresent(user -> {
                CustomPrincipal principal = new CustomPrincipal(user, Collections.emptyMap());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[JwtAuthenticationFilter] 인증 성공 userId={}", userId);
            });
        }

        filterChain.doFilter(request, response);
    }

    // 1. 쿠키 우선 (웹 브라우저), 2. Authorization 헤더 폴백 (모바일 앱)
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
