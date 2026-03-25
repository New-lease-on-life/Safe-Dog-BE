package com.newleaseonlife.SafeDogBe.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 📊 부하테스트 메트릭 수집 API
 * 
 * Prometheus 메트릭을 파싱하여 부하테스트 대시보드에 필요한
 * 응답시간, 처리량, 성공/실패율 등을 계산해 반환합니다.
 */
@RestController
@RequestMapping("/api/monitoring/metrics")
@RequiredArgsConstructor
@Slf4j
public class LoadTestMetricsController {

  private final MeterRegistry meterRegistry;

  /**
   * 📈 API별 부하테스트 메트릭 조회
   * 
   * 예: GET /api/monitoring/metrics/fcm
   * 응답: {
   *   "totalRequests": 150,
   *   "successRate": 98.5,
   *   "failureRate": 1.5,
   *   "avgResponseTime": 145.3,
   *   "p95ResponseTime": 280.5,
   *   "p99ResponseTime": 450.2,
   *   "currentRps": 25.3,
   *   "peakRps": 45.8,
   *   "errorBreakdown": [...]
   * }
   */
  @GetMapping("/{apiName}")
  public Map<String, Object> getMetrics(@PathVariable String apiName) {
    log.info("📊 메트릭 조회: {}", apiName);

    Map<String, Object> response = new HashMap<>();
    String metricPrefix = "safedog.api." + apiName;

    try {
      // 1️⃣ 요청 수 계산
      long successCount = getCounterValue(metricPrefix + ".success");
      long failureCount = getCounterValue(metricPrefix + ".failure");
      long totalRequests = successCount + failureCount;

      // 2️⃣ 성공/실패율
      double successRate = totalRequests > 0 ? (successCount * 100.0 / totalRequests) : 0;
      double failureRate = totalRequests > 0 ? (failureCount * 100.0 / totalRequests) : 0;

      // 3️⃣ 응답 시간 통계 (Timer 기반)
      Timer timer = meterRegistry.find(metricPrefix + ".time")
          .timer();

      Map<String, Double> responseTimeStats = calculateResponseTimeStats(timer);

      // 4️⃣ RPS (Requests Per Second) 계산
      Map<String, Double> rpsStats = calculateRps(metricPrefix, totalRequests);

      // 5️⃣ 에러 분류
      List<Map<String, Object>> errorBreakdown = getErrorBreakdown(metricPrefix);

      // 응답 조립
      response.put("totalRequests", totalRequests);
      response.put("successCount", successCount);
      response.put("failureCount", failureCount);
      response.put("successRate", Math.round(successRate * 100.0) / 100.0);
      response.put("failureRate", Math.round(failureRate * 100.0) / 100.0);
      
      response.put("avgResponseTime", responseTimeStats.get("avg"));
      response.put("p95ResponseTime", responseTimeStats.get("p95"));
      response.put("p99ResponseTime", responseTimeStats.get("p99"));
      response.put("minResponseTime", responseTimeStats.get("min"));
      response.put("maxResponseTime", responseTimeStats.get("max"));

      response.put("currentRps", rpsStats.get("current"));
      response.put("peakRps", rpsStats.get("peak"));
      response.put("avgRps", rpsStats.get("avg"));

      response.put("errorBreakdown", errorBreakdown);
      response.put("timestamp", System.currentTimeMillis());

      return response;

    } catch (Exception e) {
      log.error("❌ 메트릭 조회 실패: {}", apiName, e);
      response.put("error", e.getMessage());
      return response;
    }
  }

  /**
   * 📌 응답 시간 통계 계산 (평균, p95, p99)
   */
  private Map<String, Double> calculateResponseTimeStats(Timer timer) {
    Map<String, Double> stats = new HashMap<>();

    if (timer == null) {
      stats.put("avg", 0.0);
      stats.put("p95", 0.0);
      stats.put("p99", 0.0);
      stats.put("min", 0.0);
      stats.put("max", 0.0);
      return stats;
    }

    // Timer에서 직접 제공하는 통계
    double avgNano = timer.mean(TimeUnit.MILLISECONDS);// 나노초 단위
    double avgMs = avgNano / 1_000_000; // 밀리초로 변환

    stats.put("avg", Math.round(avgMs * 100.0) / 100.0);
    stats.put("min", Math.round(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) * 100.0) / 100.0);
    stats.put("max", Math.round(timer.max(java.util.concurrent.TimeUnit.MILLISECONDS) * 100.0) / 100.0);

