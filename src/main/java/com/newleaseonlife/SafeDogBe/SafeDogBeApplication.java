package com.newleaseonlife.SafeDogBe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // ★ 배치 스케줄러 활성화 ★
@EnableJpaAuditing
@SpringBootApplication
public class SafeDogBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(SafeDogBeApplication.class, args);
    System.out.println("\n" +
        "=================================================\n" +
        "🚀 Shared ToDo Application 시작 완료!\n" +
        "=================================================\n" +
        "📋 Swagger UI: http://localhost:8080/swagger-ui/index.html\n" +
        "⭐ JPA Auditing 적용: 자동 시간/사용자 추적\n" +
        "=================================================\n");
  }


}
