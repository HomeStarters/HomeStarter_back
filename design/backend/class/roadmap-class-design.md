# Roadmap Service - Class Design (Layered Architecture)

## 개요

**서비스명**: Roadmap Service
**설계 패턴**: Layered Architecture
**패키지 그룹**: `com.dwj.homestarter.roadmap`
**작성일**: 2025-12-29

## 설계 파일

1. **roadmap.puml**: 전체 클래스 상세 설계
   - 모든 필드, 메소드, 관계 포함
   - 레이어별 상세 구조
   - 예외 처리 클래스 포함

2. **roadmap-simple.puml**: 간소화 버전 + API 매핑표
   - 핵심 클래스만 포함
   - Controller 메소드와 API 엔드포인트 매핑
   - 아키텍처 패턴 설명 포함

## 레이어 구조

### 1. Controller Layer (표현 계층)

#### LifecycleEventController
생애주기 이벤트 관리 API 엔드포인트 제공

**주요 API**:
- `POST /lifecycle-events`: 이벤트 등록
- `GET /lifecycle-events`: 이벤트 목록 조회
- `GET /lifecycle-events/{id}`: 이벤트 상세 조회
- `PUT /lifecycle-events/{id}`: 이벤트 수정
- `DELETE /lifecycle-events/{id}`: 이벤트 삭제

#### RoadmapController
장기주거 로드맵 관리 API 엔드포인트 제공

**주요 API**:
- `POST /roadmaps`: 로드맵 생성 요청 (비동기)
- `GET /roadmaps`: 로드맵 조회
- `PUT /roadmaps`: 로드맵 재설계 요청 (비동기)
- `GET /roadmaps/status/{requestId}`: 생성 상태 조회
- `GET /roadmaps/versions`: 버전 이력 조회
- `GET /roadmaps/tasks/{taskId}/stream`: SSE 진행 상황 스트리밍

### 2. Service Layer (비즈니스 로직 계층)

#### LifecycleEventService
생애주기 이벤트 비즈니스 로직 처리
- 이벤트 CRUD 작업
- 이벤트 검증 (날짜 논리, 중복 체크)
- 권한 확인

#### RoadmapService
로드맵 생성 및 조회 비즈니스 로직 처리
- 로드맵 생성 필수 조건 검증 (최종목표 주택, 생애주기 이벤트)
- 비동기 작업 시작 및 메시지 발행
- 로드맵 조회 및 캐싱
- 버전 관리

#### SseService
Server-Sent Events 실시간 진행 상황 전송
- SSE Emitter 생성 및 관리
- 진행 상황 메시지 전송 (0-100%)
- 완료/실패 이벤트 전송

#### RoadmapWorker
비동기 AI 로드맵 생성 처리
- RabbitMQ 메시지 수신
- 외부 서비스 데이터 수집 (병렬 처리)
  - User, Asset, Housing, Calculator 서비스
- LLM API 호출 (Circuit Breaker 적용)
- 결과 검증 및 저장
- SSE 진행 상황 업데이트

#### MessagePublisher
RabbitMQ 메시지 발행
- 로드맵 생성 작업 메시지 발행
- Exchange: `roadmap-exchange`
- Routing Key: `roadmap.generate`

### 3. Domain Layer (도메인 계층)

#### Entity Classes

**LifecycleEvent**
- 생애주기 이벤트 엔티티
- 이벤트 유형: MARRIAGE, BIRTH, CHILD_EDUCATION, RETIREMENT, OTHER
- 날짜: YearMonth 형식 (yyyy-MM)

**Roadmap**
- 장기주거 로드맵 엔티티
- 버전 관리 (version)
- 상태: PROCESSING, COMPLETED, FAILED

**RoadmapStage**
- 로드맵 단계별 계획
- 단계 번호, 이름, 입주 시기, 거주 기간
- 주택 특성 및 재무 목표 포함

**ExecutionGuide**
- 실행 가이드 엔티티
- 월별 저축 계획, 주의사항, 팁

**RoadmapTask**
- 비동기 작업 추적 엔티티
- 작업 상태: PENDING, PROCESSING, COMPLETED, FAILED
- 진행률 (0-100%)

#### Value Objects

**HousingCharacteristics**
- 주택 특성 (가격, 위치, 타입, 특징)

**FinancialGoals**
- 재무 목표 (목표 저축액, 월 저축액, 대출 금액, 대출 상품)

**MonthlySavingsPlan**
- 월별 저축 계획 (기간, 금액, 목적)

**RoadmapVersionInfo**
- 로드맵 버전 정보 (버전, 생성일, 변경 설명)

### 4. Repository Layer (데이터 접근 계층)

