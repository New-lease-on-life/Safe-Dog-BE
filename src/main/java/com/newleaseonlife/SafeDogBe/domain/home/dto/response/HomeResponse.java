package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 홈 화면 통합 데이터 응답 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {

    /** 반려동물 미등록 여부. true이면 FE에서 등록 유도 화면으로 분기 */
    private boolean hasPets;

    /** 선택된 반려동물 프로필 */
    private HomePetProfileResponse petProfile;

    /** 오늘 케어 진행률 */
    private HomeCareProgressResponse careProgress;

    /** 최신 메모 목록 (최신순, 최대 20건) */
    private List<HomeNoteResponse> notes;

    /** 오늘 날짜 기준 체크리스트 등록 여부 */
    private boolean hasChecklist;
}
