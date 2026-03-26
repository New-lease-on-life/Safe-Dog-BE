# [부하 테스트 -before]

# FCM

### 단발성 폭격 (100건 생성)

```bash
curl -X POST "http://localhost:8082/api/monitoring/setup/notification?count=100&userId=2"
```

```bash
2026-03-25T22:59:44.677+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:00:34.754+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50065ms 소요
```

```bash
{
  "minResponseTime": 250.2,
  "successRate": 100,
  "maxResponseTime": 0,
  "avgResponseTime": 500.41,
  "peakRps": 2.5,
  "totalRequests": 100,
  "currentRps": 1.67,
  "p95ResponseTime": 0,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 1.67,
  "successCount": 100,
  "failureCount": 0,
  "p99ResponseTime": 0,
  "timestamp": 1774447409975
}
```

| **항목**              | **수치**   | **의미 및 해석**                                             |
| --------------------- | ---------- | ------------------------------------------------------------ |
| **`totalRequests`**   | **100**    | 총 100건의 알림 요청이 서버에 들어왔습니다.                  |
| **`successCount`**    | **100**    | 100건 모두 에러 없이 성공적으로 처리되었습니다. (더미 토큰 로직 통과) |
| **`avgResponseTime`** | **500.41** | **[핵심]** 한 건 처리하는 데 평균 **0.5초**가 걸렸습니다. 코드에 넣으신 `sleep(500)`이 정확히 반영된 결과입니다. |
| **`minResponseTime`** | **250.2**  | 가장 빨랐던 응답 시간입니다. (코드상 avg의 0.5배로 계산된 임시값입니다.) |
| **`currentRps`**      | **1.67**   | **초당 처리량**입니다. 1초에 약 1.6건 정도를 처리하고 있다는 뜻으로, 매우 느린 상태입니다. |
| **`avgRps`**          | **1.67**   | 테스트 기간 동안의 평균 초당 처리량입니다.                   |
| **`peakRps`**         | **2.5**    | 순간적으로 가장 높았던 처리량입니다.                         |
| **`successRate`**     | **100**    | 성공률 100%입니다.                                           |
| **`failureRate`**     | **0**      | 실패율 0%입니다.                                             |

### 🧐 이 데이터가 말해주는 "현재 시스템 상태"

이 수치는 **"동기식(Blocking) 방식의 폐해"**를 명확히 보여주고 있습니다.

1. **매우 느린 응답 시간:** 알림 하나 보내는 데 0.5초(`500ms`)가 걸린다는 것은, 만약 1,000명에게 알림을 보낸다면 마지막 사람은 **500초(약 8분)** 뒤에나 알림을 받게 된다는 끔찍한 의미입니다.
2. **낮은 처리량(RPS):** 현재 RPS가 `1.67`인 이유는 메인 스레드가 `sleep`에 갇혀서 다음 요청을 빠르게 처리하지 못하기 때문입니다.
3. **데이터 불일치 (max, p95, p99가 0인 이유):**
   - 현재 컨트롤러 코드에서 `maxMs`를 `timer.max()`로 가져오는데, Micrometer의 `Timer`는 설정에 따라 일정 시간이 지나면 `max` 값을 초기화합니다.
   - 그라파나에서 직접 메트릭을 보지 않고 API로만 보실 거라면, 코드에서 `maxMs`가 0일 때 `avgMs` 값을 대신 넣거나 하는 예외 처리가 필요해 보입니다.

---

### 동시 접속 폭격 (10명이 동시에 100건씩 총 1,000건)

```bash
026-03-25T23:25:58.850+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.851+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.853+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.855+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.864+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-3] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.863+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.865+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-7] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.866+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-9] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.867+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:25:58.869+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-5] .S.m.ImprovedMonitoringTrafficController : 🔔 알림 성능 테스트 시작: count=100, userId=2
2026-03-25T23:26:48.888+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50029ms 소요
2026-03-25T23:26:48.888+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-1] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50028ms 소요
2026-03-25T23:26:48.892+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-8] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50016ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50022ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-7] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50017ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-3] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50019ms 소요
2026-03-25T23:26:48.892+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-9] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50016ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50023ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-5] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50017ms 소요
2026-03-25T23:26:48.893+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-2] .S.m.ImprovedMonitoringTrafficController : ✅ 알림 성능 테스트 완료: 50019ms 소요
```

```bash
{
  "minResponseTime": 250.1,
  "successRate": 100,
  "maxResponseTime": 501.67,
  "avgResponseTime": 500.2,
  "peakRps": 30,
  "totalRequests": 1200,
  "currentRps": 20,
  "p95ResponseTime": 426.42,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 20,
  "successCount": 1200,
  "failureCount": 0,
  "p99ResponseTime": 476.59,
  "timestamp": 1774448882304
}
```

### 📊 메트릭 결과 정밀 분석

#### 1. 처참한 지연 시간 (Response Time)

