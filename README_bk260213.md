# HomeStarter - 내집마련 도우미 플랫폼

## 프로젝트 주제

사회초년생과 신혼부부를 위한 **내집마련 도우미 플랫폼**입니다.
사용자의 재무 상태를 분석하고, 대출 적격 여부를 판단하며, AI 기반의 생애주기별 장기주거 로드맵을 제공합니다.

## 프로젝트 목적

- 사회초년생과 신혼부부가 **니즈에 맞는 첫 집을 쉽게 선택**할 수 있도록 지원
- 본인/배우자의 자산, 소득, 지출을 종합적으로 관리하고 분석
- LTV/DTI/DSR 등 **재무 계산을 자동화**하여 대출 충족 여부를 판단
- AI(LLM)를 활용한 **생애주기별 장기주거 로드맵** 설계
- 복수의 입주희망 주택을 비교 분석하여 최적의 선택을 지원

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| Database | PostgreSQL | - |
| Cache | Redis | - |
| Message Queue | Kafka | - |
| API Documentation | SpringDoc OpenAPI | 2.5.0 |
| Authentication | JWT (jjwt) | 0.12.5 |
| Object Mapping | MapStruct | 1.5.5.Final |
| HTTP Client | OpenFeign | 13.1 |
| Resilience | Resilience4j | - |
| Build | Gradle | - |

## 서비스 아키텍처 (MSA Outer Architecture)

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Client (Browser)                           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     API Gateway (Gateway Routing)                   │
│              JWT 인증 · 로깅 · Rate Limiting · 라우팅                │
└──┬──────┬──────┬──────┬──────┬──────┬───────────────────────────────┘
   │      │      │      │      │      │
   ▼      ▼      ▼      ▼      ▼      ▼
┌──────┐┌──────┐┌──────┐┌───────┐┌──────────┐┌─────────┐
│ User ││Asset ││ Loan ││Housing││Calculator││ Roadmap │
│ :8081││      ││      ││       ││          ││         │
└──┬───┘└──┬───┘└──┬───┘└──┬────┘└────┬─────┘└────┬────┘
   │       │       │       │          │            │
   │       │       │       │    ┌─────┴─────┐      │
   │       │       │       │    │ REST 동기  │      │
   │       │       │       │    │ (Cache-    │      │
   │       │       │       │    │  Aside)    │      │
   │       │       │       │    └─────┬─────┘      │
   │       │       │       │          │            │
   │       ├───────┼───────┤          │            │
   │       │  Pub/Sub Event│          │            │
   │       │  (캐시 무효화) │          │            │
   ▼       ▼       ▼       ▼          ▼            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                            │
│                                                                     │
│  ┌──────────┐   ┌──────────────┐   ┌──────────────────────────┐    │
│  │PostgreSQL│   │    Redis     │   │     Kafka (MQ)           │    │
│  │(서비스별 │   │  (Cache-     │   │ · Pub/Sub 이벤트 전파    │    │
│  │ 독립 DB) │   │   Aside)     │   │ · Async Request-Reply    │    │
│  └──────────┘   └──────────────┘   └──────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                                         ▼
   ┌──────────────┐                        ┌──────────────┐
   │ 카카오맵 API  │                        │  LLM API     │
   │ (주소 검색)   │                        │ (Claude/GPT) │
   │ Circuit       │                        │ Circuit      │
   │ Breaker+Retry │                        │ Breaker+Retry│
   └──────────────┘                        └──────────────┘