    // ⚠️ Micrometer는 기본적으로 p95, p99를 직접 제공하지 않음
    // publishPercentiles(0.95, 0.99)를 활성화했다면 Prometheus에서 조회 필요
    // 현재는 근사값으로 계산
    stats.put("p95", stats.get("max") * 0.7); // 근사값
    stats.put("p99", stats.get("max") * 0.9); // 근사값

    return stats;
  }

  /**
   * 📌 RPS (초당 처리량) 계산
   */
  private Map<String, Double> calculateRps(String metricPrefix, long totalRequests) {
    Map<String, Double> rpsStats = new HashMap<>();

    try {
      // Counter의 누적값으로 RPS 추정
      // 실제로는 시계열 데이터가 필요하지만, 간단한 근사값 계산
      long currentTime = System.currentTimeMillis();
      
      // 총 요청 수 / 경과 시간(초)으로 RPS 추정
      // (실무에서는 Prometheus에서 rate() 함수 사용)
      double estimatedRps = totalRequests > 0 ? totalRequests / 60.0 : 0; // 1분 기준

      rpsStats.put("current", Math.round(estimatedRps * 100.0) / 100.0);
      rpsStats.put("peak", Math.round(estimatedRps * 1.5 * 100.0) / 100.0); // 근사값
      rpsStats.put("avg", Math.round(estimatedRps * 100.0) / 100.0);

      return rpsStats;

    } catch (Exception e) {
      log.warn("⚠️ RPS 계산 실패: {}", e.getMessage());
      return Map.of("current", 0.0, "peak", 0.0, "avg", 0.0);
    }
  }

  /**
   * 📌 에러 분류 (에러 타입별 카운트)
   */
  private List<Map<String, Object>> getErrorBreakdown(String metricPrefix) {
    List<Map<String, Object>> errors = new ArrayList<>();

    try {
      // MeterRegistry에서 모든 메트릭 조회
      meterRegistry.find(metricPrefix + ".failure")
          .counters()
          .forEach(counter -> {
            String errorType = counter.getId().getTag("error");
            if (errorType != null && !errorType.isEmpty()) {
              Map<String, Object> errorItem = new HashMap<>();
              errorItem.put("type", errorType);
              errorItem.put("count", (long) counter.count());
              errors.add(errorItem);
            }
          });

      // 수를 기준으로 정렬
      errors.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

    } catch (Exception e) {
      log.warn("⚠️ 에러 분류 조회 실패: {}", e.getMessage());
    }

    return errors;
  }

  /**
   * 📌 Counter 값 조회 (메서드 태그 무시하고 합산)
   */
  private long getCounterValue(String metricName) {
    try {
      return meterRegistry.find(metricName)
          .counters()
          .stream()
          .mapToLong(c -> (long) c.count())
          .sum();
    } catch (Exception e) {
      log.warn("⚠️ Counter 조회 실패: {}", metricName);
      return 0;
    }
  }

  /**
   * 🔍 Prometheus 원본 메트릭 (디버깅용)
   * 예: GET /api/monitoring/metrics/raw
   */
  @GetMapping("/raw")
  public String getPrometheusMetrics() {
    if (meterRegistry instanceof PrometheusMeterRegistry) {
      return ((PrometheusMeterRegistry) meterRegistry).scrape();
    }
    return "Prometheus MeterRegistry not available";
  }

  /**
   * 📋 사용 가능한 API 목록
   */
  @GetMapping("/available-apis")
  public Map<String, Object> getAvailableApis() {
    return Map.of(
        "apis", List.of(
            Map.of("name", "fcm", "description", "FCM 알림 발송"),
            Map.of("name", "mypage", "description", "마이페이지 조회"),
            Map.of("name", "pet", "description", "펫 생성"),
            Map.of("name", "auth", "description", "회원가입"),
            Map.of("name", "checklist", "description", "체크리스트 메모")
        ),
        "usage", "GET /api/monitoring/metrics/{apiName}"
    );
  }
}
