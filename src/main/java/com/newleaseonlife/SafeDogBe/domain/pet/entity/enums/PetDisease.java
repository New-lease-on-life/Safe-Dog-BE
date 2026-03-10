package com.newleaseonlife.SafeDogBe.domain.pet.entity.enums;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 반려동물 질병 유형. 등록 시 질병별 기본 체크리스트 템플릿 자동 생성에 사용.
 * defaultTemplates()로 해당 질병에 맞는 CareTemplate 정보를 반환한다.
 */
@Getter
@RequiredArgsConstructor
public enum PetDisease {

    DIABETES("당뇨",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "당뇨약 복용", "처방에 따라 인슐린 또는 경구약 복용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.MEAL,    "식이 관리",   "당뇨 맞춤 식단 급여 (고단백·저탄수화물)", RepeatCycle.DAILY)
            )),

    HEART_DISEASE("심장병",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "심장약 복용",  "처방에 따라 심장 관련 약 복용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.HOSPITAL, "심장 정기검진", "수의사 지시에 따른 정기 검진", RepeatCycle.MONTHLY)
            )),

    ARTHRITIS("관절염",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "관절약/보조제 복용", "처방 또는 보조제 급여", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.WALK,     "짧은 산책",          "과격하지 않은 가벼운 산책 (15~20분)", RepeatCycle.DAILY)
            )),

    KIDNEY_DISEASE("신장병",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "신장약 복용",  "처방에 따라 신장 관련 약 복용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.MEAL,     "신장 식이 관리", "저단백·저인 맞춤 식단 급여", RepeatCycle.DAILY)
            )),

    ALLERGY("알레르기",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "항히스타민/처방약 복용", "수의사 처방 약 복용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.BATH,     "정기 목욕·피부 관리",   "알레르겐 제거를 위한 주기적 목욕", RepeatCycle.WEEKLY)
            )),

    SKIN_DISEASE("피부병",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "피부약/연고 도포",  "처방에 따라 피부약·연고 적용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.BATH,     "약용 샴푸 목욕",    "수의사 권장 약용 샴푸로 목욕", RepeatCycle.WEEKLY)
            )),

    CANCER("암",
            List.of(
                    new DefaultTemplate(CareType.MEDICINE, "항암/처방약 복용",  "처방에 따라 항암제·보조약 복용", RepeatCycle.DAILY),
                    new DefaultTemplate(CareType.HOSPITAL, "항암 정기 방문",    "치료 계획에 따른 병원 방문", RepeatCycle.MONTHLY)
            ));

    /** 사용자 노출용 한국어 표시명 */
    private final String description;

    /** 해당 질병에 맞는 기본 케어 템플릿 목록 */
    private final List<DefaultTemplate> defaultTemplates;

    /**
     * 자동 생성 CareTemplate 정보를 담는 값 객체.
     * Pet 등록 시 이 정보를 기반으로 CareTemplate 엔티티를 생성한다.
     */
    public record DefaultTemplate(CareType careType, String title, String content, RepeatCycle repeatCycle) {
    }
}
