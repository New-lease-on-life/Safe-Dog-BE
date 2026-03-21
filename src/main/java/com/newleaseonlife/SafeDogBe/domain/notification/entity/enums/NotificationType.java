package com.newleaseonlife.SafeDogBe.domain.notification.entity.enums;

import lombok.Getter;

/**
 * 알림 타입 열거형
 * 2026.03 기획서 기반
 */
@Getter
public enum NotificationType {
  CARE_REQUEST("케어 요청", "반려동물 케어를 요청했어요"),
  CARE_COMPLETED("케어 완료", "반려동물 케어가 완료되었어요"),
  PET_ADDED("반려동물 추가", "새로운 반려동물이 추가되었어요"),
  PET_UPDATED("반려동물 정보 수정", "반려동물 정보가 수정되었어요"),
  PET_DELETED("반려동물 삭제", "반려동물이 삭제되었어요"),
  PET_INVITE("반려동물 공유 초대", "반려동물 관리에 초대받았어요"),
  GUARDIAN_ADDED("보호자 추가", "새로운 보호자가 추가되었어요"),
  ADMIN_CHANGED("관리자 변경", "관리자 권한이 변경되었어요"),
  PETNOTE_CREATED("반려노트 등록", "새로운 반려노트가 등록되었어요"),
  PETNOTE_UPDATED("반려노트 수정", "반려노트가 수정되었어요"),
  MEMO_ADDED("메모 추가", "새로운 메모가 추가되었어요"),
  MARKETING("마케팅 알림", "마케팅 정보 알림입니다."),
  SYSTEM("시스템 알림", "시스템 알림입니다");

  private final String label;
  private final String defaultMessage;

  NotificationType(String label, String defaultMessage) {
    this.label = label;
    this.defaultMessage = defaultMessage;
  }
}