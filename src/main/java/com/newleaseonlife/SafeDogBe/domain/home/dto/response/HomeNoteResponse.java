package com.newleaseonlife.SafeDogBe.domain.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 홈 화면 메모 목록 항목 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeNoteResponse {

    private Long id;
    private String content;

    /** 작성자 ID */
    private Long writtenByUserId;

    /** 작성자 닉네임 */
    private String writtenByNickname;

    /** 작성자 프로필 이미지 URL */
    private String writtenByProfileImageUrl;

    /** 작성자의 반려동물에 대한 역할 (OWNER / CAREGIVER) */
    private String writtenByRole;

    /** 전송(생성) 시각 */
    private LocalDateTime sentAt;

    /** 마지막 로그인 이후 새로 작성된 메모 여부 */
    private boolean isNew;

    /** 연결된 체크리스트 ID (optional) */
    private Long linkedChecklistId;
}
