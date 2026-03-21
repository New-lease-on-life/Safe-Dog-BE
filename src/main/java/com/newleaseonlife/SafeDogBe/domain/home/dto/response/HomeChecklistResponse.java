package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 홈 체크리스트 탭 구분 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeChecklistResponse {

    /** 기본 케어 탭 (DISEASE_CARE 제외한 모든 케어 타입) */
    private List<HomeChecklistCategoryResponse> basicCare;

    /** 질병 케어 탭 (DISEASE_CARE 전용) */
    private List<HomeChecklistCategoryResponse> diseaseCare;
}
