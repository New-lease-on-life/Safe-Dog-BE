package com.newleaseonlife.SafeDogBe.infra.aws.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3ServicePort {

  /**
   * S3에 이미지 업로드 후 접근 가능한 객체 URL 반환
   * @param directory S3 내부 폴더명 (예: "profiles", "care-notes")
   * @param multipartFile 업로드할 파일
   * @return 업로드된 파일의 S3 URL
   */
  String uploadImage(String directory, MultipartFile multipartFile);

  /**
   * S3에서 파일 삭제
   * @param fileUrl 삭제할 파일의 S3 URL
   */
  void deleteImage(String fileUrl);
}