# Calculator 서비스 클래스 설계서

## 개요
- **서비스명**: Calculator (재무 계산)
- **아키텍처 패턴**: Layered Architecture
- **패키지 그룹**: com.dwj.homestarter.calculator
- **설계 일시**: 2025-12-29

## 설계 참조 문서
- API 명세서: `design/backend/api/calculator-service-api.yaml`
- 내부 시퀀스 설계:
  - `design/backend/sequence/inner/calculator-입주후지출계산.puml`
  - `design/backend/sequence/inner/calculator-계산결과조회.puml`
  - `design/backend/sequence/inner/calculator-캐시무효화처리.puml`

## 패키지 구조

```
com.dwj.homestarter.calculator
├── controller
│   └── CalculatorController
├── dto
│   ├── request
│   │   └── HousingExpensesRequest
│   ├── response
│   │   ├── CalculationResultResponse
│   │   ├── CalculationResultListResponse
│   │   ├── CalculationResultListItem
│   │   ├── FinancialStatusDto
│   │   ├── LoanAnalysisDto
│   │   └── AfterMoveInDto
│   └── external
│       ├── UserProfileDto
│       ├── AssetDto
│       ├── HousingDto
│       └── LoanProductDto
├── service
│   ├── ExpenseCalculatorService (interface)
│   ├── ExpenseCalculatorServiceImpl
│   ├── CacheService
│   ├── client
│   │   ├── UserServiceClient (interface)
│   │   ├── AssetServiceClient (interface)
│   │   ├── HousingServiceClient (interface)
│   │   └── LoanServiceClient (interface)
│   └── event
│       ├── AssetEventListener
│       ├── HousingEventListener
│       ├── AssetUpdatedEvent
│       └── HousingUpdatedEvent
├── domain
│   ├── CalculatorDomain
│   ├── CalculationResult
│   ├── EligibilityResult
│   ├── AfterMoveInResult
│   └── ExternalDataBundle
├── repository
│   ├── jpa
│   │   └── CalculatorRepository (interface)
│   └── entity
│       └── CalculationResultEntity
└── config
    ├── RedisConfig
    ├── RabbitMQConfig
    └── FeignClientConfig
```

## 계층별 클래스 상세

### 1. Controller Layer

#### CalculatorController
**책임**: REST API 엔드포인트 처리, 요청/응답 변환, HTTP 프로토콜 처리

**주요 메소드**:
```java
// POST /calculator/housing-expenses - 입주 후 지출 계산
+ calculateHousingExpenses(request: HousingExpensesRequest): ResponseEntity<CalculationResultResponse>

// GET /calculator/results - 계산 결과 목록 조회
+ getCalculationResults(housingId: String, status: String, sortBy: String,
                        sortOrder: String, page: int, size: int):
                        ResponseEntity<CalculationResultListResponse>

// GET /calculator/results/{id} - 계산 결과 상세 조회
+ getCalculationResult(id: String): ResponseEntity<CalculationResultResponse>

// DELETE /calculator/results/{id} - 계산 결과 삭제
+ deleteCalculationResult(id: String): ResponseEntity<Void>
```

**의존성**:
- ExpenseCalculatorService

---

### 2. DTO Layer

#### Request DTOs

**HousingExpensesRequest**
```java
+ housingId: String          // 주택 ID
+ loanProductId: String       // 대출상품 ID
+ loanAmount: Long            // 대출 금액
+ loanTerm: Integer           // 대출 기간 (개월)
+ validate(): void            // 입력값 검증
```

#### Response DTOs

**CalculationResultResponse** (상세 응답)
```java
+ id: String
+ userId: String
+ housingId: String
+ housingName: String
+ loanProductId: String
+ loanProductName: String
+ calculatedAt: LocalDateTime
+ financialStatus: FinancialStatusDto
+ loanAnalysis: LoanAnalysisDto
+ afterMoveIn: AfterMoveInDto
+ status: String  // ELIGIBLE, INELIGIBLE
```