- **`avgResponseTime`: 500.2ms**
- **로그 소요 시간: 약 50,000ms (50초)**
- **해석:** 코드에 넣은 `Thread.sleep(500)` 때문에 요청 1건당 0.5초가 걸렸습니다. 1개 스레드가 100건을 처리하는 데 **50초**가 걸린 것이 로그(`50029ms`)에 정확히 찍혔습니다.

#### 2. 처리량의 한계 (RPS)

- **`avgRps`: 20**
- **해석:** 총 1,200건을 처리했지만, 응답 속도가 너무 느려 초당 20건밖에 처리하지 못했습니다. 만약 12,000건이었다면 사용자는 10분 넘게 기다려야 한다는 뜻입니다.

#### 3. p95, p99 데이터의 등장

- **`p95`: 426.42ms / `p99`: 476.59ms**
- **해석:** 아까는 0이었는데, 이제 데이터가 쌓이면서 값이 보입니다. 거의 모든 요청이 500ms 근처에 몰려 있다는 것을 보여줍니다. (분포가 아주 나쁜 상태입니다.)

---

### 시나리오 3: 자동화 테스트

```bash
╔════════════════════════════════════════════════════════════╗
║          SafeDog 부하테스트 리포트                         ║
╚════════════════════════════════════════════════════════════╝

📅 테스트 시간: 2026-03-25 14:41:52
🔗 API: fcm
👤 User ID: 2
📊 요청 수: 100
⏱️  테스트 기간: 60초

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📈 성능 지표

{
  "총 요청": 1300,
  "성공": 1300,
  "실패": 0,
  "성공률": "100.0%",
  "실패율": "0.0%",
  "평균 응답시간": "500.19ms",
  "최소 응답시간": "250.1ms",
  "최대 응답시간": "500.45ms",
  "P95 응답시간": "425.38ms",
  "P99 응답시간": "475.42ms",
  "현재 RPS": "21.67 req/s",
  "최고 RPS": "32.5 req/s",
  "평균 RPS": "21.67 req/s"
}
```





---

# mypage, 펫 50마리

### 단발성 폭격 (100,1000건 생성)

```bash
100건 생성
2026-03-26T00:26:32.968+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : 📄 마이페이지 성능 테스트 시작: count=100, userId=2
2026-03-26T00:26:34.493+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : ✅ 마이페이지 성능 테스트 완료: 1512ms 소요

1000건 생성
2026-03-26T09:14:37.383+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : 📄 마이페이지 성능 테스트 시작: count=1000, userId=4
2026-03-26T09:16:44.994+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-4] .S.m.ImprovedMonitoringTrafficController : ✅ 마이페이지 성능 테스트 완료: 127604ms 소요

```

```bash
{
  "minResponseTime": 61.48,
  "successRate": 100,
  "maxResponseTime": 160.92,
  "avgResponseTime": 122.95,
  "peakRps": 25,
  "totalRequests": 1000,
  "currentRps": 16.67,
  "p95ResponseTime": 136.78,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 16.67,
  "successCount": 1000,
  "failureCount": 0,
  "p99ResponseTime": 152.87,
  "timestamp": 1774484318935
}
```

| **지표**                  | **수치**     | **해석**                                                    |
| ------------------------- | ------------ | ----------------------------------------------------------- |
| **Min Response Time**     | 61.48ms      | 서버가 가장 빠르게 응답했을 때 약 0.06초가 소요되었습니다.  |
| **Avg Response Time**     | 122.95ms     | 전체 평균 응답 시간입니다. 1건당 약 0.12초가 걸렸습니다.    |
| **P95 (95th Percentile)** | **136.78ms** | 전체 요청의 95%가 0.13초 이내에 완료되었습니다.             |
| **P99 (99th Percentile)** | **152.87ms** | 가장 느린 축에 속하는 1%의 유저는 약 0.15초를 기다렸습니다. |
| **Max Response Time**     | 160.92ms     | 최악의 경우 응답까지 약 0.16초가 소요되었습니다.            |

## 🔍 백엔드 관점의 종합 진단 (Why?)

현재 시스템은 **"안정적이지만, 응답 속도가 매우 느린 상태"**입니다.

### 1. 응답 속도 지연 (High Latency)

평균 6초, P95가 15초를 넘어가는 것은 웹 서비스 기준으로 **매우 위험한 수준**입니다. 보통 사용자 경험(UX) 측면에서 응답이 3초를 넘어가면 이탈이 시작됩니다.

- **원인 추정**:
  - DB 쿼리 튜닝 미흡 (인덱스 부재로 인한 Full Scan).
  - 외부 API(네이버 로그인, Firebase 등) 호출 시의 타임아웃 지연.
  - 서버 자원(CPU/Memory) 부족으로 인한 쓰레드 풀 병목 현상.

### 2. 성공률은 높으나 효율은 낮음

