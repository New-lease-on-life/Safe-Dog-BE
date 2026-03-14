package com.newleaseonlife.SafeDogBe.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 약관 동의를 완료하지 않은 PENDING 유저의 핵심 API 접근을 차단하는 보안 필터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingUserCheckFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  // PENDING 유저라도 반드시 접근해야 하는 경로 (가입 완료 API, 약관 조회 등)
  private static final List<String> WHITE_LIST = Arrays.asList(
      "/api/auth/**",
      "/api/terms/**",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/actuator/**"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    // 1. 화이트리스트에 포함된 경로는 PENDING 검사 없이 통과 (ex. /api/auth/social-signup-complete)
    if (WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 2. 인증된 사용자 중 상태가 PENDING인 경우 차단
    if (authentication != null && authentication.getPrincipal() instanceof CustomPrincipal principal) {

      // 🌟 핵심 변경: isOnboardingCompleted가 아닌 Status를 검사
      if (principal.getUser().getStatus() == UserStatus.PENDING) {
        log.warn("[PendingUserCheckFilter] 약관 미동의 유저의 API 접근 차단 userId={}, uri={}",
            principal.getUser().getId(), requestURI);

        sendErrorResponse(response);
        return; // 필터 체인 중단 (Controller로 넘어가지 않음)
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendErrorResponse(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // 프론트엔드가 이 에러코드를 받으면 약관 동의 화면으로 튕겨냄
    String json = objectMapper.writeValueAsString(
        java.util.Map.of("code", "TERMS_AGREEMENT_REQUIRED", "message", "약관 동의 및 가입 완료가 필요합니다.")
    );
    response.getWriter().write(json);
  }
}