#### JPA Repository Interfaces

**LifecycleEventRepository**
- 생애주기 이벤트 데이터 접근
- 사용자별 조회, 이벤트 유형별 필터링, 개수 카운트

**RoadmapRepository**
- 로드맵 데이터 접근
- 최신 버전 조회, 특정 버전 조회, 버전 이력 조회

**RoadmapStageRepository**
- 로드맵 단계 데이터 접근
- 로드맵 ID로 단계 목록 조회 (순서 정렬)

**ExecutionGuideRepository**
- 실행 가이드 데이터 접근
- 로드맵 ID로 조회

**RoadmapTaskRepository**
- 비동기 작업 데이터 접근
- 진행 중 작업 조회 (PENDING, PROCESSING 상태)

### 5. DTO Layer (데이터 전송 계층)

#### Request DTOs

**LifecycleEventRequest**
- 생애주기 이벤트 등록/수정 요청
- 필드: name, eventType, eventDate, housingCriteria
- Bean Validation 적용

#### Response DTOs

**LifecycleEventResponse**
- 생애주기 이벤트 응답
- Entity -> DTO 변환 메소드 포함

**LifecycleEventListResponse**
- 이벤트 목록 응답 (events, total)

**RoadmapResponse**
- 로드맵 전체 정보 응답
- 최종목표 주택, 단계별 계획, 실행 가이드 포함

**RoadmapTaskResponse**
- 비동기 작업 생성 응답 (202 Accepted)
- 필드: requestId, status, progress, message, estimatedCompletionTime

**RoadmapStatusResponse**
- 비동기 작업 상태 조회 응답
- PROCESSING: progress, message
- COMPLETED: roadmapId
- FAILED: error

**RoadmapVersionListResponse**
- 로드맵 버전 이력 응답 (최대 3개)

### 6. External Client Layer (외부 서비스 통신 계층)

#### Feign Clients

**HousingClient**
- Housing 서비스 통신
- 최종목표 주택 정보 조회
- Circuit Breaker 적용

**UserClient**
- User 서비스 통신
- 사용자 프로필 조회 (생년월일, 투자성향 등)

**AssetClient**
- Asset 서비스 통신
- 자산 요약 정보 조회 (본인/배우자 자산, 소득, 지출)

**CalculatorClient**
- Calculator 서비스 통신
- 입주 후 지출 계산 결과 조회

**LlmClient**
- LLM API 통신
- AI 로드맵 생성 요청
- Circuit Breaker 적용 (재시도 최대 3회)

#### Circuit Breaker

**CircuitBreaker**
- 외부 서비스 장애 대응
- 상태: CLOSED, OPEN, HALF_OPEN
- LLM API 호출 시 적용
- 장애 시 재시도 전략 (5초 → 15초 → 30초)

### 7. Message Layer (메시징 계층)

#### Message Objects

**RoadmapGenerationMessage**
- 로드맵 생성 작업 메시지
- 필드: taskId, userId, finalHousingId, timestamp
- Queue: `roadmap-generation-queue`

**RoadmapInputData**
- AI 입력 데이터 구조체
- 사용자 프로필, 자산 정보, 최종목표 주택, 계산 결과, 생애주기 이벤트

**RoadmapAIResponse**
- AI 생성 결과
- 단계별 계획 (StageData), 실행 가이드

### 8. Exception Layer (예외 처리 계층)

**ValidationException**
- 입력 검증 실패 (400 Bad Request)

**EntityNotFoundException**
- 리소스 없음 (404 Not Found)

**ConflictException**
- 중복 작업 요청 (409 Conflict)

**ServiceUnavailableException**
- 외부 서비스 장애 (503 Service Unavailable)

**UnauthorizedException**
- 권한 없음 (403 Forbidden)

## 비동기 처리 흐름

### 1. 로드맵 생성 요청 (동기 부분)

```
Client → RoadmapController.createRoadmap()
  → RoadmapService.generateRoadmap(userId)
    1) 필수 조건 검증
       - HousingClient: 최종목표 주택 확인
       - EventRepository: 생애주기 이벤트 개수 확인
    2) 진행 중 작업 확인 (중복 방지)
    3) RoadmapTask 생성 (status: PENDING, progress: 0)
    4) MessagePublisher: 작업 메시지 발행
    5) SseService: SSE Emitter 생성 및 등록
  → 202 Accepted 응답 (3초 이내)
```

### 2. AI 워커 비동기 처리

