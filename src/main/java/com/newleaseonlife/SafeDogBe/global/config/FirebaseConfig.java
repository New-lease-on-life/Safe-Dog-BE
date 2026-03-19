package com.newleaseonlife.SafeDogBe.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

  // 파라미터 스토어의 이름을 변수로 받습니다.
  @Value("${firebase.credentials-content}")
  private String firebaseCredentialsContent;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {

      // AWS에서 가져온 Base64 인코딩된 문자열을 바이트 배열로 디코딩
      byte[] decodedBytes = java.util.Base64.getDecoder().decode(firebaseCredentialsContent);

      // 파일 경로 대신 문자열(JSON)을 스트림으로 변환
      InputStream credentialsStream = new ByteArrayInputStream(decodedBytes);

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(credentialsStream))
          .build();

      FirebaseApp.initializeApp(options);
      log.info("Firebase app initialized using AWS Parameter Store");
    }

    return FirebaseApp.getInstance();
  }

  @Bean
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