```

### 적용 아키텍처 패턴

| 패턴 | 적용 서비스 | 설명 |
|------|------------|------|
| **Gateway Routing** | API Gateway | 단일 진입점, JWT 인증, 로깅, Rate Limiting |
| **Cache-Aside** | Calculator | Redis 캐시 우선 조회, 캐시 히트율 60% 목표 |
| **Publisher-Subscriber** | Asset, Housing → Calculator | 데이터 변경 이벤트 발행 → 캐시 무효화 |
| **Async Request-Reply** | Roadmap | AI 로드맵 비동기 생성, SSE 실시간 진행 상황 전달 |
| **Circuit Breaker** | Housing, Roadmap | 외부 API 장애 격리 (카카오맵, LLM) |
| **Retry** | Housing, Roadmap | 일시적 오류 복구, 지수 백오프 (1s→2s→4s) |

### 서비스 간 통신

- **동기 (REST)**: Calculator → User, Asset, Housing, Loan (데이터 조회)
- **비동기 (Kafka)**: Roadmap 생성 작업 등록 및 처리
- **이벤트 (Pub/Sub)**: Asset/Housing 변경 → Calculator 캐시 무효화

---

## 서비스별 소스코드 구조

### user-service

사용자 인증/인가 및 프로필 관리

```
user-service/src/main/java/com/dwj/homestarter/user/
├── UserApplication.java
├── config/
│   └── SwaggerConfig.java
├── controller/
│   └── UserController.java
├── service/
│   ├── UserService.java
│   └── jwt/
│       ├── JwtTokenProvider.java
│       └── UserPrincipal.java
├── repository/
│   ├── jpa/
│   │   ├── UserRepository.java
│   │   └── UserProfileRepository.java
│   └── entity/
│       ├── UserEntity.java
│       ├── AddressEmbeddable.java
│       ├── Gender.java
│       └── InvestmentPropensity.java
├── dto/
│   ├── request/
│   │   ├── UserRegisterRequest.java
│   │   ├── UserLoginRequest.java
│   │   └── AddressRequest.java
│   └── response/
│       ├── UserRegisterResponse.java
│       ├── UserLoginResponse.java
│       └── AddressResponse.java
└── domain/
```

**주요 기능**: 회원가입/로그인, JWT 토큰 발급, 사용자 프로필 CRUD, Redis 세션 관리

---

### asset-service

본인/배우자 자산정보 관리 (자산, 대출, 소득, 지출)

```
asset-service/src/main/java/com/dwj/homestarter/asset/
├── AssetApplication.java
├── config/
├── controller/
├── service/
├── domain/
│   ├── Asset.java
│   ├── AssetItem.java
│   ├── LoanItem.java
│   ├── IncomeItem.java
│   ├── ExpenseItem.java
│   ├── AssetSummary.java
│   ├── AssetUpdatedEvent.java
│   └── OwnerType.java
├── dto/
│   ├── request/
│   │   ├── CreateAssetRequest.java
│   │   └── UpdateAssetRequest.java
│   └── response/
├── event/
├── repository/
│   ├── jpa/
│   │   ├── AssetItemRepository.java
│   │   ├── LoanItemRepository.java
│   │   ├── IncomeItemRepository.java
│   │   └── ExpenseItemRepository.java
│   └── entity/
│       ├── AssetEntity.java
│       ├── AssetItemEntity.java
│       ├── LoanItemEntity.java
│       ├── IncomeItemEntity.java
│       └── ExpenseItemEntity.java
```

**주요 기능**: 복수 자산항목 CRUD (1:N), 본인/배우자 구분 관리, 변경 이벤트 발행 (Pub/Sub)

---

### loan-service

대출상품 정보 관리

```
loan-service/src/main/java/com/dwj/homestarter/loan/
├── LoanApplication.java
├── config/
├── controller/
├── service/
│   └── LoanProductService.java
├── domain/
├── dto/
│   ├── LoanProductDTO.java
│   ├── LoanProductResponse.java
│   ├── CreateLoanProductRequest.java
│   ├── UpdateLoanProductRequest.java
│   ├── LoanProductListResponse.java
│   ├── LoanProductListData.java
│   └── PageInfo.java
└── repository/
```

**주요 기능**: 대출상품 CRUD, 페이지네이션 조회, 상품 필터링

---

### housing-service

입주희망 주택정보 및 출퇴근 정보 관리

```
housing-service/src/main/java/com/dwj/homestarter/housing/
├── HousingApplication.java
├── config/
├── controller/
├── service/
├── domain/
│   ├── Address.java
│   └── enums/
│       ├── HousingType.java
│       ├── SunlightLevel.java
│       ├── NoiseLevel.java
│       └── TransportType.java
├── dto/
│   ├── request/
│   │   ├── AddressRequest.java
│   │   ├── CommuteTimeRequest.java
│   │   └── TransportationRequest.java
│   └── response/
│       ├── AddressResponse.java
│       ├── CommuteTimeResponse.java
│       ├── TransportationResponse.java
│       ├── HousingListResponse.java
│       ├── HousingListItem.java
│       └── GoalHousingResponse.java
├── event/
├── exception/
├── infrastructure/
└── repository/
    ├── jpa/
    │   ├── HousingRepository.java
    │   └── TransportationRepository.java
    └── entity/
        ├── CommuteTimeEntity.java
        └── TransportationEntity.java
