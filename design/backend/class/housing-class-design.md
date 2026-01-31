# Housing Service - 클래스 설계서

## 개요

- **서비스명**: Housing Service
- **아키텍처 패턴**: Layered Architecture
- **패키지 그룹**: com.dwj.homestarter.housing
- **설계 날짜**: 2024-12-29

## 아키텍처 패턴 선택

### Layered Architecture 채택 이유

1. **명확한 계층 분리**: Controller → Service → Domain → Repository 계층 구조
2. **단순성과 유지보수성**: 각 계층의 역할이 명확하고 이해하기 쉬움
3. **기존 Spring Boot 프레임워크와의 자연스러운 통합**
4. **팀 친숙도**: 대부분의 개발자들이 익숙한 패턴

## 계층 구조

```
┌─────────────────────────────────────────┐
│      Controller Layer (Presentation)    │
│  - REST API 엔드포인트                   │
│  - 요청/응답 검증 및 변환                │
│  - JWT 인증 처리                         │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        Service Layer (Business)         │
│  - 비즈니스 로직 처리                    │
│  - 트랜잭션 관리                         │
│  - 이벤트 발행 및 캐시 관리              │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│            Domain Layer                 │
│  - 엔티티 및 Value Objects              │
│  - 도메인 규칙 및 제약사항               │
│  - JPA Entity 매핑                      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│     Repository Layer (Persistence)      │
│  - 데이터 영속성 관리                    │
│  - Spring Data JPA                      │
│  - 쿼리 메서드 정의                      │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│       Infrastructure Layer              │
│  - 외부 서비스 연동 (Kakao Map)          │
│  - 캐시, 이벤트, Circuit Breaker        │
│  - 기술적 관심사 처리                    │
└─────────────────────────────────────────┘
```

## 주요 클래스

### 1. Controller Layer

#### HousingController
- **역할**: REST API 엔드포인트 제공
- **주요 메서드**:
  - `createHousing()`: POST /housings - 주택 등록
  - `getHousings()`: GET /housings - 주택 목록 조회
  - `getHousing()`: GET /housings/{id} - 주택 상세 조회
  - `updateHousing()`: PUT /housings/{id} - 주택 정보 수정
  - `deleteHousing()`: DELETE /housings/{id} - 주택 삭제
  - `setGoalHousing()`: PUT /housings/{id}/goal - 최종목표 주택 설정

### 2. Service Layer

#### HousingService
- **역할**: 비즈니스 로직 처리 및 트랜잭션 관리
- **주요 책임**:
  - 주택 CRUD 작업
  - 최종목표 주택 설정 (기존 목표 해제 포함)
  - Kakao Map API 연동 (Circuit Breaker 적용)
  - 캐시 관리 및 무효화
  - 도메인 이벤트 발행

#### HousingValidator
- **역할**: 입력 데이터 유효성 검증
- **검증 항목**:
  - 필수 필드 존재 여부
  - 가격 형식 및 범위
  - 입주희망년월 미래 날짜 확인
  - 주택 유형 유효성

### 3. Domain Layer

#### Housing (Entity)
- **역할**: 주택 정보 핵심 엔티티
- **주요 속성**:
  - 기본 정보: id, userId, name, housingType, price
  - 날짜 정보: moveInDate, completionDate
  - 위치 정보: address (Value Object)
  - 관계 정보: transportations (1:N)
  - 부가 정보: complexInfo, livingEnvironment
  - 상태 정보: isGoal, createdAt, updatedAt

#### Address (Value Object)
- **역할**: 주소 정보 값 객체
- **주요 속성**: fullAddress, roadAddress, jibunAddress, latitude, longitude
- **불변성**: equals/hashCode 구현

#### Transportation (Entity)
- **역할**: 교통호재 정보
- **관계**: Housing과 1:N, CommuteTime과 1:1

#### CommuteTime (Value Object)
- **역할**: 출퇴근 시간 정보
- **주요 속성**: selfBefore/After, spouseBefore/After

### 4. Repository Layer

#### HousingRepository
- **역할**: 주택 데이터 영속성 관리
- **주요 메서드**:
  - `save()`: 주택 저장/수정
  - `findById()`: ID로 조회
  - `findByUserId()`: 사용자별 주택 목록
  - `findGoalHousingByUserId()`: 최종목표 주택 조회
  - `clearGoalHousing()`: 최종목표 해제

#### TransportationRepository
- **역할**: 교통호재 데이터 관리
- **주요 메서드**:
  - `saveAll()`: 여러 교통호재 일괄 저장
  - `findByHousingId()`: 주택별 교통호재 조회
  - `deleteByHousingId()`: 주택별 교통호재 삭제

### 5. Infrastructure Layer

#### KakaoMapClient
- **역할**: Kakao Map API 연동
- **주요 기능**: 주소 → 좌표 변환

#### CircuitBreaker
- **역할**: 외부 서비스 장애 대응
- **상태**: CLOSED, OPEN, HALF_OPEN

#### EventPublisher
- **역할**: 도메인 이벤트 발행
- **이벤트 타입**:
  - HousingCreatedEvent: 주택 생성
  - HousingUpdatedEvent: 주택 수정
  - GoalHousingSelectedEvent: 최종목표 선택

#### CacheManager
- **역할**: Redis 캐시 관리
- **캐시 키 패턴**:
  - `housing:detail:{housingId}`: 상세 정보 (TTL: 10분)
  - `housing:list:{userId}:{page}`: 목록 정보 (TTL: 5분)
  - `housing:goal:{userId}`: 최종목표 주택 (TTL: 30분)

## DTO 설계

### Request DTOs

