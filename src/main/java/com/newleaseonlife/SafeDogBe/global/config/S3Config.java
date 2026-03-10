package com.newleaseonlife.SafeDogBe.global.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  public AmazonS3 amazonS3Client() {
    // 백엔드 보안 핵심: AWS 자격 증명 탐색 체인 적용 (OIDC, EC2 IAM Role 자동 인식)
    return AmazonS3ClientBuilder
        .standard()
        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
        .withRegion(region)
        .build();
  }
}