package com.newleaseonlife.SafeDogBe.infra.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3ServicePort {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  // 기획서 요구사항: 10MB 제한, 특정 확장자 허용
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "heic");

  @Override
  public String uploadImage(String directory, MultipartFile multipartFile) {
    validateFile(multipartFile);

    String originalFilename = multipartFile.getOriginalFilename();
    String extension = getFileExtension(originalFilename);
    String s3FileName = directory + "/" + UUID.randomUUID() + "." + extension;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(multipartFile.getContentType());
    metadata.setContentLength(multipartFile.getSize());

    try (InputStream inputStream = multipartFile.getInputStream()) {
      amazonS3.putObject(new PutObjectRequest(bucket, s3FileName, inputStream, metadata)
          // (선택 사항) 버킷 정책에 따라 Public Read 권한 부여
          .withCannedAcl(CannedAccessControlList.PublicRead));
    } catch (IOException e) {
      log.error("S3 파일 업로드 중 오류 발생: {}", e.getMessage());
      throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
    }

    return amazonS3.getUrl(bucket, s3FileName).toString();
  }

  @Override
  public void deleteImage(String fileUrl) {
    if (fileUrl == null || !fileUrl.contains(bucket)) {
      return;
    }
    try {
      // URL에서 S3 Key(폴더명/파일명)만 추출
      String s3Key = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
      amazonS3.deleteObject(new DeleteObjectRequest(bucket, s3Key));
      log.info("S3 파일 삭제 완료: {}", s3Key);
    } catch (Exception e) {
      log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일이 비어있습니다.");
    }
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("이미지는 10MB 이하만 등록 가능합니다.");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isEmpty()) {
      throw new IllegalArgumentException("파일명이 없습니다.");
    }

    String extension = getFileExtension(originalFilename).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("이미지는 jpg, jpeg, png, HEIC 만 등록이 가능합니다.");
    }
  }

  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      return "";
    }
    return filename.substring(lastDotIndex + 1);
  }
}