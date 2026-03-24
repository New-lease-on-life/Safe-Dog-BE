package com.newleaseonlife.SafeDogBe.domain.petnote.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChecklistMemoRequest(
    @NotBlank(message = "메모 내용을 입력해주세요.")
    @Size(max = 100, message = "메모는 최대 100자까지 입력 가능합니다.")
    String content
) {}