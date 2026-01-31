# Asset 서비스 클래스 설계서

## 1. 개요

### 1.1 설계 패턴
- **아키텍처 패턴**: Layered Architecture
- **패키지 그룹**: com.dwj.homestarter.asset
- **데이터베이스**: JPA/Hibernate 기반
- **이벤트**: Kafka 기반 비동기 이벤트 발행

### 1.2 레이어 구조
```
Controller Layer (API 엔드포인트)
    ↓
Service Layer (비즈니스 로직)
    ↓
Domain Layer (도메인 모델)
    ↓
Repository Layer (데이터 접근)
    ↓
Entity Layer (JPA 엔티티)
```

## 2. 클래스 설계

### 2.1 Controller Layer

#### SelfAssetController
본인 자산정보 관리 컨트롤러

**메소드**:
- `createSelfAssets(request: CreateAssetRequest): ResponseEntity<AssetResponse>`
  - API: `POST /assets/self`
  - 설명: 본인 자산정보 입력
  - Operation ID: createSelfAssets

#### SpouseAssetController
배우자 자산정보 관리 컨트롤러

**메소드**:
- `createSpouseAssets(request: CreateAssetRequest): ResponseEntity<AssetResponse>`
  - API: `POST /assets/spouse`
  - 설명: 배우자 자산정보 입력
  - Operation ID: createSpouseAssets

#### AssetController
자산정보 조회/수정/삭제 컨트롤러

**메소드**:
- `getAssets(ownerType: String): ResponseEntity<AssetListResponse>`
  - API: `GET /assets`
  - 설명: 자산정보 조회
  - Operation ID: getAssets

- `updateAsset(id: String, request: UpdateAssetRequest): ResponseEntity<AssetResponse>`
  - API: `PUT /assets/{id}`
  - 설명: 자산정보 수정
  - Operation ID: updateAsset

- `deleteAsset(id: String): ResponseEntity<Void>`
  - API: `DELETE /assets/{id}`
  - 설명: 자산정보 삭제
  - Operation ID: deleteAsset

### 2.2 Service Layer

#### AssetService (Interface)
자산정보 관리 서비스 인터페이스

**메소드**:
- `createSelfAssets(userId: String, request: CreateAssetRequest): AssetResponse`
- `createSpouseAssets(userId: String, request: CreateAssetRequest): AssetResponse`
- `getAssets(userId: String, ownerType: OwnerType): AssetListResponse`
- `updateAsset(id: String, userId: String, request: UpdateAssetRequest): AssetResponse`
- `deleteAsset(id: String, userId: String): void`
- `calculateTotals(userId: String, ownerType: OwnerType): AssetSummary`

#### AssetServiceImpl
자산정보 관리 서비스 구현

**의존성**:
- AssetRepository
- AssetItemRepository
- LoanItemRepository
- IncomeItemRepository
- ExpenseItemRepository
- ValidationService
- AssetEventPublisher

**주요 메소드**:
- `checkDuplicateAsset(userId: String, ownerType: OwnerType): void`
  - 중복 자산정보 체크
- `calculateAssetTotals(items: List<AssetItemEntity>): AssetTotals`
  - 자산 총액 계산
- `publishAssetUpdatedEvent(userId: String, ownerType: OwnerType, changeType: String): void`
  - 자산 변경 이벤트 발행

#### ValidationService
자산정보 검증 서비스

**메소드**:
- `validateAssetItem(request: CreateAssetRequest): void`
- `validateUpdateRequest(request: UpdateAssetRequest): void`
- `validateItemName(name: String): void`
- `validateAmount(amount: Long): void`

#### AssetEventPublisher
자산 이벤트 발행 서비스

**메소드**:
- `publishAssetUpdated(event: AssetUpdatedEvent): void`
- `buildEvent(userId: String, ownerType: OwnerType, changeType: String, summary: AssetSummary): AssetUpdatedEvent`

### 2.3 Domain Layer

#### Asset
자산정보 도메인 모델

**속성**:
- id: String - 자산정보 ID
- userId: String - 사용자 ID
- ownerType: OwnerType - 소유자 유형 (SELF/SPOUSE)
- totalAssets: Long - 총 자산액
- totalLoans: Long - 총 대출액
- totalMonthlyIncome: Long - 총 월소득
- totalMonthlyExpense: Long - 총 월지출
- netAssets: Long - 순자산
- monthlyAvailableFunds: Long - 월 가용자금
- createdAt: LocalDateTime - 생성일시
- updatedAt: LocalDateTime - 수정일시

**메소드**:
- `calculateNetAssets(): Long` - 순자산 계산 (총 자산 - 총 대출)
- `calculateMonthlyAvailableFunds(): Long` - 월 가용자금 계산 (총 월소득 - 총 월지출)

#### AssetItem
자산 항목 도메인 모델

**속성**:
- id: String
- name: String
- amount: Long

#### LoanItem
대출 항목 도메인 모델

**속성**:
- id: String
- name: String
- amount: Long

