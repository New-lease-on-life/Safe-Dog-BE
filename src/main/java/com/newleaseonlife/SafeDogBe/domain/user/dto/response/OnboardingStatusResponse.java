package com.newleaseonlife.SafeDogBe.domain.user.dto.response;

/**
 * 온보딩 노출 여부 응답.
 * FE는 shouldShowOnboarding == true이면 온보딩 화면을 표시, false면 홈으로 이동.
 *
 * @param shouldShowOnboarding 온보딩 화면 노출 여부. 최초 가입 후 온보딩 미완료 시 true
 */
public record OnboardingStatusResponse(boolean shouldShowOnboarding) {
}