1. **HousingCreateRequest**: 주택 생성 요청
2. **HousingUpdateRequest**: 주택 수정 요청
3. **AddressRequest**: 주소 정보
4. **TransportationRequest**: 교통호재 정보
5. **CommuteTimeRequest**: 출퇴근 시간 정보

### Response DTOs

1. **HousingResponse**: 주택 상세 응답
2. **HousingListResponse**: 주택 목록 응답
3. **HousingListItem**: 목록 항목 (간소화)
4. **GoalHousingResponse**: 최종목표 설정 응답
5. **ErrorResponse**: 에러 응답

## API 매핑표

| Controller 메서드 | HTTP Method | Endpoint | Operation ID | Request | Response | User Story |
|------------------|-------------|----------|--------------|---------|----------|------------|
| createHousing | POST | /housings | createHousing | HousingCreateRequest | HousingResponse (201) | UFR-HOUS-010 |
| getHousings | GET | /housings | getHousings | Query Params | HousingListResponse (200) | UFR-HOUS-020 |
| getHousing | GET | /housings/{id} | getHousing | Path: id | HousingResponse (200) | UFR-HOUS-030 |
| updateHousing | PUT | /housings/{id} | updateHousing | HousingUpdateRequest | HousingResponse (200) | UFR-HOUS-040 |
| deleteHousing | DELETE | /housings/{id} | deleteHousing | Path: id | 204 No Content | UFR-HOUS-040 |
| setGoalHousing | PUT | /housings/{id}/goal | setGoalHousing | Path: id | GoalHousingResponse (200) | UFR-HOUS-050 |

## 예외 처리

### 커스텀 예외

1. **HousingNotFoundException**: 주택을 찾을 수 없음 (404)
2. **UnauthorizedAccessException**: 권한 없음 (403)
3. **InvalidHousingDataException**: 잘못된 입력 데이터 (400)
4. **ExternalServiceException**: 외부 서비스 오류 (502/503)

## 이벤트 발행

### 도메인 이벤트

1. **HousingCreatedEvent**
   - 발행 시점: 주택 생성 완료
   - 페이로드: housingId, userId, address, price, occurredAt

2. **HousingUpdatedEvent**
   - 발행 시점: 주택 정보 수정 완료
   - 페이로드: housingId, userId, updatedFields, occurredAt

3. **GoalHousingSelectedEvent**
   - 발행 시점: 최종목표 주택 설정 완료
   - 페이로드: housingId, userId, occurredAt
   - 구독자: Roadmap 서비스, Calculator 서비스

## 패키지 구조

```
com.dwj.homestarter.housing
├── controller
│   └── HousingController
├── service
│   ├── HousingService
│   └── HousingValidator
├── domain
│   ├── model
│   │   ├── Housing
│   │   ├── Address
│   │   ├── Transportation
│   │   ├── CommuteTime
│   │   ├── ComplexInfo
│   │   └── LivingEnvironment
│   └── enums
│       ├── HousingType
│       ├── SunlightLevel
│       └── NoiseLevel
├── repository
│   ├── HousingRepository
│   ├── TransportationRepository
│   └── CommuteTimeRepository
├── dto
│   ├── request
│   │   ├── HousingCreateRequest
│   │   ├── HousingUpdateRequest
│   │   ├── AddressRequest
│   │   └── TransportationRequest
│   └── response
│       ├── HousingResponse
│       ├── HousingListResponse
│       ├── HousingListItem
│       ├── GoalHousingResponse
│       └── ErrorResponse
├── infrastructure
│   ├── KakaoMapClient
│   ├── CircuitBreaker
│   ├── EventPublisher
│   └── CacheManager
├── event
│   ├── DomainEvent
│   ├── HousingCreatedEvent
│   ├── HousingUpdatedEvent
│   └── GoalHousingSelectedEvent
└── exception
    ├── HousingNotFoundException
    ├── UnauthorizedAccessException
    ├── InvalidHousingDataException
    └── ExternalServiceException
```

## 다이어그램 파일

1. **housing.puml**: 전체 클래스 설계 (상세 버전)
   - 모든 클래스와 관계 포함
   - 메서드 및 속성 상세 정의

2. **housing-simple.puml**: 간소화 버전
   - 핵심 클래스만 포함
   - API 매핑표 포함
   - 계층 구조 설명 포함

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.x
- **ORM**: Spring Data JPA (Hibernate)
- **캐시**: Redis
- **메시지 큐**: Kafka
- **외부 API**: Kakao Map API
- **패턴**: Circuit Breaker (Resilience4j)

## 설계 고려사항

### 1. 보안
- JWT 기반 사용자 인증
- 주택 소유자 검증 (userId 확인)
- 다른 사용자의 주택 접근 차단

### 2. 성능
- Redis 캐시를 통한 조회 성능 최적화
- 목록 조회: 5분 TTL
- 상세 조회: 10분 TTL
- 캐시 무효화 전략 (생성/수정/삭제 시)

### 3. 안정성
- Circuit Breaker 패턴으로 외부 서비스 장애 대응
- Kakao Map API 장애 시 좌표 null 허용
- 배치 작업으로 좌표 재처리 가능

### 4. 확장성
- 도메인 이벤트 발행으로 서비스 간 느슨한 결합
- 이벤트 기반 비동기 처리 가능
- 마이크로서비스 아키텍처 전환 용이

### 5. 유지보수성
- 계층별 명확한 책임 분리
- Value Object 패턴으로 불변성 보장
- 커스텀 예외를 통한 명확한 에러 처리

## 향후 개선 사항

1. **검색 기능**: Elasticsearch 도입으로 복합 검색 지원
2. **이미지 관리**: 주택 사진 업로드 및 관리
3. **좋아요 기능**: 관심 주택 북마크
4. **비교 기능**: 여러 주택 비교 분석
5. **추천 기능**: AI 기반 주택 추천
