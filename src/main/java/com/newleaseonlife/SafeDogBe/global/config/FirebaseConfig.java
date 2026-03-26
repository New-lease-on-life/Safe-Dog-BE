package com.newleaseonlife.SafeDogBe.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

  // 파라미터 스토어에서 전달받는 Base64 인코딩 Firebase credentials(JSON)
  @Value("${firebase.credentials-path:}")
  private String firebaseCredentialsContent;

  @Bean
  @ConditionalOnProperty(name = "firebase.credentials-path")
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
  @ConditionalOnProperty(name = "firebase.credentials-path")
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }
}