**CalculationResultListResponse** (목록 응답)
```java
+ results: List<CalculationResultListItem>
+ page: Integer
+ size: Integer
+ total: Long
```

**CalculationResultListItem** (목록 항목)
```java
+ id: String
+ housingName: String
+ loanProductName: String
+ calculatedAt: LocalDateTime
+ status: String
+ availableFunds: Long
```

**FinancialStatusDto** (재무 현황)
```java
+ currentAssets: Long          // 현재 순자산
+ estimatedAssets: Long        // 예상자산
+ loanRequired: Long           // 대출필요금액
```

**LoanAnalysisDto** (대출 분석)
```java
+ ltv: Double                  // 계산된 LTV
+ dti: Double                  // 계산된 DTI
+ dsr: Double                  // 계산된 DSR
+ ltvLimit: Double             // LTV 한도
+ dtiLimit: Double             // DTI 한도
+ dsrLimit: Double             // DSR 한도
+ isEligible: Boolean          // 충족 여부
+ ineligibilityReasons: List<String>
+ monthlyPayment: Long         // 월 상환액
```

**AfterMoveInDto** (입주 후 재무상태)
```java
+ assets: Long                 // 입주 후 자산
+ monthlyExpenses: Long        // 월 지출
+ monthlyIncome: Long          // 월 소득
+ availableFunds: Long         // 여유 자금
```

#### External DTOs (외부 서비스 연동)

**UserProfileDto**
```java
+ userId: String
+ birthDate: LocalDate
+ gender: String
+ residence: String
+ workLocation: String
```

**AssetDto**
```java
+ userId: String
+ totalAssets: Long
+ totalLoans: Long
+ monthlyIncome: Long
+ monthlyExpenses: Long
```

**HousingDto**
```java
+ housingId: String
+ name: String
+ type: String
+ price: Long
+ moveInDate: LocalDate
```

**LoanProductDto**
```java
+ loanProductId: String
+ name: String
+ ltvLimit: Double
+ dtiLimit: Double
+ dsrLimit: Double
+ interestRate: Double
+ maxAmount: Long
```

---

### 3. Service Layer

#### ExpenseCalculatorService (Interface)
**책임**: 비즈니스 로직 조율, 트랜잭션 관리, 캐시 전략

**주요 메소드**:
```java
+ calculateHousingExpenses(request: HousingExpensesRequest, userId: String):
                          CalculationResultResponse
+ getCalculationResults(userId: String, housingId: String, status: String,
                       pageable: Pageable): CalculationResultListResponse
+ getCalculationResult(id: String, userId: String): CalculationResultResponse
+ deleteCalculationResult(id: String, userId: String): void
```

#### ExpenseCalculatorServiceImpl
**구현 세부사항**:

**의존성**:
- CalculatorRepository
- CacheService
- UserServiceClient
- AssetServiceClient
- HousingServiceClient
- LoanServiceClient
- CalculatorDomain

**Private 메소드**:
```java
- generateCacheKey(userId: String, housingId: String, loanProductId: String): String
- fetchExternalData(userId: String, housingId: String, loanProductId: String): ExternalDataBundle
- mapToResponse(entity: CalculationResultEntity): CalculationResultResponse
- mapToListItem(entity: CalculationResultEntity): CalculationResultListItem
```

**캐시 전략 (Cache-Aside Pattern)**:
1. 계산 결과 캐시:
   - Key: `calc:{userId}:{housingId}:{loanId}`
   - TTL: 3600초 (1시간)
   - 히트율 목표: 60% 이상

2. 목록 캐시:
   - Key: `calc:list:{userId}:{page}:{size}`
   - TTL: 300초 (5분)

3. 상세 캐시:
   - Key: `calc:detail:{resultId}`
   - TTL: 3600초 (1시간)

#### CacheService
**책임**: Redis 캐시 관리

**주요 메소드**:
```java
+ get(key: String): Optional<Object>
+ set(key: String, value: Object, ttl: Duration): void
+ delete(key: String): void
+ deletePattern(pattern: String): Long
```

#### External Service Clients

