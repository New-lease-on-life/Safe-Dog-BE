package com.newleaseonlife.SafeDogBe.domain.petnote.dto.response;

import com.newleaseonlife.SafeDogBe.domain.petnote.entity.ChecklistMemo;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChecklistMemoResponse {
  private Long memoId;
  private String content;
  private Long authorId;
  private String authorNickname;
  private String authorProfileImageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ChecklistMemoResponse from(ChecklistMemo memo) {
    return ChecklistMemoResponse.builder()
        .memoId(memo.getId())
        .content(memo.getContent())
        .authorId(memo.getAuthor().getId())
        .authorNickname(memo.getAuthor().getNickname())
        .authorProfileImageUrl(memo.getAuthor().getProfileImageUrl())
        .createdAt(memo.getCreatedAt())
        .updatedAt(memo.getUpdatedAt())
        .build();
  }
}
