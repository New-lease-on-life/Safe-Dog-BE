package com.newleaseonlife.SafeDogBe.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "필수 입력 값을 작성해주세요."),
  FILE_SIZE_EXCEED(HttpStatus.PAYLOAD_TOO_LARGE, "C002", "10MB 이하의 이미지만 등록 가능합니다."),

  // Auth & Member
  DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "M001", "이미 해당 정보로 가입된 계정이 있어요."),
  DELEGATION_REQUIRED(HttpStatus.BAD_REQUEST, "M002", "모든 강아지의 정보가 사라지니, 관리자 위임 후 탈퇴를 진행해주세요."),

  // Pet & Care
  PET_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "반려동물 정보를 찾을 수 없습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "P002", "해당 반려동물에 대한 접근 권한이 없습니다."),

  // Concurrency (동시성)
  OPTIMISTIC_LOCK_CONFLICT(HttpStatus.CONFLICT, "L001",
      "동시에 저장 요청이 발생했습니다. 최신 내용으로 다시 불러온 후 변경사항을 확인해주세요.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}