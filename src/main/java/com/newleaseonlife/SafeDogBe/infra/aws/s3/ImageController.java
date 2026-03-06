package com.newleaseonlife.SafeDogBe.infra.aws.s3;

import com.newleaseonlife.SafeDogBe.global.common.dto.response.ImageUploadResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

  private final S3ServicePort s3ServicePort;

  /**
   * 공용 이미지 업로드 API
   * 프론트엔드는 이 API를 호출해 URL을 반환받은 뒤, 도메인 생성 API(유저, 반려동물 등)에 해당 URL을 문자열로 넘깁니다.
   * * @param directory S3 내 폴더명 (예: profiles, pets, care-notes)
   * @param file 업로드할 이미지 파일
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ImageUploadResponse> uploadImage(
      @RequestParam(value = "directory", defaultValue = "general") String directory,
      @RequestPart("file") MultipartFile file) {

    String uploadedUrl = s3ServicePort.uploadImage(directory, file);

    ImageUploadResponse response = ImageUploadResponse.builder()
        .imageUrl(uploadedUrl)
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}