package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 홈 체크리스트 개별 항목 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeChecklistItemResponse {

    private Long id;
    private String title;
    private String content;
    private boolean isCompleted;

    /** 케어 템플릿이 있으면 true (스케줄러에 의해 요청된 항목) */
    private boolean isRequested;

    /** 완료 처리 시각. 완료 전이면 null */
    private LocalDateTime completedAt;

    /** 완료자 프로필 이미지 URL */
    private String completedByProfileImageUrl;

    /** 완료자 닉네임 */
    private String completedByNickname;

    /** 낙관적 락 버전 */
    private Integer version;
}
