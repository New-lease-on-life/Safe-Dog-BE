package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 홈 반려동물 목록 조회 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomePetListResponse {

    /** 마지막으로 선택한 반려동물 ID. null이면 미선택 */
    private Long lastSelectedPetId;

    /** 직접 등록 + 공유받은 반려동물 목록 (등록일 오름차순) */
    private List<HomePetSummaryResponse> pets;
}