**UserServiceClient** (Interface)
```java
+ getUserProfile(userId: String): UserProfileDto
```

**AssetServiceClient** (Interface)
```java
+ getAssetInfo(userId: String): AssetDto
```

**HousingServiceClient** (Interface)
```java
+ getHousingInfo(housingId: String): HousingDto
```

**LoanServiceClient** (Interface)
```java
+ getLoanProduct(loanProductId: String): LoanProductDto
```

#### Event Listeners

**AssetEventListener**
**책임**: Asset 변경 이벤트 수신 및 캐시 무효화

**주요 메소드**:
```java
+ handleAssetUpdated(event: AssetUpdatedEvent): void
- invalidateCacheByUserId(userId: String): void
```

**처리 로직**:
1. AssetUpdated 이벤트 수신
2. userId 추출
3. `calc:{userId}:*` 패턴 캐시 삭제
4. 목록 캐시 삭제
5. ACK 전송

**HousingEventListener**
**책임**: Housing 변경 이벤트 수신 및 캐시 무효화

**주요 메소드**:
```java
+ handleHousingUpdated(event: HousingUpdatedEvent): void
- invalidateCacheByHousingId(housingId: String): void
```

**처리 로직**:
1. HousingUpdated 이벤트 수신
2. housingId 추출
3. `calc:*:{housingId}:*` 패턴 캐시 삭제
4. 영향받는 사용자 식별
5. 사용자별 목록 캐시 삭제
6. ACK 전송

**재시도 정책**:
- 최대 3회 재시도
- Exponential Backoff (1초 → 2초 → 4초)
- 실패 시 DLQ 이동

---

### 4. Domain Layer

#### CalculatorDomain
**책임**: 핵심 재무 계산 로직, 비즈니스 규칙 캡슐화

**주요 메소드**:
```java
+ calculate(user: UserProfileDto, asset: AssetDto, housing: HousingDto,
           loan: LoanProductDto, loanAmount: Long, loanTerm: Integer):
           CalculationResult
- calculateEstimatedAssets(asset: AssetDto, housing: HousingDto): Long
- calculateLoanRequired(housing: HousingDto, estimatedAssets: Long): Long
- calculateLTV(loanRequired: Long, housingPrice: Long): Double
- calculateDTI(monthlyPayment: Long, monthlyIncome: Long): Double
- calculateDSR(monthlyPayment: Long, monthlyIncome: Long, existingLoanPayment: Long): Double
- calculateMonthlyPayment(loanAmount: Long, interestRate: Double, loanTerm: Integer): Long
- checkEligibility(ltv: Double, dti: Double, dsr: Double, loan: LoanProductDto, loanRequired: Long): EligibilityResult
- calculateAfterMoveIn(asset: AssetDto, housing: HousingDto, monthlyPayment: Long,
                      estimatedAssets: Long, loanRequired: Long): AfterMoveInResult
```

**계산 로직 상세**:

1. **예상자산 계산**:
```
estimatedAssets = currentAssets
                + (monthlyIncome - monthlyExpense) × months
                - totalLoans
```

2. **대출필요금액 계산**:
```
loanRequired = housingPrice - estimatedAssets
```

3. **LTV 계산** (Loan To Value):
```
LTV = (loanRequired / housingPrice) × 100
```

4. **DTI 계산** (Debt To Income):
```
DTI = (연간대출원리금 / 연소득) × 100
```

5. **DSR 계산** (Debt Service Ratio):
```
DSR = (연간총부채원리금 / 연소득) × 100
```

6. **월 상환액 계산** (원리금균등상환):
```
monthlyPayment = loanAmount ×
                (monthlyRate × (1 + monthlyRate)^months) /
                ((1 + monthlyRate)^months - 1)
```

7. **적격성 판단**:
```
isEligible = (LTV ≤ ltvLimit) AND
             (DTI ≤ dtiLimit) AND
             (DSR ≤ dsrLimit) AND
             (loanRequired ≤ maxAmount)
```

