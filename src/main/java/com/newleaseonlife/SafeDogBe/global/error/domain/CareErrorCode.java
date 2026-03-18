package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 케어 도메인 오류 코드 */
@AllArgsConstructor
@Getter
public enum CareErrorCode implements ApiCode {

  CARE_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 케어 템플릿입니다."),
  CARE_TEMPLATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "해당 케어 템플릿에 접근 권한이 없습니다."),
  CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 체크리스트입니다."),
  CHECKLIST_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, 400, "이미 완료 처리된 항목입니다."),
  CHECKLIST_NOT_COMPLETED(HttpStatus.BAD_REQUEST, 400, "아직 완료되지 않은 항목입니다."),
  CHECKLIST_DATE_NOT_TODAY(HttpStatus.BAD_REQUEST, 400, "오늘 날짜의 체크리스트만 수정할 수 있습니다."),
  CHECKLIST_VERSION_CONFLICT(HttpStatus.CONFLICT, 409, "다른 보호자가 먼저 저장했습니다. 최신 내용을 확인해주세요."),
  EXCRETION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 배변 기록입니다."),
  WEIGHT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 체중 기록입니다."),
  EXCRETION_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, 400, "배변 상태(정상/이상) 값은 필수입니다.");

  private final HttpStatus httpStatus;
  private final Integer code;
  private final String message;

  @Override
  public HttpStatus getHttpStatus() { return this.httpStatus; }
}