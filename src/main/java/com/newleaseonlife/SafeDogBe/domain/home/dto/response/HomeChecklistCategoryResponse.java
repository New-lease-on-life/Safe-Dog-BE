package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 케어 타입 카테고리 단위 체크리스트 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeChecklistCategoryResponse {

    /** CareType enum 값 (MEAL, WALK 등) */
    private String careType;

    /** 케어 타입 한글 설명 */
    private String careTypeDescription;

    /** 해당 카테고리의 체크리스트 항목 목록 */
    private List<HomeChecklistItemResponse> items;
}