8. **입주 후 재무상태**:
```
afterMoveInAssets = estimatedAssets - loanRequired
afterMoveInExpenses = monthlyExpense + monthlyPayment
availableFunds = monthlyIncome - afterMoveInExpenses
```

#### Domain 모델 클래스

**CalculationResult**
```java
+ estimatedAssets: Long
+ loanRequired: Long
+ ltv: Double
+ dti: Double
+ dsr: Double
+ isEligible: Boolean
+ ineligibilityReasons: List<String>
+ monthlyPayment: Long
+ afterMoveInAssets: Long
+ afterMoveInMonthlyExpenses: Long
+ afterMoveInMonthlyIncome: Long
+ afterMoveInAvailableFunds: Long
```

**EligibilityResult**
```java
+ isEligible: Boolean
+ reasons: List<String>
```

**AfterMoveInResult**
```java
+ assets: Long
+ monthlyExpenses: Long
+ monthlyIncome: Long
+ availableFunds: Long
```

**ExternalDataBundle**
```java
+ user: UserProfileDto
+ asset: AssetDto
+ housing: HousingDto
+ loan: LoanProductDto
```

---

### 5. Repository Layer

#### CalculatorRepository (Interface)
**책임**: 데이터 영속성, CRUD 작업

**주요 메소드**:
```java
+ save(entity: CalculationResultEntity): CalculationResultEntity
+ findById(id: String): Optional<CalculationResultEntity>
+ findByUserId(userId: String, pageable: Pageable): Page<CalculationResultEntity>
+ findByUserIdAndHousingId(userId: String, housingId: String, pageable: Pageable): Page<CalculationResultEntity>
+ findByUserIdAndStatus(userId: String, status: String, pageable: Pageable): Page<CalculationResultEntity>
+ findByIdAndUserId(id: String, userId: String): Optional<CalculationResultEntity>
+ findUserIdsByHousingId(housingId: String): List<String>
+ deleteById(id: String): void
```

#### CalculationResultEntity
**테이블**: calculation_results

**필드**:
```java
@Id
+ id: String                           // 계산 결과 ID (UUID)

// 기본 정보
+ userId: String                       // 사용자 ID
+ housingId: String                    // 주택 ID
+ housingName: String                  // 주택 이름
+ loanProductId: String                // 대출상품 ID
+ loanProductName: String              // 대출상품 이름
+ loanAmount: Long                     // 대출 금액
+ loanTerm: Integer                    // 대출 기간

// 재무 현황
+ currentAssets: Long                  // 현재 순자산
+ estimatedAssets: Long                // 예상자산
+ loanRequired: Long                   // 대출필요금액

// 대출 분석
+ ltv: Double                          // 계산된 LTV
+ dti: Double                          // 계산된 DTI
+ dsr: Double                          // 계산된 DSR
+ ltvLimit: Double                     // LTV 한도
+ dtiLimit: Double                     // DTI 한도
+ dsrLimit: Double                     // DSR 한도
+ isEligible: Boolean                  // 충족 여부
+ ineligibilityReasons: String         // 미충족 사유 (JSON)
+ monthlyPayment: Long                 // 월 상환액

// 입주 후 재무상태
+ afterMoveInAssets: Long              // 입주 후 자산
+ afterMoveInMonthlyExpenses: Long     // 입주 후 월지출
+ afterMoveInMonthlyIncome: Long       // 월소득
+ afterMoveInAvailableFunds: Long      // 여유자금

// 상태
+ status: String                       // ELIGIBLE, INELIGIBLE

// 시간
+ calculatedAt: LocalDateTime          // 계산 일시
+ createdAt: LocalDateTime             // 생성 일시
+ updatedAt: LocalDateTime             // 수정 일시
```

---

### 6. Config Layer

#### RedisConfig
**책임**: Redis 연결 및 템플릿 설정

**주요 메소드**:
```java
+ redisTemplate(): RedisTemplate<String, Object>
+ redisConnectionFactory(): RedisConnectionFactory
```

**설정**:
- Host: localhost
- Port: 6379
- Serializer: JSON (Jackson)