#### IncomeItem
월소득 항목 도메인 모델

**속성**:
- id: String
- name: String
- amount: Long

#### ExpenseItem
월지출 항목 도메인 모델

**속성**:
- id: String
- name: String
- amount: Long

#### OwnerType (Enum)
소유자 유형

**값**:
- SELF - 본인
- SPOUSE - 배우자

#### AssetSummary
자산 요약 정보

**속성**:
- totalAssets: Long
- totalLoans: Long
- totalMonthlyIncome: Long
- totalMonthlyExpense: Long
- netAssets: Long
- monthlyAvailableFunds: Long

#### AssetUpdatedEvent
자산 변경 이벤트

**속성**:
- eventId: String
- eventType: String
- timestamp: LocalDateTime
- userId: String
- changeType: String
- ownerType: OwnerType
- summary: AssetSummary

### 2.4 DTO Layer

#### CreateAssetRequest
자산정보 생성 요청

**속성**:
- assets: List<AssetItemDto>
- loans: List<LoanItemDto>
- monthlyIncomes: List<IncomeItemDto>
- monthlyExpenses: List<ExpenseItemDto>

#### UpdateAssetRequest
자산정보 수정 요청

**속성**:
- assets: List<AssetItemDto>
- loans: List<LoanItemDto>
- monthlyIncomes: List<IncomeItemDto>
- monthlyExpenses: List<ExpenseItemDto>

#### AssetResponse
자산정보 응답

**속성**:
- userId: String
- ownerType: String
- assets: List<AssetItemDto>
- loans: List<LoanItemDto>
- monthlyIncomes: List<IncomeItemDto>
- monthlyExpenses: List<ExpenseItemDto>
- totalAssets: Long
- totalLoans: Long
- totalMonthlyIncome: Long
- totalMonthlyExpense: Long
- netAssets: Long
- monthlyAvailableFunds: Long
- createdAt: String
- updatedAt: String

#### AssetListResponse
자산정보 목록 응답

**속성**:
- assets: List<AssetResponse>
- combinedSummary: CombinedAssetSummaryDto

#### CombinedAssetSummaryDto
가구 전체 자산 요약

**속성**:
- totalAssets: Long
- totalLoans: Long
- totalMonthlyIncome: Long
- totalMonthlyExpense: Long
- netAssets: Long
- monthlyAvailableFunds: Long

### 2.5 Repository Layer

#### AssetRepository
자산정보 리포지토리

**메소드**:
- `findByUserIdAndOwnerType(userId: String, ownerType: OwnerType): Optional<AssetEntity>`
- `findByUserId(userId: String): List<AssetEntity>`
- `existsByUserIdAndOwnerType(userId: String, ownerType: OwnerType): boolean`
- `save(entity: AssetEntity): AssetEntity`
- `deleteById(id: String): void`

#### AssetItemRepository
자산 항목 리포지토리

**메소드**:
- `findByAssetId(assetId: String): List<AssetItemEntity>`
- `save(entity: AssetItemEntity): AssetItemEntity`
- `saveAll(entities: List<AssetItemEntity>): List<AssetItemEntity>`
- `deleteByAssetId(assetId: String): void`

#### LoanItemRepository
대출 항목 리포지토리

**메소드**:
- `findByAssetId(assetId: String): List<LoanItemEntity>`
- `save(entity: LoanItemEntity): LoanItemEntity`
- `saveAll(entities: List<LoanItemEntity>): List<LoanItemEntity>`
- `deleteByAssetId(assetId: String): void`

#### IncomeItemRepository
월소득 항목 리포지토리

**메소드**:
- `findByAssetId(assetId: String): List<IncomeItemEntity>`
- `save(entity: IncomeItemEntity): IncomeItemEntity`
- `saveAll(entities: List<IncomeItemEntity>): List<IncomeItemEntity>`
- `deleteByAssetId(assetId: String): void`

#### ExpenseItemRepository
월지출 항목 리포지토리

**메소드**:
- `findByAssetId(assetId: String): List<ExpenseItemEntity>`
- `save(entity: ExpenseItemEntity): ExpenseItemEntity`
- `saveAll(entities: List<ExpenseItemEntity>): List<ExpenseItemEntity>`
- `deleteByAssetId(assetId: String): void`

### 2.6 Entity Layer

#### AssetEntity
자산정보 JPA 엔티티

**속성**:
- id: String (PK)
- userId: String
- ownerType: String
- totalAssets: Long
- totalLoans: Long
- totalMonthlyIncome: Long
- totalMonthlyExpense: Long
- netAssets: Long
- monthlyAvailableFunds: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

**메소드**:
- `toDomain(): Asset` - 도메인 객체로 변환

#### AssetItemEntity
자산 항목 JPA 엔티티

**속성**:
- id: String (PK)
- assetId: String (FK)
- name: String
- amount: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

#### LoanItemEntity
대출 항목 JPA 엔티티

**속성**:
- id: String (PK)
- assetId: String (FK)
- name: String
- amount: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

