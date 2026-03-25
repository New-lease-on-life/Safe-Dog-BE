package com.newleaseonlife.SafeDogBe.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SafeDogMetricsAspect {

  private final MeterRegistry registry;

  // 포인트컷 정의 (가독성을 위해 상단으로 분리)
  @Around("execution(* com.newleaseonlife.SafeDogBe.domain.notification.service.FCMService.*(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.mypage.service.MypageService.*(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.pet.service.PetService.create(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.auth.service.AuthService.signup(..)) || " +
      "execution(* com.newleaseonlife.SafeDogBe.domain.petnote.service.ChecklistMemoService.*(..))")
  public Object monitorAll(ProceedingJoinPoint pjp) throws Throwable {
    // 메트릭 이름 결정 로직 (패키지명에 따라 태그 분리 가능)
    String className = pjp.getSignature().getDeclaringType().getSimpleName();
    String metricName = "safedog.api." + className.toLowerCase().replace("service", "");

    return recordWithTag(pjp, metricName);
  }

  private Object recordWithTag(ProceedingJoinPoint pjp, String metricName) throws Throwable {
    String methodName = pjp.getSignature().getName();

    // 1. 타이머 시작 (Micrometer 공식 권장 방식)
    Timer.Sample sample = Timer.start(registry);

    try {
      Object result = pjp.proceed();

      // 2. 성공 카운트 증가
      Counter.builder(metricName + ".success")
          .tag("method", methodName)
          .register(registry).increment();

      return result;
    } catch (Throwable e) {
      // 3. 실패 카운트 및 에러 타입 기록
      Counter.builder(metricName + ".failure")
          .tag("method", methodName)
          .tag("error", e.getClass().getSimpleName())
          .register(registry).increment();
      throw e;
    } finally {
      // 4. 시간 기록 종료 (자동으로 Timer에 누적)
      sample.stop(Timer.builder(metricName + ".time")
          .tag("method", methodName)
          .description("Execution time for " + methodName)
          .register(registry));
    }
  }
}
