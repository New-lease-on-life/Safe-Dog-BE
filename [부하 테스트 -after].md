# [부하 테스트 -after]

# FCM

### 단발성 폭격 (100건 생성)

```bash
curl -X POST "http://localhost:8082/api/monitoring/setup/notification?count=100&userId=2"
```

```bash
2026-03-26T01:24:38.014+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:24:38.029+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 5ms 소요
```

```bash
{
  "minResponseTime": 0.83,
  "successRate": 100,
  "maxResponseTime": 22.15,
  "avgResponseTime": 1.66,
  "peakRps": 2.5,
  "totalRequests": 100,
  "currentRps": 1.67,
  "p95ResponseTime": 18.83,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 1.67,
  "successCount": 100,
  "failureCount": 0,
  "p99ResponseTime": 21.05,
  "timestamp": 1774455958492
}
```

| **항목**              | **수치**          | **의미 및 해석**                                             |
| --------------------- | ----------------- | ------------------------------------------------------------ |
| **`avgResponseTime`** | **1.66ms**        | **[놀라운 수치]** 평균 응답 시간이 0.0016초입니다. 아까 500ms였던 것과 비교하면 약 **300배 빨라졌습니다.** |
| **`maxResponseTime`** | **22.15ms**       | 가장 느렸던 요청도 0.02초밖에 안 걸렸습니다. 초기 연결이나 스레드 생성 시의 일시적 지연으로 보입니다. |
| **`p95 / p99`**       | **18.83 / 21.05** | 전체 사용자의 99%가 21ms 이내에 응답을 받았습니다. 시스템이 매우 일관되게 빠르다는 뜻입니다. |
| **`totalRequests`**   | **100**           | 100건의 테스트 요청이 누락 없이 모두 집계되었습니다.         |
| **`avgRps`**          | **1.67**          | 현재 100건을 한 번에 쏘고 바로 조회했기 때문에 수치가 낮게 보이지만, 응답 시간이 1ms대이므로 서버는 초당 수천 건도 처리할 수 있는 잠재력을 가졌습니다. |

### 🧐 이 데이터가 증명하는 것

1. **비동기(`@Async`)의 승리**: 아까는 메인 스레드가 `Thread.sleep`에 묶여서 50초 동안 고생했지만, 지금은 요청을 받자마자 비동기 스레드에 넘기고 바로 응답(`return`)하기 때문에 사용자(테스터)가 느끼는 속도가 비약적으로 향상되었습니다.
2. **안정적인 꼬리 지연(Tail Latency)**: p99 수치가 21ms라는 것은 시스템에 병목 현상이 거의 없음을 의미합니다.
3. **정확한 메트릭 수집**: 아까 단위 변환 오류(`1,000,000`으로 나누기)를 수정하신 덕분에, 이제 소수점 단위의 정밀한 응답 시간(ms)이 잘 표현되고 있습니다.

---

### 동시 접속 폭격 (10명이 동시에 100건씩 총 1,000건)

명령어

```bash
for i in {1..10}; do   curl -s -X POST "http://localhost:8081/api/monitoring/setup/notification?count=100&userId=4" & done
```

```bash
2026-03-26T01:32:49.068+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.076+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-9] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.081+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 5ms 소요
2026-03-26T01:32:49.087+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.091+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.093+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.094+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-9] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 1ms 소요
2026-03-26T01:32:49.098+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.090+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.100+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-3] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.098+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 1ms 소요
2026-03-26T01:32:49.098+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.104+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-5] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=4
2026-03-26T01:32:49.105+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 1ms 소요
2026-03-26T01:32:49.114+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 1ms 소요
2026-03-26T01:32:49.113+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 4ms 소요
2026-03-26T01:32:49.115+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 2ms 소요
2026-03-26T01:32:49.117+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 1ms 소요
2026-03-26T01:32:49.120+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-3] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 0ms 소요
2026-03-26T01:32:49.121+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-5] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 0ms 소요
2026-03-26T01:33:01.750+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-5] c.n.S.m.LoadTestMetricsController        : 📊 메트릭 조회: fcm
```

```bash
{
  "minResponseTime": 0.09,
  "successRate": 100,
  "maxResponseTime": 4.37,
  "avgResponseTime": 0.17,
  "peakRps": 27.5,
  "totalRequests": 1100,
  "currentRps": 18.33,
  "p95ResponseTime": 3.71,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 18.33,
  "successCount": 1100,
  "failureCount": 0,
  "p99ResponseTime": 4.15,
  "timestamp": 1774456381751
}
```

### 📊 메트릭 결과 정밀 분석

#### 1. 환상적인 지연 시간 (Response Time)

- **`avgResponseTime`: 0.17ms**
  - **해석:** 요청 1건을 처리하는 데 평균 **0.17밀리초**가 걸렸습니다. 이건 거의 "빛의 속도"급입니다.
  - **이유:** 비동기(`@Async`) 처리가 성공적으로 적용되어, 사용자는 FCM 서버의 응답을 기다리지 않고 즉시 "OK"를 받았기 때문입니다.
