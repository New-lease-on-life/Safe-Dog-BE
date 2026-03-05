# 🐾 반려동물 맞춤형 케어 서비스 - Backend 

본 프로젝트는 반려동물의 일상적인 케어(식사, 배변, 산책, 투약 등)를 다수의 보호자가 공동으로 관리하고 기록할 수 있도록 돕는 서비스의 백엔드 API 서버입니다.

## 🛠 Tech Stack

* **Language & Framework:** Java, Spring Boot
* **Database & ORM:** MySQL, Spring Data JPA, QueryDSL
* **Cache & Session:** Redis
* **Infrastructure:** AWS (EC2, RDS, S3, ECR), GitHub Actions, Docker

---

## 핵심 설계 원칙 (Core Design Principles)

1. **다대다(N:M) 관계 배제 및 단방향 매핑 지향**
   * **왜 사용하는가 (Why):** JPA에서 양방향 연관관계나 `@ManyToMany`를 남발하면 순환 참조 에러와 N+1 쿼리 문제가 발생하기 쉽습니다. 이를 방지하고 중간 테이블에 Role(권한) 필드를 추가하기 위해 `PetGuardian` 매핑 엔티티를 승격시키고 철저히 단방향으로 설계합니다.
   * **시나리오:** 특정 유저의 동물 목록을 조회할 때 User 엔티티에서 리스트를 꺼내지 않고, `PetGuardianRepository`에서 명시적인 JPQL 쿼리를 통해 필요한 데이터만 조회합니다.
2. **비관적 락 대신 낙관적 락(Optimistic Lock) 활용**
   * **왜 사용하는가 (Why):** 트래픽 병목을 일으키는 DB 레벨의 물리적 락(Pessimistic) 대신, 애플리케이션 레벨의 `@Version`을 활용하여 동시성 처리 성능을 높입니다.
3. **스냅샷 패턴 (Snapshot Pattern)**
   * **왜 사용하는가 (Why):** 반복 생성되는 '템플릿'과 매일 생성되는 '일일 기록'을 물리적으로 분리하여, 템플릿 수정 시 과거의 기록이 함께 변조되는 데이터 오염을 막습니다.

----

## ERD 다이어 그램
<img width="1914" height="1525" alt="safeDogBE" src="https://github.com/user-attachments/assets/b1ba4cef-68f2-402e-95ea-8249d15ffb46" />


---

## 패키지 구조(DDD 구조)

```
com.newleaseonlife.SafeDogBe (또는 com.notfoundjerry.petcare)
├── Application.java
├── global                        # 전역 설정 및 공통 모듈 (프레임워크 레벨)
│   ├── config                    # Security, JPA, Redis, Swagger, AWS 등 설정
│   ├── common                    # BaseTimeEntity, ApiResponse 등 공통 포맷
│   ├── error                     # ExceptionHandler, CustomException, ErrorCode
│   ├── security                  # JWT 필터, OAuth2 핸들러, SecurityUtil
│   └── utils                     # DateUtils, UUID 발급기 등 유틸리티
│
├── domain                        # 핵심 비즈니스 로직 (도메인별 완벽 분리)
│   │
│   ├── user                      # 회원 프로필 및 온보딩 도메인
│   │   ├── controller            # UserController (회원가입, 내 정보 조회 등)
│   │   ├── converter             # UserConverter (User <-> UserResponseDto 변환)
│   │   ├── dto                   # Request/Response DTO (record 사용 권장)
│   │   ├── entity                # User, UserStatus(Enum), UserRole(Enum)
│   │   ├── repository            # UserRepository
│   │   └── service               # UserService
│   │
│   ├── auth                      # 인증 및 인가 도메인 (소셜 연동, 토큰 관리)
│   │   ├── controller            # AuthController (로그인, 토큰 재발급)
│   │   ├── converter             # AuthConverter
│   │   ├── dto                   # TokenRequest, TokenResponse 등
│   │   ├── entity                # OAuthAccount, RefreshToken
│   │   ├── repository            # OAuthAccountRepository, RefreshTokenRepository
│   │   └── service               # AuthService, OAuthService
│   │
│   ├── term                      # 약관 동의 도메인
│   │   ├── controller            # TermController
│   │   ├── converter             # TermConverter
│   │   ├── dto                   # TermAgreementDto 등
│   │   ├── entity                # Term, UserTerm
│   │   ├── repository            # TermRepository, UserTermRepository
│   │   └── service               # TermService
│   │
│   ├── pet                       # 반려동물 및 보호자 도메인
│   │   ├── controller            # PetController
│   │   ├── converter             # PetConverter
│   │   ├── dto                   # PetRequestDto, PetResponseDto 등
│   │   ├── entity                # Pet, PetGuardian
│   │   ├── repository            # PetRepository, PetGuardianRepository
│   │   └── service               # PetService (등록, 수정, 다중 보호자 지정 로직)
│   │
│   ├── petnote                   # 반려노트 (날짜별 다이어리) 도메인 [NEW]
│   │   ├── controller            # PetNoteController
│   │   ├── converter             # PetNoteConverter
│   │   ├── dto                   # PetNoteDto
│   │   ├── entity                # PetNote
│   │   ├── repository            # PetNoteRepository
│   │   └── service               # PetNoteService
│   │
│   ├── care                      # 케어 템플릿 및 일일 체크리스트 도메인 (비즈니스 핵심)
│   │   ├── controller            # CareController, ChecklistController
│   │   ├── converter             # CareConverter, ChecklistConverter
│   │   ├── dto                   # CareTemplateDto, DailyChecklistDto 등
│   │   ├── entity                # CareTemplate, DailyChecklist, ChecklistHistoryLog
│   │   ├── repository            # CareTemplateRepository, DailyChecklistRepository 등
│   │   └── service               # CareTemplateService, ChecklistService, CareSchedulerService
│   │
│   └── article                   # 아티클 도메인 (건강 정보, 팁 등 - 예시 유지)
│       ├── controller
│       ├── converter
│       ├── dto
│       ├── entity                # Article
│       ├── repository
│       └── service
│
└── infra                         # 외부 시스템 연동 (인프라스트럭처)
    ├── aws                       # AWS S3 (이미지 업로드) 등
    ├── ai                        # AI API 연동 (추후 확장을 고려한 분리)
    └── notification              # FCM 등 푸시 알림 발송 모듈
```

