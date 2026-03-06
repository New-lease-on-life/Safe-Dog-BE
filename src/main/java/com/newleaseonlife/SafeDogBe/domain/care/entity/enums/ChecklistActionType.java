package com.newleaseonlife.SafeDogBe.domain.care.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChecklistActionType {
  CHECK("완료 처리"),
  UNCHECK("완료 취소"),
  UPDATE_MEMO("메모/내용 수정");

  private final String description;
}