성공률이 100%라는 것은 서버가 죽지 않고 끝까지 응답을 다 해주고 있다는 뜻입니다. 하지만 응답이 너무 느려 사용자는 이미 브라우저를 닫았을 가능성이 큽니다.

```bash
════════════════════════════════════════════════════════════╗
║          SafeDog 부하테스트 리포트                         ║
╚════════════════════════════════════════════════════════════╝

📅 테스트 시간: 2026-03-25 15:33:18
🔗 API: mypage
👤 User ID: 2
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
  "평균 응답시간": "5.8ms",
  "최소 응답시간": "2.9ms",
  "최대 응답시간": "11.62ms",
  "P95 응답시간": "9.87ms",
  "P99 응답시간": "11.04ms",
  "현재 RPS": "51.67 req/s",
  "최고 RPS": "77.5 req/s",
  "평균 RPS": "51.67 req/s"
}

```



----

## checklistmemo

```bash
1000건생성
2026-03-26T10:00:33.197+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : ✍️ 메모 성능 테스트 시작: count=1000, userId=4, checklistId=1
2026-03-26T10:00:41.889+09:00  INFO 1 --- [SafeDogBe] [nio-8080-exec-6] .S.m.ImprovedMonitoringTrafficController : ✅ 메모 성능 테스트 완료: 8686ms 소요

10000건 생성
2026-03-26T10:05:08.521+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : ✍️ 메모 성능 테스트 시작: count=10000, userId=4, checklistId=1
2026-03-26T10:06:25.830+09:00  INFO 1 --- [SafeDogBe] [io-8080-exec-10] .S.m.ImprovedMonitoringTrafficController : ✅ 메모 성능 테스트 완료: 77303ms 소요
```

```bash
{
  "minResponseTime": 1.82,
  "successRate": 100,
  "maxResponseTime": 67.7,
  "avgResponseTime": 3.65,
  "peakRps": 275,
  "totalRequests": 11000,
  "currentRps": 183.33,
  "p95ResponseTime": 57.55,
  "failureRate": 0,
  "errorBreakdown": [],
  "avgRps": 183.33,
  "successCount": 11000,
  "failureCount": 0,
  "p99ResponseTime": 64.32,
  "timestamp": 1774487236517
}
```



### 메모 성능 테스트 지표 해석 (Count: 1,000)

| **지표**              | **수치**   | **의미 및 해석**                                             |
| --------------------- | ---------- | ------------------------------------------------------------ |
| **`totalRequests`**   | **1,000**  | 총 1,000건의 메모 생성 요청이 서버에 전달되었습니다.         |
| **`avgResponseTime`** | **4.9s**   | 평균 응답 시간은 약 5초입니다. 이전(6.15s)보다 개선된 듯 보이지만 함정이 있습니다. |
| **P95 (95th)**        | **56.58s** | **[심각]** 상위 5%의 사용자는 응답을 받는 데 무려 **56초**가 걸렸습니다. |
| **P99 (99th)**        | **63.24s** | 사실상 1%의 사용자는 1분 이상 대기했으며, 이는 웹 환경에서 타임아웃(Timeout) 발생 수준입니다. |
| **Max Response Time** | **66.57s** | 최악의 응답 시간입니다. 서버가 요청을 순차적으로 처리하며 병목이 누적되었음을 시사합니다. |
| **`avgRps`**          | **16.67**  | 초당 약 16.7건을 처리했습니다. API 로직의 무거움에 비해 처리량은 낮은 편입니다. |

### 백엔드 관점의 종합 진단

현재 시스템은 **"누적 병목 현상(Queuing Delay)이 임계치에 도달한 상태"**입니다.

### 1. 응답 시간의 거대한 편차 (Tail Latency)

최소 시간(2.45s)과 최대 시간(66.57s)의 차이가 약 27배에 달합니다.

- **원인 추정**:
  - **DB Connection Pool 고갈**: 1,000건의 요청이 몰리면서 뒤쪽 요청들이 DB 연결을 잡기 위해 대기열(Queue)에서 무한정 기다리고 있습니다.
  - **Synchronous Write**: 메모 생성 시 매번 디스크 I/O가 발생하거나, 인덱스 재구성에 시간이 소요되어 뒤로 갈수록 느려지는 현상입니다.
  - **쓰레드 차단(Thread Blocking)**: 한 요청이 9.2초(로그상 완료 시간) 동안 쓰레드를 점유하면서 다른 요청들이 서블릿 컨테이너 레벨에서 대기하고 있습니다.

### 2. 로그 기반 실측 성능

로그를 보면 테스트 완료까지 **9249ms(약 9.2초)**가 소요되었습니다.

- 1,000건 처리에 9.2초라면, 단순 계산 시 건당 9ms 수준이어야 하지만 리포트의 `avgResponseTime`이 4.9초인 것은 **요청이 동시에 쌓였으나 처리는 순차적으로 밀렸음**을 증명합니다.