- **`p95`: 3.71ms / `p99`: 4.15ms**
  - **해석:** 아무리 운이 나빠도(상위 1%) **4.15ms** 안에 처리가 끝났습니다. p99와 평균값의 차이가 매우 작으므로, 서버가 매우 일관성 있게 빠르게 동작하고 있다는 뜻입니다.

#### 2. 처리량 및 부하량 (RPS & Total)

- **`totalRequests`: 1,100건**
  - 아까 쉘 스크립트로 100건씩 10번(1,000건) + 알파로 테스트하신 결과가 누적된 것으로 보입니다.
- **`avgRps`: 18.33**
  - 1초당 약 18건의 요청을 처리했습니다. 현재는 `curl`로 순차적으로 쏜 수준이라 서버의 한계치(Peak)까지 도달한 것은 아니며, 아주 여유롭게 처리 중입니다.

#### 3. 성공률

- **`successRate`: 100%**
  - 1,100건 중 단 한 건의 실패도 없었습니다.

---

### 시나리오 3: 자동화 테스트

```bash
╔════════════════════════════════════════════════════════════╗
║          SafeDog 부하테스트 리포트                            ║
╚════════════════════════════════════════════════════════════╝

📅 테스트 시간: 2026-03-25 16:36:32
🔗 API: fcm
👤 User ID: 4
📊 요청 수: 1000
⏱️  테스트 기간: 60초

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📈 성능 지표

{
  "총 요청": 2100,
  "성공": 2100,
  "실패": 0,
  "성공률": "100.0%",
  "실패율": "0.0%",
  "평균 응답시간": "0.1ms",
  "최소 응답시간": "0.05ms",
  "최대 응답시간": "6.11ms",
  "P95 응답시간": "5.19ms",
  "P99 응답시간": "5.8ms",
  "현재 RPS": "35.0 req/s",
  "최고 RPS": "52.5 req/s",
  "평균 RPS": "35.0 req/s"
}
```



---

# mypage, 펫 50마리

### 단발성 폭격 (100,1000건 생성)

```bash
100건 생성
2026-03-26T09:30:32.817+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : 📄 마이페이지 성능 테스트 시작: count=100, userId=4
2026-03-26T09:30:33.678+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : ✅ 마이페이지 성능 테스트 완료: 854ms 소요

1000건 생성
2026-03-26T09:29:00.654+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : 📄 마이페이지 성능 테스트 시작: count=1000, userId=4
2026-03-26T09:29:09.908+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : ✅ 마이페이지 성능 테스트 완료: 9241ms 소요

```

```bash
{
  "minResponseTime": 2.09,
  "successRate": 100,
  "maxResponseTime": 61.08,
  "avgResponseTime": 4.17,
  "peakRps": 52.5,
  "totalRequests": 2100,
  "currentRps": 35,
  "p95ResponseTime": 51.92,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 35,
  "successCount": 2100,
  "failureCount": 0,
  "p99ResponseTime": 58.03,
  "timestamp": 1774485074763
}
```

| **지표**              | **Before (N+1 발생)**    | **After (JOIN FETCH 적용)** | **개선 효과**        |
| --------------------- | ------------------------ | --------------------------- | -------------------- |
| **1000건 처리 시간**  | **127,604ms (약 127초)** | **9,241ms (약 9.2초)**      | **약 13.8배 빨라짐** |
| **평균 응답 시간**    | **122.95ms**             | **4.17ms**                  | **약 29.5배 빨라짐** |
| **평균 처리량 (RPS)** | **16.67 requests/s**     | **35.0 requests/s**         | **약 2배 이상 상승** |
| **p99 (꼬리 지연)**   | **152.87ms**             | **58.03ms**                 | **약 2.6배 안정화**  |

### 🧐 왜 이렇게 차이가 날까요? (핵심 기술 분석)

**1. 쿼리 수의 폭발적 감소**

- **Before:** 마이페이지 하나를 그릴 때 `유저 조회(1) + 펫 목록 조회(1) + 각 펫당 보호자 조회(N)`의 쿼리가 나갔습니다. 1,000건 테스트 시 수천 번의 네트워크 왕복(Round-trip)이 발생해 시간이 **127초**나 걸린 것입니다.
- **After:** 직접 작성하신 `@Query`와 `JOIN FETCH` 덕분에 **단 한 번의 복합 쿼리**로 모든 연관 데이터를 가져옵니다. DB와의 대화 횟수가 획기적으로 줄어들었습니다.

**2. 지연 로딩(Lazy Loading) 비용 제거**

- JPA에서 루프를 돌며 연관 엔티티에 접근할 때 발생하는 Proxy 객체 초기화 비용이 사라졌습니다. 덕분에 `avgResponseTime`이 **4ms**라는 경이로운 수치로 떨어졌습니다.

**N+1 문제 해결을 통한 조회 성능 1,300% 개선"**

