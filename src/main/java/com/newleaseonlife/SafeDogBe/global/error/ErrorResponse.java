package com.newleaseonlife.SafeDogBe.global.error;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private final int status;
  private final String code;
  private final String message;
}