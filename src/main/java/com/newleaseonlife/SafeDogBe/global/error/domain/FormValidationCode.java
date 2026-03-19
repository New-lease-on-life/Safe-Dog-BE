package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 3월 19일 수정
 * 공통 에러 코드. (deprecation 경고: Spring HttpStatus 등 외부 API 사용으로 인한 ADVICE 수준) */
@SuppressWarnings("deprecation")
@AllArgsConstructor
@Getter
public enum FormValidationCode implements ApiCode {
  // --- 폼 검증 관련 메시지 (반려동물 정보) ---
  PET_NAME_REQUIRED(HttpStatus.BAD_REQUEST, 400, "반려견 이름은 필수 항목입니다."),
  PET_BIRTH_REQUIRED(HttpStatus.BAD_REQUEST, 400, "생년월일은 필수 항목입니다."),
  PET_GENDER_REQUIRED(HttpStatus.BAD_REQUEST, 400, "성별은 필수 항목입니다."),

  // --- 폼 검증 관련 메시지 (체크리스트) ---
  CHECKLIST_TITLE_REQUIRED(HttpStatus.BAD_REQUEST, 400, "체크리스트 제목은 필수 항목입니다."),
  CARE_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, 400, "케어 항목은 최소 하나 이상 필요합니다."),

  // --- 폼 검증 관련 메시지 (반려노트) ---
  PETNOTE_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, 400, "반려노트 내용은 필수 항목입니다."),

  // --- 폼 검증 관련 메시지 (사용자 정보) ---
  USER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, 400, "이름은 필수 항목입니다."),
  USER_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, 400, "이메일은 필수 항목입니다.");

  private final HttpStatus httpStatus;
  private final Integer code;
  private final String message;

  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }
}