#### RabbitMQConfig
**책임**: RabbitMQ 큐 및 바인딩 설정

**주요 메소드**:
```java
+ assetEventsQueue(): Queue
+ housingEventsQueue(): Queue
+ assetEventsBinding(): Binding
+ housingEventsBinding(): Binding
```

**큐 설정**:
- Asset Events Queue: `calculator.asset.events`
  - Exchange: `asset.events`
  - Routing Key: `asset.updated`

- Housing Events Queue: `calculator.housing.events`
  - Exchange: `housing.events`
  - Routing Key: `housing.updated`

#### FeignClientConfig
**책임**: Feign 클라이언트 설정 (외부 서비스 연동)

**주요 메소드**:
```java
+ userServiceClient(): UserServiceClient
+ assetServiceClient(): AssetServiceClient
+ housingServiceClient(): HousingServiceClient
+ loanServiceClient(): LoanServiceClient
```

---

## Controller 메소드 - API 매핑표

| 메소드명 | HTTP Method | URI Path | 설명 |
|---------|------------|----------|------|
| calculateHousingExpenses() | POST | /calculator/housing-expenses | 입주 후 지출 계산 |
| getCalculationResults() | GET | /calculator/results | 계산 결과 목록 조회 |
| getCalculationResult() | GET | /calculator/results/{id} | 계산 결과 상세 조회 |
| deleteCalculationResult() | DELETE | /calculator/results/{id} | 계산 결과 삭제 |

### API 상세 매핑

#### 1. calculateHousingExpenses()
**엔드포인트**: `POST /calculator/housing-expenses`

**Request**: HousingExpensesRequest
- housingId: String
- loanProductId: String
- loanAmount: Long
- loanTerm: Integer

**Response**: CalculationResultResponse (201 Created)

**비즈니스 로직**:
1. 캐시 조회 (`calc:{userId}:{housingId}:{loanId}`)
2. 캐시 미스 시:
   - 외부 서비스 병렬 호출 (User, Asset, Housing, Loan)
   - Domain 계산 수행 (LTV/DTI/DSR)
   - 결과 DB 저장
   - 캐시 저장 (TTL: 1시간)
3. 계산 결과 반환

---

#### 2. getCalculationResults()
**엔드포인트**: `GET /calculator/results`

**Query Parameters**:
- housingId: String (optional)
- status: String (optional, ELIGIBLE/INELIGIBLE)
- sortBy: String (default: calculatedAt)
- sortOrder: String (default: desc)
- page: Integer (default: 0)
- size: Integer (default: 20)

**Response**: CalculationResultListResponse (200 OK)

**비즈니스 로직**:
1. 목록 캐시 조회 (`calc:list:{userId}:{page}:{size}`)
2. 캐시 미스 시:
   - Repository 페이징 조회
   - 목록 캐시 저장 (TTL: 5분)
3. 목록 반환

---

#### 3. getCalculationResult()
**엔드포인트**: `GET /calculator/results/{id}`

**Path Parameter**: id (String)

**Response**: CalculationResultResponse (200 OK)

**비즈니스 로직**:
1. 상세 캐시 조회 (`calc:detail:{resultId}`)
2. 캐시 미스 시:
   - Repository 조회
   - 상세 캐시 저장 (TTL: 1시간)
3. 상세 정보 반환

---

#### 4. deleteCalculationResult()
**엔드포인트**: `DELETE /calculator/results/{id}`

**Path Parameter**: id (String)

**Response**: 204 No Content

**비즈니스 로직**:
1. 권한 확인 (본인 결과만 삭제 가능)
2. Repository 삭제
3. 관련 캐시 무효화:
   - `calc:detail:{resultId}`
   - `calc:list:{userId}:*` (패턴 매칭)

---

## 캐시 무효화 전략

### 무효화 시나리오

1. **Asset 변경 시**:
   - 패턴: `calc:{userId}:*`
   - 사용자의 모든 계산 결과 캐시 삭제
   - 목록 캐시 삭제

