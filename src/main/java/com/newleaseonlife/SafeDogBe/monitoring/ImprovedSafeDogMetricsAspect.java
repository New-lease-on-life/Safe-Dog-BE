package com.newleaseonlife.SafeDogBe.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ImprovedSafeDogMetricsAspect {

  private final MeterRegistry registry;

  /**
   * 📌 포인트컷: 중요 API만 모니터링
   * 
   * 모니터링 대상:
   * - FCMService: 알림 발송 (중요도: ★★★)
   * - MypageService: 마이페이지 조회 (중요도: ★★)
   * - PetService.create: 펫 등록 (중요도: ★★)
   * - AuthService.signup: 회원가입 (중요도: ★★)
   * - ChecklistMemoService: 체크리스트 (중요도: ★)
   */
  @Around("execution(* com.newleaseonlife.SafeDogBe.domain.notification.service.FCMService.*(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.mypage.service.MypageService.*(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.pet.service.PetService.create(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService.signup(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.petnote.service.ChecklistMemoService.*(..))")
  public Object monitorWithMetrics(ProceedingJoinPoint pjp) throws Throwable {
    String className = pjp.getSignature().getDeclaringType().getSimpleName();
    String methodName = pjp.getSignature().getName();
    String metricPrefix = getMetricPrefix(className);

    return recordMetrics(pjp, metricPrefix, methodName);
  }

  /**
   * 📊 메트릭 기록 (타이머 + 카운터)
   */
  private Object recordMetrics(ProceedingJoinPoint pjp, String metricPrefix, String methodName)
      throws Throwable {
    
    // 1️⃣ 타이머 샘플 시작
    Timer.Sample sample = Timer.start(registry);
    long startTime = System.currentTimeMillis();

    try {
      // 2️⃣ 메서드 실행
      Object result = pjp.proceed();

      // 3️⃣ 성공 카운터 증가
      Counter.builder(metricPrefix + ".success")
          .tag("method", methodName)
          .tag("status", "success")
          .description("Successful " + methodName + " execution count")
          .register(registry)
          .increment();

      long duration = System.currentTimeMillis() - startTime;
      log.debug("✅ [{}] {} 성공 ({}ms)", metricPrefix, methodName, duration);

      return result;

    } catch (Exception e) {
      // 4️⃣ 실패 카운터 증가 (예외 타입별)
      String errorType = e.getClass().getSimpleName();
      
      Counter.builder(metricPrefix + ".failure")
          .tag("method", methodName)
          .tag("error", errorType)
          .tag("status", "failure")
          .description("Failed " + methodName + " execution count")
          .register(registry)
          .increment();

      long duration = System.currentTimeMillis() - startTime;
      log.warn("❌ [{}] {} 실패 - {} ({}ms)", metricPrefix, methodName, errorType, duration);

      throw e;

    } finally {
      // 5️⃣ 응답 시간 기록 (성공/실패 모두 포함)
      sample.stop(
          Timer.builder(metricPrefix + ".time")
              .tag("method", methodName)
              .description("Execution time for " + methodName)
              .publishPercentiles(0.5, 0.95, 0.99)  // 중앙값, p95, p99
              .publishPercentileHistogram(true)      // 히스토그램 활성화
              .register(registry)
      );
    }
  }

  /**
   * 📌 Service 클래스명 → 메트릭 이름 변환
   * 
   * 예시:
   * - FCMService → "safedog.api.fcm"
   * - MypageService → "safedog.api.mypage"
   * - PetService → "safedog.api.pet"
   */
  private String getMetricPrefix(String className) {
    String serviceName = className
        .replace("Service", "")           // "FCMService" → "FCM"
        .toLowerCase();                    // "FCM" → "fcm"
    
    return "safedog.api." + serviceName;
  }
}