```
RabbitMQ → RoadmapWorker.processRoadmapGeneration(message)
  1) 작업 상태 업데이트 (PROCESSING, 10%)
  2) 병렬 데이터 수집 (30%)
     - UserClient: 사용자 프로필
     - AssetClient: 자산 요약
     - HousingClient: 최종목표 주택
     - CalculatorClient: 계산 결과
     - EventRepository: 생애주기 이벤트
  3) LLM 입력 데이터 구조화 (40%)
  4) LLM API 호출 (Circuit Breaker, 60%)
  5) 결과 검증 (80%)
     - 재무 논리 검증 (월 저축액 <= 월 가용자금)
     - 시기 논리 검증 (단계 간 충분한 기간)
     - 데이터 무결성 검증
  6) 로드맵 저장 (100%)
     - Roadmap, RoadmapStage, ExecutionGuide
  7) 작업 완료 (COMPLETED)

  각 단계마다 SseService로 진행 상황 전송
```

### 3. 클라이언트 진행 상황 확인

```
Client → RoadmapController.streamRoadmapProgress(taskId)
  → SseService.getEmitter(taskId)
  → SSE 스트림 연결
  → 실시간 이벤트 수신:
     - {progress: 10, message: "데이터 수집 시작", status: "PROCESSING"}
     - {progress: 30, message: "데이터 수집 완료", status: "PROCESSING"}
     - {progress: 40, message: "AI 분석 시작", status: "PROCESSING"}
     - {progress: 60, message: "AI 분석 중...", status: "PROCESSING"}
     - {progress: 80, message: "로드맵 검증 중", status: "PROCESSING"}
     - {progress: 100, status: "COMPLETED", roadmapId: "uuid"}
```

## Redis 캐싱 전략

### 캐시 정책

**캐시 키**: `roadmap:{userId}`
**TTL**: 30분
**캐시 대상**: 최신 로드맵 조회 결과

### 캐시 흐름

```
Client → RoadmapController.getRoadmap()
  → RoadmapService.getRoadmap(userId)
    1) Redis 캐시 조회
    2-1) 캐시 히트 → 즉시 반환
    2-2) 캐시 미스
         → RoadmapRepository 조회
         → RoadmapStageRepository 조회
         → ExecutionGuideRepository 조회
         → EventRepository 조회 (타임라인 마커)
         → 타임라인 데이터 생성
         → RoadmapResponse DTO 생성
         → Redis 캐싱 (30분)
         → 반환
```

### 캐시 무효화

- 로드맵 재설계 완료 시
- 생애주기 이벤트 수정 시
- 최종목표 주택 변경 시

## API와 Controller 메소드 매핑표

### LifecycleEventController

| HTTP Method | Endpoint | operationId | Controller Method | Request | Response |
|------------|----------|-------------|------------------|---------|----------|
| POST | /lifecycle-events | createLifecycleEvent | createLifecycleEvent() | LifecycleEventRequest | 201 Created (LifecycleEventResponse) |
| GET | /lifecycle-events | getLifecycleEvents | getLifecycleEvents() | eventType (query) | 200 OK (LifecycleEventListResponse) |
| GET | /lifecycle-events/{id} | getLifecycleEvent | getLifecycleEvent() | id (path) | 200 OK (LifecycleEventResponse) |
| PUT | /lifecycle-events/{id} | updateLifecycleEvent | updateLifecycleEvent() | id (path), LifecycleEventRequest | 200 OK (LifecycleEventResponse) |
| DELETE | /lifecycle-events/{id} | deleteLifecycleEvent | deleteLifecycleEvent() | id (path) | 204 No Content |

### RoadmapController

| HTTP Method | Endpoint | operationId | Controller Method | Request | Response |
|------------|----------|-------------|------------------|---------|----------|
| POST | /roadmaps | createRoadmap | createRoadmap() | - | 202 Accepted (RoadmapTaskResponse) |
| GET | /roadmaps | getRoadmap | getRoadmap() | version (query) | 200 OK (RoadmapResponse) |
| PUT | /roadmaps | updateRoadmap | updateRoadmap() | - | 202 Accepted (RoadmapTaskResponse) |
| GET | /roadmaps/status/{requestId} | getRoadmapStatus | getRoadmapStatus() | requestId (path) | 200 OK (RoadmapStatusResponse) |
| GET | /roadmaps/versions | getRoadmapVersions | getRoadmapVersions() | - | 200 OK (RoadmapVersionListResponse) |
| GET | /roadmaps/tasks/{taskId}/stream | - | streamRoadmapProgress() | taskId (path) | SseEmitter (SSE) |

## 주요 설계 특징

### 1. Layered Architecture 적용

- **명확한 계층 분리**: Controller → Service → Repository
- **의존성 방향**: 상위 계층 → 하위 계층
- **단방향 의존성**: 역방향 참조 금지
- **계층별 책임 명확화**: 각 계층의 역할 분리