#### IncomeItemEntity
월소득 항목 JPA 엔티티

**속성**:
- id: String (PK)
- assetId: String (FK)
- name: String
- amount: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

#### ExpenseItemEntity
월지출 항목 JPA 엔티티

**속성**:
- id: String (PK)
- assetId: String (FK)
- name: String
- amount: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime

## 3. 패키지 구조

```
com.dwj.homestarter.asset
├── controller
│   ├── SelfAssetController.java
│   ├── SpouseAssetController.java
│   └── AssetController.java
├── service
│   ├── AssetService.java (interface)
│   ├── AssetServiceImpl.java
│   ├── ValidationService.java
│   └── AssetEventPublisher.java
├── domain
│   ├── Asset.java
│   ├── AssetItem.java
│   ├── LoanItem.java
│   ├── IncomeItem.java
│   ├── ExpenseItem.java
│   ├── OwnerType.java (enum)
│   ├── AssetSummary.java
│   └── AssetUpdatedEvent.java
├── dto
│   ├── CreateAssetRequest.java
│   ├── UpdateAssetRequest.java
│   ├── AssetResponse.java
│   ├── AssetListResponse.java
│   ├── AssetItemDto.java
│   ├── LoanItemDto.java
│   ├── IncomeItemDto.java
│   ├── ExpenseItemDto.java
│   └── CombinedAssetSummaryDto.java
├── repository
│   ├── jpa
│   │   ├── AssetRepository.java
│   │   ├── AssetItemRepository.java
│   │   ├── LoanItemRepository.java
│   │   ├── IncomeItemRepository.java
│   │   └── ExpenseItemRepository.java
│   └── entity
│       ├── AssetEntity.java
│       ├── AssetItemEntity.java
│       ├── LoanItemEntity.java
│       ├── IncomeItemEntity.java
│       └── ExpenseItemEntity.java
└── config
    ├── KafkaConfig.java
    └── JpaConfig.java
```

## 4. API 매핑표

### 4.1 SelfAssetController

| 메소드 | HTTP 메소드 | API 경로 | Operation ID | 설명 |
|--------|------------|----------|--------------|------|
| createSelfAssets | POST | /assets/self | createSelfAssets | 본인 자산정보 입력 |

### 4.2 SpouseAssetController

| 메소드 | HTTP 메소드 | API 경로 | Operation ID | 설명 |
|--------|------------|----------|--------------|------|
| createSpouseAssets | POST | /assets/spouse | createSpouseAssets | 배우자 자산정보 입력 |

### 4.3 AssetController

| 메소드 | HTTP 메소드 | API 경로 | Operation ID | 설명 |
|--------|------------|----------|--------------|------|
| getAssets | GET | /assets | getAssets | 자산정보 조회 |
| updateAsset | PUT | /assets/{id} | updateAsset | 자산정보 수정 |
| deleteAsset | DELETE | /assets/{id} | deleteAsset | 자산정보 삭제 |

## 5. 주요 설계 특징

### 5.1 Layered Architecture 적용
- Controller, Service, Domain, Repository, Entity 레이어 명확 분리
- 각 레이어는 하위 레이어에만 의존
- 단방향 의존성으로 결합도 최소화

### 5.2 도메인 중심 설계
- 비즈니스 로직을 Domain Layer에 집중
- Entity는 단순 데이터 매핑 역할
- Domain 객체에 계산 로직 포함 (calculateNetAssets, calculateMonthlyAvailableFunds)

### 5.3 이벤트 기반 통신
- AssetEventPublisher를 통한 이벤트 발행
- Kafka를 통해 Calculator 서비스와 비동기 통신
- 서비스 간 느슨한 결합 유지

### 5.4 검증 분리
- ValidationService를 별도로 분리
- 입력 검증 로직의 재사용성 향상
- 비즈니스 로직과 검증 로직 분리

### 5.5 항목별 Repository 분리
- AssetItem, LoanItem, IncomeItem, ExpenseItem별 Repository 분리
- 각 항목의 독립적 관리 가능
- 확장성 및 유지보수성 향상

## 6. 산출물

### 6.1 클래스 다이어그램
- **전체 클래스 설계**: design/backend/class/asset.puml
- **간소화 버전 + API 매핑**: design/backend/class/asset-simple.puml

### 6.2 설계 문서
- **클래스 설계서**: design/backend/class/asset-class-design.md

## 7. 참조 문서

### 7.1 API 명세서
- design/backend/api/asset-service-api.yaml

### 7.2 내부 시퀀스 설계
- design/backend/sequence/inner/asset-본인자산정보입력.puml
- design/backend/sequence/inner/asset-배우자자산정보입력.puml
- design/backend/sequence/inner/asset-자산항목수정.puml
- design/backend/sequence/inner/asset-자산항목삭제.puml
- design/backend/sequence/inner/asset-재무요약조회.puml

### 7.3 표준 문서
- claude/standard_package_structure.md (패키지 구조 표준)
