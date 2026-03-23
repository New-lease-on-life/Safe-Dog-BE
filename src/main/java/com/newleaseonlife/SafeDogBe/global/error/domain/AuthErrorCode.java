package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.http.HttpStatus;

/**
 * 인증 도메인 오류 코드 (이메일 중복, 로그인 실패, 리프레시 토큰 무효).
 */
@AllArgsConstructor
@Getter
public enum AuthErrorCode implements ApiCode {

  EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, 400, "이미 존재하는 이메일입니다."),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 401, "아이디 또는 비밀번호가 잘못되었습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 401, "유효하지 않은 리프레시 토큰입니다."),

  /**
   * 만 14세 미만 가입 차단 (기존 UNDER_AGE 유지)
   */
  UNDER_AGE(HttpStatus.BAD_REQUEST, 400, "만 14세 이상만 가입할 수 있습니다."),

  /**
   * ✅ 추가: 필수 약관 동의 누락
   */
  REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, 400, "서비스 이용을 위해 필수 정보 제공이 필요합니다."),
  BIRTHDATE_REQUIRED(HttpStatus.BAD_REQUEST, 400, "생년월일 입력은 필수입니다."),

  /**
   * 이미 연결된 소셜 계정
   */
  SOCIAL_ALREADY_LINKED(HttpStatus.CONFLICT, 409, "이미 연결된 소셜 계정입니다."),

  /**
   * 이미 온보딩을 완료한 회원
   */
  ALREADY_ACTIVE_USER(HttpStatus.CONFLICT, 409, "이미 온보딩을 완료한 계정입니다.");
  private final HttpStatus httpStatus;
  private final Integer code;
  private final String message;

  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }
}