- **문제:** 기존 마이페이지 조회 시 펫 수에 비례한 다중 쿼리 발생으로 1,000건 처리 시 약 127초 소요 (avg 122ms).
- **해결:** `JOIN FETCH`를 활용한 단일 쿼리 최적화 수행.
- **결과:** 평균 응답 시간을 **122ms에서 4ms로 단축**, 1,000건 처리 시간을 **127초에서 9초 대**로 줄이며 대규모 요청 상황에서의 시스템 안정성 확보.

```bash
╔════════════════════════════════════════════════════════════╗
║          SafeDog 부하테스트 리포트                         ║
╚════════════════════════════════════════════════════════════╝

📅 테스트 시간: 2026-03-26 00:36:38
🔗 API: mypage
👤 User ID: 4
📊 요청 수: 1000
⏱️  테스트 기간: 60초

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📈 성능 지표

{
  "총 요청": 3100,
  "성공": 3100,
  "실패": 0,
  "성공률": "100.0%",
  "실패율": "0.0%",
  "평균 응답시간": "4.03ms",
  "최소 응답시간": "2.02ms",
  "최대 응답시간": "17.36ms",
  "P95 응답시간": "14.75ms",
  "P99 응답시간": "16.49ms",
  "현재 RPS": "51.67 req/s",
  "최고 RPS": "77.5 req/s",
  "평균 RPS": "51.67 req/s"
}
```



----

## checklistmemo

벌크 처리

```bash
1000건 생성
2026-03-26T10:35:14.487+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✍️ 메모 성능 테스트 시작: count=1000, userId=4, checklistId=1
2026-03-26T10:35:16.229+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✅ 메모 성능 테스트 완료: 1742ms 소요

10000건 생성
2026-03-26T10:43:16.623+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✍️ 메모 성능 테스트 시작: count=10000, userId=4, checklistId=1
2026-03-26T10:43:27.209+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✅ 메모 성능 테스트 완료: 10586ms 소요
```

```bash
{
  "minResponseTime": 1.85,
  "successRate": 100,
  "maxResponseTime": 0,
  "avgResponseTime": 3.71,
  "peakRps": 250.05,
  "totalRequests": 10002,
  "currentRps": 166.7,
  "p95ResponseTime": 0,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 166.7,
  "successCount": 10002,
  "failureCount": 0,
  "p99ResponseTime": 0,
  "timestamp": 1774489445340
}
```

| **지표**          | **[Before] 개별 호출 (Loop)** | **[After] 벌크 호출 (Bulk)** | **개선율**      |
| ----------------- | ----------------------------- | ---------------------------- | --------------- |
| **1,000건 생성**  | 8,686ms (8.6초)               | **1,742ms (1.7초)**          | **약 80% 단축** |
| **10,000건 생성** | 77,303ms (77초)               | **10,586ms (10초)**          | **약 86% 단축** |

#### 1. 트랜잭션 오버헤드 제거 (가장 큰 이유)

- **기존**: 컨트롤러에서 루프를 돌며 서비스를 불렀기 때문에, DB와 연결하고 트랜잭션을 시작/커밋하는 무거운 작업을 **10,000번** 반복했습니다.
- **현재**: 서비스를 한 번만 호출(`@Transactional` 안에서 루프)하므로, 트랜잭션 시작/커밋이 **딱 1번**만 일어납니다. 문을 10,000번 열고 닫느냐, 1번 열고 다 하느냐의 차이입니다.

#### 2. 네트워크 왕복(RTT) 비용 절감

- **기존**: 애플리케이션 서버 ↔ DB 서버 사이를 10,000번 왕복했습니다.
- **현재**: 리스트를 한 번에 넘기므로 통신 횟수가 급격히 줄어듭니다. 특히 `application-prod.yaml`에 추가하신 `rewriteBatchedStatements=true` 설정 덕분에 MySQL 드라이버가 여러 INSERT 문을 하나로 묶어 DB에 전달했습니다.

#### 3. 하이버네이트의 '쓰기 지연' 최적화

`saveAll` 내부에서 리스트를 다 만들고 마지막에 `flush`할 때, 하이버네이트는 설정한 `batch_size: 1000`에 맞춰 데이터를 1,000개씩 묶어서 보냅니다. DB 엔진 입장에서는 한 번에 대량의 데이터를 인덱싱하는 것이 훨씬 효율적입니다.



**[대량 데이터 삽입 성능 86% 개선: 트랜잭션 범위 최적화 및 JDBC Batch 적용]**

- **현상**: 1만 건의 메모 생성 요청 시, 개별 트랜잭션 처리 방식으로 인해 77초의 응답 지연 발생.
- **원인**: 빈번한 트랜잭션 Context 전환 오버헤드 및 데이터베이스 네트워크 왕복(RTT) 비용 누적.
- **해결**:
  1. 비즈니스 로직을 서비스 레이어 내부의 단일 트랜잭션으로 통합.
  2. MySQL `rewriteBatchedStatements` 및 Hibernate `batch_size` 설정을 통한 JDBC 배치 저장 최적화.
- **성과**: 동일 부하(1만 건) 기준 처리 속도를 **77초에서 10초로 단축(86% 향상)**하였으며, DB 커넥션 점유 효율을 극대화함.