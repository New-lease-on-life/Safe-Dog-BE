package com.newleaseonlife.SafeDogBe.domain.mypage.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MypageMarketingConsentResponse {

  private boolean agreed;

  /** 동의 시각(없으면 null). */
  private LocalDateTime agreedAt;

  public static MypageMarketingConsentResponse of(boolean agreed, LocalDateTime agreedAt) {
    return MypageMarketingConsentResponse.builder()
        .agreed(agreed)
        .agreedAt(agreedAt)
        .build();
  }
}