---

## 📝 기능적 요구사항 (Functional Requirements)

### 1. 유저 및 인증 도메인 (`User`, `Auth`)

- **소셜 로그인 연동:** Google, Kakao, Naver OAuth2를 지원해야 하며, 최초 가입 시 계정 정보(`oauth_accounts`)를 생성한다.
- **JWT 토큰 정책:** 보안을 위해 짧은 주기의 토큰 전략을 사용한다.
  - **Access Token:** 만료 시간 30분.
  - **Refresh Token:** 만료 시간 2주. 생성 시 `refresh_tokens` 테이블에 저장하며, 클라이언트의 토큰 갱신 요청 시 검증 후 재발급한다.
- **온보딩 프로세스:** 소셜 로그인 직후 추가 정보(닉네임 등)가 없는 유저는 `is_onboarding_completed = false` 상태로 관리되며, 온보딩 API 호출 시 `true`로 전환된다.
- **회원 탈퇴 (Soft Delete):** 유저 탈퇴 시 물리적 삭제 대신 `status`를 `WITHDRAWN`으로 변경하고 `withdrawn_at`에 현재 시간을 기록한다. (보안 및 복구 정책 지원)

### 2. 약관 도메인 (`Term`)

- **약관 동의 관리:** 서비스 이용, 개인정보, 마케팅 수신 등의 약관 동의 상태를 기록한다.
- **이력 추적:** 유저가 약관에 동의한 시점을 `agreed_at`으로 기록하여 법적 근거 및 마케팅 활용 데이터로 남긴다.

### 3. 반려동물 도메인 (`Pet`, `PetGuardian`)

- **프로필 관리:** 반려동물의 이름, 종, 품종, 생일, 성별, 중성화 여부를 관리한다.
- **이미지 처리 (AWS S3):** 프로필 이미지는 AWS S3에 업로드 후 반환된 URL(길이를 고려하여 `TEXT` 타입으로 저장)을 엔티티에 저장한다.
- **다중 보호자 시스템:** `pet_guardian`을 통해 한 마리의 반려동물에 여러 유저(OWNER, CAREGIVER 등)가 접근 및 관리할 수 있도록 권한을 분리한다.

### 4. 반려노트 도메인 (`PetNote`)

- **날짜별 다이어리:** 체크리스트와 별개로, 특정 날짜(`note_date`)에 반려동물의 일상이나 특이사항을 텍스트 형태로 자유롭게 기록, 수정, 삭제할 수 있다.

### 5. 케어 및 체크리스트 도메인 (`CareTemplate`, `DailyChecklist`) - 🌟 핵심 도메인

- **케어 템플릿 설정:** 보호자는 식사, 산책, 약 복용 등의 반복적인 케어 항목(`care_template`)을 설정할 수 있다.
- **일일 체크리스트 자동 생성 (Batch):** 매일 자정(KST 00:00), 스케줄러가 활성화된 템플릿(`is_active=true`)을 기반으로 그날의 `daily_checklist`를 자동 생성한다.
- **동시성 제어 (Optimistic Locking):** 다중 보호자가 동시에 동일한 체크리스트 항목을 완료 처리할 경우, 낙관적 락(`@Version`)을 통해 데이터 정합성을 보장하고 충돌 시 예외를 발생시킨다.
- **히스토리 로깅:** 체크리스트의 상태 변경(완료/취소) 액션은 누가 언제 했는지 `checklist_history_log`에 기록된다.

------

## 비기능적 요구사항 (Non-Functional Requirements)

### 1. 성능 및 최적화

- **인덱스 활용:** `user_id`, `pet_id`, `target_date` 등 조회가 빈번한 컬럼에 단일/복합 인덱스를 적용하여 조회 쿼리 성능을 최적화한다.
- **지연 로딩 (Lazy Loading):** 모든 `@ManyToOne` 및 `@OneToMany` 연관관계는 `FetchType.LAZY`로 설정하여 N+1 문제를 방지한다.
- **DTO 및 Converter 계층화:** Controller와 Service 간의 데이터 전송 시 Entity를 직접 노출하지 않고 Converter를 통해 DTO로 변환하여 응답 페이로드를 최적화한다.

### 2. 보안 및 안정성

- **입력값 검증 (Validation):** Controller 계층에서 `@Valid`를 활용하여 프론트엔드로부터 넘어오는 DTO의 필수 값 및 형식을 엄격히 검증한다. (예: 닉네임 길이, 날짜 형식 등)
- **CORS 및 Security:** 프론트엔드 도메인에 대한 CORS 허용 처리 및 JWT 인증 필터를 통해 인가되지 않은 API 접근을 차단한다.

### 3. 백그라운드 작업 (Scheduler/Batch)

- **데이터 정리 정책:** 시스템 부하 방지를 위해 `checklist_history_log` 등 오래된 로그 데이터는 90일이 지나면 스케줄러를 통해 물리적 삭제(Hard Delete)를 수행한다.