2. **Housing 변경 시**:
   - 패턴: `calc:*:{housingId}:*`
   - 해당 주택 관련 모든 계산 결과 캐시 삭제
   - 영향받는 사용자 식별 후 목록 캐시 삭제

3. **신규 계산 시**:
   - 목록 캐시 무효화 (`calc:list:{userId}:*`)

4. **결과 삭제 시**:
   - 상세 캐시 삭제 (`calc:detail:{resultId}`)
   - 목록 캐시 삭제 (`calc:list:{userId}:*`)

---

## 성능 최적화 전략

### 1. 병렬 데이터 수집
외부 서비스 호출을 병렬로 처리하여 응답 시간 단축:
- User Service
- Asset Service
- Housing Service
- Loan Service

**예상 효과**: 5초 → 2초 (60% 단축)

### 2. 캐시 전략
- Cache-Aside 패턴 적용
- 계산 결과 캐시 히트율 목표: 60% 이상
- 평균 응답시간:
  - 캐시 히트: 0.1초
  - 캐시 미스: 2초

### 3. 데이터베이스 인덱스
```sql
CREATE INDEX idx_user_id ON calculation_results(user_id);
CREATE INDEX idx_housing_id ON calculation_results(housing_id);
CREATE INDEX idx_user_status ON calculation_results(user_id, status);
CREATE INDEX idx_calculated_at ON calculation_results(calculated_at DESC);
```

---

## 에러 처리

### 예외 종류
1. **NotFoundException**: 계산 결과를 찾을 수 없음
2. **ForbiddenException**: 권한 없음 (타인의 결과 접근)
3. **BadRequestException**: 잘못된 요청 데이터
4. **ExternalServiceException**: 외부 서비스 호출 실패
5. **CacheException**: 캐시 작업 실패

### 재시도 정책
- 외부 서비스 호출: 최대 3회, Exponential Backoff
- 캐시 작업: 최대 3회, 1초 → 2초 → 4초
- 이벤트 처리: 최대 3회, 실패 시 DLQ 이동

---

## 모니터링 지표

### 성능 지표
- 평균 응답시간: < 1초 (캐시 히트), < 3초 (캐시 미스)
- 캐시 히트율: > 60%
- 외부 서비스 호출 시간: < 500ms (병렬)

### 신뢰성 지표
- 이벤트 처리 성공률: > 99.9%
- 캐시 무효화 성공률: > 99.9%
- 재시도 발생률: < 1%
- DLQ 이동률: < 0.1%

### 알림 조건
- Redis 연결 실패 지속 (1분 이상)
- DLQ 메시지 누적 (10개 이상)
- 캐시 무효화 실패율 급증 (> 5%)
- 외부 서비스 호출 실패율 급증 (> 10%)

---

## 산출물

### PlantUML 다이어그램
1. **전체 클래스 설계**: `design/backend/class/calculator.puml`
   - 모든 클래스, 메소드, 필드 상세
   - 계층 간 의존성 관계
   - 상세 주석 및 설명

2. **간소화 버전**: `design/backend/class/calculator-simple.puml`
   - 핵심 클래스 및 관계
   - Controller 메소드 - API 매핑표
   - 계층 구조 설명
   - 캐시 전략 설명
   - 이벤트 기반 무효화 설명
   - Domain 계산 로직 설명

---

## 구현 순서 권고

1. **Phase 1: 기본 구조**
   - Entity, DTO 클래스
   - Repository 인터페이스
   - Config 클래스

2. **Phase 2: Domain 로직**
   - CalculatorDomain 구현
   - 계산 로직 단위 테스트

3. **Phase 3: Service 구현**
   - External Service Clients (Feign)
   - ExpenseCalculatorServiceImpl
   - CacheService

4. **Phase 4: Controller**
   - CalculatorController
   - API 통합 테스트

5. **Phase 5: 이벤트 처리**
   - Event Listeners
   - RabbitMQ 설정
   - 캐시 무효화 로직

6. **Phase 6: 최적화 및 모니터링**
   - 성능 테스트
   - 캐시 히트율 측정
   - 모니터링 대시보드 구축
