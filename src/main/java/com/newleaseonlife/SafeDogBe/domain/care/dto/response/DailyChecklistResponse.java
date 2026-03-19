// domain/care/dto/response/DailyChecklistResponse.java
package com.newleaseonlife.SafeDogBe.domain.care.dto.response;

import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 3월 18일 수정
 * 일일 체크리스트 응답 DTO.
 * ✅ 추가: completedByProfileImageUrl (기획서 3: 체크 완료 시 보호자 프로필 이미지+닉네임 노출)
 * ✅ 추가: updatedAt (마지막 수정 시간)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyChecklistResponse {

  private Long id;
  private Long petId;
  private Long careTemplateId;
  private LocalDate targetDate;
  private CareType careType;
  private String careTypeDescription;
  private String title;
  private String content;
  private boolean isCompleted;
  private Long completedByUserId;
  private String completedByNickname;
  /** ✅ 추가: 완료자 프로필 이미지 URL */
  private String completedByProfileImageUrl;
  private Integer version;
  private LocalDateTime updatedAt;
}