### 2. 비동기 처리 (RabbitMQ + SSE)

- **즉시 응답**: 202 Accepted (3초 이내)
- **백그라운드 처리**: RabbitMQ Worker
- **실시간 피드백**: SSE 진행 상황 스트리밍
- **안정성**: Circuit Breaker, 재시도 전략

### 3. 외부 서비스 통신

- **Feign Client**: 선언적 HTTP 클라이언트
- **Circuit Breaker**: 장애 전파 방지
- **병렬 데이터 수집**: CompletableFuture 활용
- **타임아웃 설정**: 각 서비스별 적절한 타임아웃

### 4. 캐싱 전략

- **Redis 캐싱**: 자주 조회되는 로드맵 데이터
- **TTL 30분**: 적절한 캐시 유효 시간
- **캐시 무효화**: 데이터 변경 시 자동 무효화

### 5. 도메인 모델

- **Rich Domain Model**: 엔티티에 비즈니스 로직 포함
- **Value Object**: 불변 객체로 값 표현
- **Enum 활용**: 이벤트 유형, 상태 타입 안전성 보장

### 6. 예외 처리

- **계층별 예외**: 각 상황에 맞는 예외 클래스
- **HTTP 상태 코드 매핑**: 적절한 응답 코드 반환
- **에러 메시지**: 명확한 에러 메시지 제공

### 7. DTO 변환

- **Request DTO**: API 요청 → Domain Entity
- **Response DTO**: Domain Entity → API 응답
- **계층 간 데이터 캡슐화**: 내부 구조 노출 방지

## 데이터베이스 테이블 구조

### lifecycle_events

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | VARCHAR(36) | PK, UUID |
| user_id | VARCHAR(36) | FK (Users), 인덱스 |
| name | VARCHAR(100) | 이벤트 이름 |
| event_type | VARCHAR(20) | 이벤트 유형 (Enum) |
| event_date | VARCHAR(7) | 이벤트 예정일 (yyyy-MM) |
| housing_criteria | VARCHAR(200) | 주택선택 고려기준 |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

### roadmaps

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | VARCHAR(36) | PK, UUID |
| user_id | VARCHAR(36) | FK (Users), 인덱스 |
| version | INT | 로드맵 버전 |
| status | VARCHAR(20) | 상태 (Enum) |
| task_id | VARCHAR(36) | FK (roadmap_tasks) |
| final_housing_id | VARCHAR(36) | 최종목표 주택 ID |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |

### roadmap_stages

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | VARCHAR(36) | PK, UUID |
| roadmap_id | VARCHAR(36) | FK (roadmaps) |
| stage_number | INT | 단계 번호 |
| stage_name | VARCHAR(100) | 단계명 |
| move_in_date | VARCHAR(7) | 입주 시기 (yyyy-MM) |
| duration | INT | 거주 기간 (개월) |
| estimated_price | BIGINT | 예상 가격 |
| location | VARCHAR(200) | 위치 |
| type | VARCHAR(50) | 주택 타입 |
| features | TEXT | 특징 (JSON) |
| target_savings | BIGINT | 목표 저축액 |
| monthly_savings | BIGINT | 월 저축액 |
| loan_amount | BIGINT | 대출 금액 |
| loan_product | VARCHAR(100) | 대출 상품 |
| strategy | TEXT | 실행 전략 |
| tips | TEXT | 팁 (JSON) |

### execution_guides

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | VARCHAR(36) | PK, UUID |
| roadmap_id | VARCHAR(36) | FK (roadmaps) |
| monthly_savings_plan | TEXT | 월별 저축 플랜 (JSON) |
| warnings | TEXT | 주의사항 (JSON) |
| tips | TEXT | 팁 (JSON) |

### roadmap_tasks

| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | VARCHAR(36) | PK, UUID |
| user_id | VARCHAR(36) | FK (Users), 인덱스 |
| status | VARCHAR(20) | 상태 (Enum) |
| progress | INT | 진행률 (0-100) |
| message | VARCHAR(200) | 진행 상황 메시지 |
| roadmap_id | VARCHAR(36) | FK (roadmaps), nullable |
| error_message | TEXT | 에러 메시지, nullable |
| created_at | TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | 수정일시 |
| completed_at | TIMESTAMP | 완료일시, nullable |

## 참조

- API 명세서: `/design/backend/api/roadmap-service-api.yaml`
- 내부 시퀀스: `/design/backend/sequence/inner/roadmap-*.puml`
- 클래스 다이어그램: `/design/backend/class/roadmap.puml`
- 간소화 다이어그램: `/design/backend/class/roadmap-simple.puml`