```

**주요 기능**: 주택정보 CRUD, 교통수단별 출퇴근 시간 관리 (중첩 데이터), 최종목표 주택 선택, 변경 이벤트 발행

---

### calculator-service

재무 계산 및 대출 적격 여부 판단

```
calculator-service/src/main/java/com/dwj/homestarter/calculator/
├── CalculatorApplication.java
├── config/
├── controller/
├── service/
│   ├── CacheService.java
│   ├── ExpenseCalculatorService.java
│   └── ...
├── domain/
│   ├── ExternalDataBundle.java
│   ├── EligibilityResult.java
│   ├── AfterMoveInResult.java
│   └── CalculationResult.java
├── dto/
│   ├── request/
│   │   └── HousingExpensesRequest.java
│   ├── response/
│   │   ├── FinancialStatusDto.java
│   │   ├── LoanAnalysisDto.java
│   │   └── CalculationResultListResponse.java
│   └── external/
│       ├── UserProfileDto.java
│       ├── AssetDto.java
│       ├── HousingDto.java
│       └── LoanProductDto.java
└── repository/
    └── jpa/
        └── CalculatorRepository.java
```

**주요 기능**: LTV/DTI/DSR 재무 계산, 4개 서비스 데이터 통합 조회, Cache-Aside 패턴 (Redis), 이벤트 구독으로 캐시 무효화

---

### roadmap-service

AI 기반 생애주기별 장기주거 로드맵 생성

```
roadmap-service/src/main/java/com/dwj/homestarter/roadmap/
├── RoadmapApplication.java
├── client/                          # Feign 클라이언트 (서비스 간 통신)
├── config/
├── controller/
├── service/
├── domain/
├── dto/
├── worker/                          # 비동기 AI 처리 워커
├── exception/
├── repository/
│   └── jpa/
│       ├── RoadmapStageRepository.java
│       └── ExecutionGuideRepository.java
└── entity/
    ├── EventType.java
    ├── RoadmapStatus.java
    ├── TaskStatus.java
    ├── LifecycleEventEntity.java
    ├── RoadmapEntity.java
    ├── RoadmapStageEntity.java
    ├── ExecutionGuideEntity.java
    └── RoadmapTaskEntity.java
```

**주요 기능**: 비동기 로드맵 생성 (Async Request-Reply), Feign 클라이언트로 전체 서비스 데이터 통합, LLM API 호출 (Circuit Breaker), SSE 실시간 진행 상황 스트리밍

---

### common (공통 모듈)

```
common/src/main/java/com/dwj/homestarter/common/
├── dto/
│   ├── ApiResponse.java
│   └── ErrorResponse.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── BusinessException.java
    ├── UnauthorizedException.java
    ├── NotFoundException.java
    └── ValidationException.java
```

**역할**: 공통 API 응답 포맷, 전역 예외 처리, 공통 DTO
