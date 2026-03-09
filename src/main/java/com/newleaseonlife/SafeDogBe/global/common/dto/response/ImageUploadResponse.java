package com.newleaseonlife.SafeDogBe.global.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 이미지 업로드 API 응답. 업로드된 이미지 URL. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private String imageUrl;
}