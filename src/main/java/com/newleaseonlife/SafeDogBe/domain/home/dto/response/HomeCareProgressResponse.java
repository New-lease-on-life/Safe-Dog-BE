package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 홈 화면 케어 진행률 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeCareProgressResponse {

    /** 오늘 요청된 전체 케어 수 */
    private long totalCount;

    /** 완료된 케어 수 */
    private long completedCount;
}
