package com.newleaseonlife.SafeDogBe.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 기존 서비스 코드를 수정하지 않고 핵심 비즈니스 이벤트를 Prometheus 메트릭으로 수집하는 AOP.
 *
 * <h3>수집 항목</h3>
 * <ul>
 *   <li>safedog.checklist.action.total  (action=complete|uncheck) – 체크리스트 완료/취소 횟수</li>
 *   <li>safedog.pet.created.total                                  – 반려동물 등록 횟수</li>
 *   <li>safedog.guardian.added.total                               – 공동보호자 추가 횟수</li>
 *   <li>safedog.service.operation (service, method 태그)           – 핵심 서비스 응답 시간</li>
 * </ul>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SafeDogMetricsAspect {

    private final MeterRegistry registry;

    // Counter를 필드로 캐싱. 매 호출마다 registry 조회(HashMap lookup)가 반복되지 않도록.
    private Counter checklistCompleteCounter;
    private Counter checklistUncheckCounter;
    private Counter petCreatedCounter;
    private Counter guardianAddedCounter;

    /**
     * Timer는 (service, method) 태그 조합별로 생성되므로 캐싱해 매 호출마다 registry 조회를 피한다.
     * key 형식: "ServiceClass#methodName"
     */
    private final ConcurrentHashMap<String, Timer> operationTimerCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCounters() {
        checklistCompleteCounter = Counter.builder("safedog.checklist.action.total")
                .description("Number of checklist actions")
                .tag("action", "complete")
                .register(registry);

        checklistUncheckCounter = Counter.builder("safedog.checklist.action.total")
                .description("Number of checklist actions")
                .tag("action", "uncheck")
                .register(registry);

        petCreatedCounter = Counter.builder("safedog.pet.created.total")
                .description("Number of pets registered")
                .register(registry);

        guardianAddedCounter = Counter.builder("safedog.guardian.added.total")
                .description("Number of co-guardians added")
                .register(registry);

        log.info("[SafeDogMetricsAspect] 메트릭 카운터 초기화 완료");
    }

    // ────────────── Pointcuts ──────────────

    @Pointcut("execution(* com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService.*(..))")
    private void checklistServiceMethods() {}

    @Pointcut("execution(* com.newleaseonlife.SafeDogBe.domain.pet.service.PetService.*(..))")
    private void petServiceMethods() {}

    // ────────────── Counters ──────────────

    /** 체크리스트 완료 처리 횟수 */
    @AfterReturning("execution(* com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService.completeChecklist(..))")
    public void countChecklistCompleted() {
        checklistCompleteCounter.increment();
    }

    /** 체크리스트 완료 취소 횟수 */
    @AfterReturning("execution(* com.newleaseonlife.SafeDogBe.domain.care.service.DailyChecklistService.uncompleteChecklist(..))")
    public void countChecklistUncompleted() {
        checklistUncheckCounter.increment();
    }

    /** 반려동물 등록 횟수 */
    @AfterReturning("execution(* com.newleaseonlife.SafeDogBe.domain.pet.service.PetService.create(..))")
    public void countPetCreated() {
        petCreatedCounter.increment();
    }

    /** 공동보호자 추가 횟수 */
    @AfterReturning("execution(* com.newleaseonlife.SafeDogBe.domain.pet.service.PetService.addGuardian(..))")
    public void countGuardianAdded() {
        guardianAddedCounter.increment();
    }

    // ────────────── Timers ──────────────

    /**
     * 체크리스트·펫 서비스 주요 메서드 응답 시간 측정.
     * Grafana에서 service + method 태그로 분류해 병목 확인 가능.
     * 예외 발생 시에도 finally로 타이머를 기록하고, 에러 로그를 남겨 추적 가능하게 함.
     */
    @Around("checklistServiceMethods() || petServiceMethods()")
    public Object timeServiceOperations(ProceedingJoinPoint pjp) throws Throwable {
        String service = pjp.getTarget().getClass().getSimpleName();
        String method = pjp.getSignature().getName();
        String timerKey = service + "#" + method;
        long startNano = System.nanoTime();
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            log.error("[Metrics] 서비스 예외 발생 service={}, method={}, error={}", service, method, t.getMessage());
            throw t;
        } finally {
            Timer timer = operationTimerCache.computeIfAbsent(timerKey, key ->
                    Timer.builder("safedog.service.operation")
                            .description("SafeDog service operation execution time")
                            .tag("service", service)
                            .tag("method", method)
                            .register(registry)
            );
            timer.record(System.nanoTime() - startNano, TimeUnit.NANOSECONDS);
        }
    }
}
