package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/** 수정_3월18일
 * 반려동물 질병 유형 (기획서 기준 6종).
 * 등록 시 질병별 기본 체크리스트 자동 생성에 사용.
 * defaultCheckItems()로 해당 질병의 기본 체크 항목 문자열 목록을 반환한다.
 *
 * ✅ 변경: DIABETES, ALLERGY, SKIN_DISEASE 제거
 * ✅ 변경: EYE_DISEASE, CUSHING 추가
 * ✅ 변경: 기본 체크리스트 내용을 기획서 명세 기준으로 전면 교체
 * ✅ 알레르기는 Pet.hasAllergy + Pet.allergyDescription 필드로 분리
 */
@Getter
@RequiredArgsConstructor
public enum PetDisease {

  HEART_DISEASE("심장병", List.of(
      "수면 중 안정 시 호흡수(SRR) 측정 및 기록",
      "점막(혀/잇몸) 색깔 및 기침 양상 관찰",
      "날씨 확인 후 산책",
      "식후 즉시 활동 금지 및 휴식 유도"
  )),

  KIDNEY_DISEASE("신장질환", List.of(
      "피부 탄력(탈수) 테스트",
      "구강 및 잇몸 상태(악취, 궤양) 관찰",
      "물그릇 세척 및 신선한 물 교체",
      "음수 유도(입 가까이 물 대주기)"
  )),

  CANCER("암", List.of(
      "환부 소독 및 드레싱 교체",
      "종양 크기 변화 및 새 멍울 촉진",
      "통증 신호 및 컨디션(식욕 등) 모니터링",
      "체온 측정 및 미열 확인",
      "반려동물과 5분 놀이하기"
  )),

  EYE_DISEASE("안과질환", List.of(
      "안구 세정 및 눈 주변 분비물 닦기",
      "인공눈물 점안",
      "안구 통증, 충혈, 혼탁도 관찰",
      "점안 후 눈 비비지 않도록 제지"
  )),

  CUSHING("쿠싱 증후군", List.of(
      "복부 팽만(올챙이 배) 상태 확인",
      "피부 발진, 각질 및 피부 얇아짐 관찰",
      "음수량 및 배뇨 횟수 관찰",
      "근육 감소 및 보행 상태 확인",
      "피부 접히는 부위 청결 관리 및 보습제 도포"
  )),

  ARTHRITIS("관절염", List.of(
      "기상 직후 보행 및 절뚝임 상태 관찰",
      "아픈 관절 부위 온찜질 및 마사지 수행",
      "수동 관절 운동(PROM) 수행",
      "발바닥 털 정리 및 미끄럼 방지 점검"
  ));

  /** 사용자 노출용 한국어 표시명 */
  private final String description;

  /**
   * 해당 질병의 기본 케어 체크리스트 항목 문자열 목록.
   * Pet 등록 시 이 목록을 기반으로 CareTemplate 엔티티를 생성한다.
   */
  private final List<String> defaultCheckItems;
}