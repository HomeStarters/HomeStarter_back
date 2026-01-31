# Roadmap Service 개발 결과서 (최종)

**작성일**: 2025-12-30
**작성자**: 준호 (Backend Developer)
**서비스명**: Roadmap Service
**포트**: 8086

## 1. 개발 개요

### 1.1 목적
장기주거 로드맵 서비스의 핵심 기능 구현
- 생애주기 이벤트 관리
- AI 기반 로드맵 생성 (비동기 처리)
- 로드맵 조회 및 버전 관리

### 1.2 적용 아키텍처
- **패턴**: Layered Architecture
- **패키지 구조**: Controller → Service → Repository
- **데이터베이스**: PostgreSQL (roadmap_service 스키마)
- **캐시**: Redis (Database 6)
- **메시지 큐**: RabbitMQ

## 2. 개발 완료 항목

### 2.1 기본 설정
- ✅ roadmap-service 디렉토리 구조 생성
- ✅ build.gradle 작성 (Spring Cloud Feign, Circuit Breaker, RabbitMQ 포함)
- ✅ application.yml 설정 (포트 8086, 환경 변수 활용)
- ✅ RoadmapApplication 메인 클래스

### 2.2 Entity Layer (repository/entity)
- ✅ EventType (enum): 생애주기 이벤트 유형
- ✅ RoadmapStatus (enum): 로드맵 상태
- ✅ TaskStatus (enum): 비동기 작업 상태
- ✅ LifecycleEventEntity: 생애주기 이벤트
- ✅ RoadmapEntity: 로드맵 기본 정보
- ✅ RoadmapStageEntity: 로드맵 단계별 계획
- ✅ ExecutionGuideEntity: 실행 가이드
- ✅ RoadmapTaskEntity: 비동기 작업 추적

### 2.3 Repository Layer (repository/jpa)
- ✅ LifecycleEventRepository: 생애주기 이벤트 데이터 접근
  - findByUserIdOrderByEventDateAsc 메서드 추가
- ✅ RoadmapRepository: 로드맵 데이터 접근 (버전 관리 쿼리 포함)
  - findTopByUserIdAndStatusOrderByVersionDesc 메서드 추가
  - existsByUserId 메서드 추가
- ✅ RoadmapStageRepository: 로드맵 단계 데이터 접근
- ✅ ExecutionGuideRepository: 실행 가이드 데이터 접근
- ✅ RoadmapTaskRepository: 비동기 작업 데이터 접근
  - findByUserIdAndStatusIn 메서드 추가

### 2.4 DTO Layer (dto/)
- ✅ **Request DTOs** (dto/request/)
  - LifecycleEventRequest: 생애주기 이벤트 등록/수정 요청 (Bean Validation 적용)

- ✅ **Response DTOs** (dto/response/)
  - LifecycleEventResponse: 생애주기 이벤트 응답
  - LifecycleEventListResponse: 생애주기 이벤트 목록 응답
  - GoalHousingDto: 최종목표 주택 정보
  - HousingCharacteristicsDto: 주택 특징
  - FinancialGoalsDto: 재무 목표
  - MonthlySavingsPlanDto: 월별 저축 플랜
  - ExecutionGuideDto: 실행 가이드
  - RoadmapStageDto: 로드맵 단계
  - RoadmapResponse: 로드맵 전체 정보
  - RoadmapTaskResponse: 비동기 작업 요청 응답 (202 Accepted)
  - RoadmapStatusResponse: 비동기 작업 상태 조회 응답
  - RoadmapVersionInfo: 로드맵 버전 정보
  - RoadmapVersionListResponse: 로드맵 버전 목록 응답

### 2.5 Exception Layer (exception/)
- ✅ ConflictException: 중복 작업 충돌 예외
- ✅ ServiceUnavailableException: 외부 서비스 장애 예외

### 2.6 Config Layer (config/)
- ✅ **SecurityConfig**: JWT 인증 및 CORS 설정
  - JwtAuthenticationFilter 적용
  - Redis를 활용한 JWT 블랙리스트 관리

- ✅ **SwaggerConfig**: OpenAPI 3.0 문서화 설정
  - API 문서 자동 생성
  - JWT 인증 스키마 포함

- ✅ **RedisConfig**: Redis 연결 및 캐싱 설정
  - Database 6 사용
  - RedisTemplate 빈 생성

- ✅ **RabbitMQConfig**: RabbitMQ 메시지 큐 설정
  - roadmap-exchange (DirectExchange)
  - roadmap-generation-queue
  - roadmap.generate (routing key)

### 2.7 Feign Client Layer (client/)
- ✅ **Client Interfaces** (client/)
  - UserClient: 사용자 프로필 조회
  - AssetClient: 자산 요약 정보 조회
  - HousingClient: 최종목표 주택 조회
  - CalculatorClient: 재무 계산 결과 조회
  - LlmClient: AI 로드맵 생성 요청

- ✅ **Client DTOs** (client/dto/)
  - UserProfileDto: 사용자 프로필
  - AssetSummaryDto: 자산 요약
  - HousingInfoDto: 주택 정보
  - CalculationResultDto: 계산 결과
  - LlmRequest: LLM 요청
  - LlmResponse: LLM 응답

### 2.8 Service Layer (service/)
- ✅ **LifecycleEventService** (service/LifecycleEventService.java)
  - 인터페이스 정의 (6개 메서드)

- ✅ **LifecycleEventServiceImpl** (service/impl/LifecycleEventServiceImpl.java)
  - CRUD 기능 구현
  - Redis 캐시 무효화
  - 권한 검증 (UnauthorizedException)

- ✅ **RoadmapService** (service/RoadmapService.java)
  - 인터페이스 정의 (5개 메서드)

- ✅ **RoadmapServiceImpl** (service/impl/RoadmapServiceImpl.java)
  - 비동기 로드맵 생성 (RabbitMQ 메시지 발행)
  - Redis 캐싱 (30분 TTL)
  - 버전 관리 (최대 3개)
  - 필수 조건 검증 (최종목표 주택, 생애주기 이벤트)
  - 진행 중인 작업 중복 체크

- ✅ **MessagePublisher** (service/message/MessagePublisher.java)
  - RabbitMQ 메시지 발행
  - 로드맵 생성 작업 메시지 전송

- ✅ **SseService** (service/sse/SseService.java)
  - Server-Sent Events 관리
  - 실시간 진행 상황 스트리밍
  - Emitter 생명주기 관리

### 2.9 Controller Layer (controller/)
- ✅ **LifecycleEventController** (controller/LifecycleEventController.java)
  - POST /lifecycle-events: 이벤트 등록 (201 Created)
  - GET /lifecycle-events: 이벤트 목록 조회 (eventType 필터)
  - GET /lifecycle-events/{id}: 이벤트 상세 조회
  - PUT /lifecycle-events/{id}: 이벤트 수정
  - DELETE /lifecycle-events/{id}: 이벤트 삭제 (204 No Content)
  - Swagger 어노테이션 적용

- ✅ **RoadmapController** (controller/RoadmapController.java)
  - POST /roadmaps: 로드맵 생성 요청 (202 Accepted)
  - GET /roadmaps: 로드맵 조회 (version query param)
  - PUT /roadmaps: 로드맵 재설계 요청 (202 Accepted)
  - GET /roadmaps/status/{requestId}: 작업 상태 조회
  - GET /roadmaps/versions: 버전 이력 조회
  - GET /roadmaps/tasks/{taskId}/stream: SSE 스트리밍
  - Swagger 어노테이션 적용

### 2.10 Async Processing Layer (worker/)
- ✅ **RoadmapWorker** (worker/RoadmapWorker.java)
  - RabbitMQ 메시지 수신 (@RabbitListener)
  - 외부 서비스 데이터 수집 (User, Asset, Housing, Calculator)
  - LLM 로드맵 생성 요청
  - 로드맵 저장 (roadmaps, roadmap_stages, execution_guides)
  - SSE 진행 상황 전송 (0% → 20% → 40% → 80% → 100%)
  - 에러 처리 및 롤백
  - Redis 캐시 무효화

### 2.11 빌드 구성
- ✅ 루트 build.gradle에 Spring Cloud BOM 추가
- ✅ roadmap-service build.gradle 작성
- ✅ 컴파일 성공: `./gradlew roadmap-service:compileJava`
- ✅ 빌드 성공: `./gradlew roadmap-service:build -x test`

## 3. 패키지 구조 (최종)

```
roadmap-service/src/main/java/com/dwj/homestarter/roadmap/
├── RoadmapApplication.java              # 메인 애플리케이션 클래스
├── controller/
│   ├── LifecycleEventController.java    # 생애주기 이벤트 API (5개 엔드포인트)
│   └── RoadmapController.java           # 로드맵 API (6개 엔드포인트, SSE 포함)
├── service/
│   ├── LifecycleEventService.java       # 인터페이스
│   ├── RoadmapService.java              # 인터페이스
│   ├── impl/
│   │   ├── LifecycleEventServiceImpl.java # 구현체
│   │   └── RoadmapServiceImpl.java        # 구현체
│   ├── message/
│   │   └── MessagePublisher.java        # RabbitMQ 메시지 발행
│   └── sse/
│       └── SseService.java              # SSE 스트리밍 관리
├── worker/
│   └── RoadmapWorker.java               # 비동기 로드맵 생성 워커
├── dto/
│   ├── request/
│   │   └── LifecycleEventRequest.java   # Request DTO (Bean Validation)
│   └── response/
│       ├── LifecycleEventResponse.java  # 생애주기 이벤트 응답
│       ├── LifecycleEventListResponse.java
│       ├── GoalHousingDto.java
│       ├── HousingCharacteristicsDto.java
│       ├── FinancialGoalsDto.java
│       ├── MonthlySavingsPlanDto.java
│       ├── ExecutionGuideDto.java
│       ├── RoadmapStageDto.java
│       ├── RoadmapResponse.java         # 로드맵 전체 응답
│       ├── RoadmapTaskResponse.java     # 비동기 작업 요청 응답
│       ├── RoadmapStatusResponse.java   # 비동기 작업 상태 응답
│       ├── RoadmapVersionInfo.java
│       └── RoadmapVersionListResponse.java
├── client/
│   ├── UserClient.java                  # User Service Feign Client
│   ├── AssetClient.java                 # Asset Service Feign Client
│   ├── HousingClient.java               # Housing Service Feign Client
│   ├── CalculatorClient.java            # Calculator Service Feign Client
│   ├── LlmClient.java                   # LLM API Feign Client
│   └── dto/
│       ├── UserProfileDto.java
│       ├── AssetSummaryDto.java
│       ├── HousingInfoDto.java
│       ├── CalculationResultDto.java
│       ├── LlmRequest.java
│       └── LlmResponse.java
├── config/
│   ├── SecurityConfig.java              # JWT 인증 및 CORS 설정
│   ├── SwaggerConfig.java               # OpenAPI 문서화 설정
│   ├── RedisConfig.java                 # Redis 캐싱 설정 (DB 6)
│   └── RabbitMQConfig.java              # RabbitMQ 메시지 큐 설정
├── exception/
│   ├── ConflictException.java           # 중복 작업 예외
│   └── ServiceUnavailableException.java # 외부 서비스 장애 예외
└── repository/
    ├── entity/
    │   ├── EventType.java               # 이벤트 유형 enum
    │   ├── RoadmapStatus.java           # 로드맵 상태 enum
    │   ├── TaskStatus.java              # 작업 상태 enum
    │   ├── LifecycleEventEntity.java    # 생애주기 이벤트
    │   ├── RoadmapEntity.java           # 로드맵
    │   ├── RoadmapStageEntity.java      # 로드맵 단계
    │   ├── ExecutionGuideEntity.java    # 실행 가이드
    │   └── RoadmapTaskEntity.java       # 비동기 작업
    └── jpa/
        ├── LifecycleEventRepository.java
        ├── RoadmapRepository.java
        ├── RoadmapStageRepository.java
        ├── ExecutionGuideRepository.java
        └── RoadmapTaskRepository.java
```

## 4. 주요 설정

### 4.1 application.yml 주요 설정
- **Server Port**: 8086
- **Database**: PostgreSQL (roadmap_service)
- **Redis Database**: 6
- **RabbitMQ**:
  - Exchange: roadmap-exchange
  - Queue: roadmap-generation-queue
  - Routing Key: roadmap.generate

### 4.2 Feign Client 설정
- user-service: http://localhost:8081
- asset-service: http://localhost:8082
- housing-service: http://localhost:8083
- calculator-service: http://localhost:8085
- llm-api: https://api.openai.com (타임아웃: 120초)

### 4.3 Circuit Breaker 설정
- LLM API 호출 시 Circuit Breaker 적용
- Failure Rate Threshold: 60%
- Wait Duration: 30초

## 5. 데이터베이스 테이블

### 5.1 lifecycle_events
- 생애주기 이벤트 저장
- 인덱스: user_id, event_type
- 제약조건: event_date 형식 검증 (yyyy-MM)

### 5.2 roadmaps
- 로드맵 기본 정보
- 버전 관리 (사용자당 최대 3개)
- 유니크 제약: (user_id, version)

### 5.3 roadmap_stages
- 로드맵 단계별 계획
- 외래키: roadmap_id (CASCADE DELETE)
- JSON 필드: features, tips

### 5.4 execution_guides
- 실행 가이드
- 외래키: roadmap_id (CASCADE DELETE)
- JSON 필드: monthly_savings_plan, warnings, tips

### 5.5 roadmap_tasks
- 비동기 작업 추적
- 인덱스: user_id, status
- 작업 상태: PENDING → PROCESSING → COMPLETED/FAILED

## 6. API 엔드포인트 (11개)

### 6.1 생애주기 이벤트 API (5개)
1. `POST /lifecycle-events` - 이벤트 등록 (201 Created)
2. `GET /lifecycle-events?eventType={type}` - 이벤트 목록 조회
3. `GET /lifecycle-events/{id}` - 이벤트 상세 조회
4. `PUT /lifecycle-events/{id}` - 이벤트 수정
5. `DELETE /lifecycle-events/{id}` - 이벤트 삭제 (204 No Content)

### 6.2 로드맵 API (6개)
1. `POST /roadmaps` - 로드맵 생성 요청 (202 Accepted, 비동기)
2. `GET /roadmaps?version={n}` - 로드맵 조회 (캐싱 적용)
3. `PUT /roadmaps` - 로드맵 재설계 요청 (202 Accepted, 비동기)
4. `GET /roadmaps/status/{requestId}` - 작업 상태 조회
5. `GET /roadmaps/versions` - 버전 이력 조회 (최대 3개)
6. `GET /roadmaps/tasks/{taskId}/stream` - SSE 실시간 진행 상황 스트리밍

## 7. 비동기 처리 플로우

### 7.1 로드맵 생성 플로우
1. **Client** → POST /roadmaps
2. **Controller** → RoadmapService.generateRoadmap()
3. **Service** →
   - 필수 조건 검증 (최종목표 주택, 생애주기 이벤트)
   - 진행 중인 작업 중복 체크
   - RoadmapTask 생성 (status: PENDING)
   - MessagePublisher를 통해 RabbitMQ 메시지 발행
4. **Response** → 202 Accepted (requestId 반환)
5. **RabbitMQ** → roadmap-generation-queue에 메시지 적재
6. **RoadmapWorker** →
   - @RabbitListener로 메시지 수신
   - 작업 상태 업데이트 (PROCESSING)
   - SSE 진행 상황 전송 (0% → 20% → 40% → 80% → 100%)
   - 외부 서비스 데이터 수집 (User, Asset, Housing, Calculator)
   - LLM API 호출 (로드맵 생성)
   - DB 저장 (roadmaps, roadmap_stages, execution_guides)
   - 작업 상태 업데이트 (COMPLETED)
   - SSE 완료 이벤트 전송
   - Redis 캐시 무효화

### 7.2 실시간 진행 상황 확인
1. **Client** → GET /roadmaps/tasks/{taskId}/stream (SSE 연결)
2. **SseService** → createEmitter(taskId)
3. **RoadmapWorker** →
   - sendProgress(taskId, 0, "로드맵 생성을 시작합니다", "PROCESSING")
   - sendProgress(taskId, 20, "필요한 정보를 수집하고 있습니다", "PROCESSING")
   - sendProgress(taskId, 40, "AI가 로드맵을 생성하고 있습니다", "PROCESSING")
   - sendProgress(taskId, 80, "로드맵을 저장하고 있습니다", "PROCESSING")
   - sendComplete(taskId, roadmapId)
4. **Client** → SSE 이벤트 수신 및 UI 업데이트

## 8. 컴파일 및 빌드 결과

### 8.1 컴파일
```bash
./gradlew roadmap-service:compileJava
```
**결과**: ✅ BUILD SUCCESSFUL in 1s

### 8.2 빌드
```bash
./gradlew roadmap-service:build -x test
```
**결과**: ✅ BUILD SUCCESSFUL in 2s

### 8.3 생성된 JAR 파일
- **경로**: `roadmap-service/build/libs/roadmap-service.jar`
- **크기**: 정상 생성 (bootJar 포함)

## 9. 개발 완료 확인

### 9.1 레이어별 완성도
- ✅ **Entity Layer**: 100% (8개 클래스)
- ✅ **Repository Layer**: 100% (5개 인터페이스, 필요 메서드 추가 완료)
- ✅ **DTO Layer**: 100% (13개 클래스, Bean Validation 적용)
- ✅ **Exception Layer**: 100% (2개 클래스)
- ✅ **Config Layer**: 100% (4개 클래스)
- ✅ **Feign Client Layer**: 100% (5개 인터페이스, 6개 DTO)
- ✅ **Service Layer**: 100% (4개 서비스 클래스)
- ✅ **Controller Layer**: 100% (2개 컨트롤러, 11개 API)
- ✅ **Worker Layer**: 100% (1개 비동기 워커)

### 9.2 기능별 완성도
- ✅ **생애주기 이벤트 관리**: 100% (CRUD 완료)
- ✅ **비동기 로드맵 생성**: 100% (RabbitMQ + Worker 완료)
- ✅ **실시간 진행 상황**: 100% (SSE 스트리밍 완료)
- ✅ **로드맵 조회 및 캐싱**: 100% (Redis 캐싱 완료)
- ✅ **버전 관리**: 100% (최대 3개 버전 관리 완료)
- ✅ **외부 서비스 연동**: 100% (5개 Feign Client 완료)
- ✅ **보안 및 인증**: 100% (JWT + Redis 블랙리스트 완료)
- ✅ **API 문서화**: 100% (Swagger 설정 완료)

## 10. 참조 문서
- 클래스 설계서: `/Users/daewoong/home_starter/design/backend/class/roadmap-class-design.md`
- 데이터 설계서: `/Users/daewoong/home_starter/design/backend/database/roadmap.md`
- API 명세서: `/Users/daewoong/home_starter/design/backend/api/roadmap-service-api.yaml`

## 11. 개발 환경
- **Java Version**: 21
- **Spring Boot**: 3.3.0
- **Spring Cloud**: 2023.0.2
- **Gradle**: 사용
- **Database**: PostgreSQL 16.x
- **Cache**: Redis 7.x
- **Message Queue**: RabbitMQ 3.x

## 12. 주요 기술 스택
- Spring Boot Web
- Spring Data JPA
- Spring Cloud OpenFeign
- Spring Cloud Circuit Breaker (Resilience4j)
- Spring AMQP (RabbitMQ)
- Spring Data Redis
- Spring Security (JWT)
- SpringDoc OpenAPI (Swagger)
- Lombok
- Jackson (JSON 처리)

## 13. 개발 완료 요약

### 13.1 개발된 클래스 총 개수
- **Entity**: 8개
- **Repository**: 5개
- **DTO**: 19개 (Request 1개 + Response 12개 + Client 6개)
- **Exception**: 2개
- **Config**: 4개
- **Client**: 5개
- **Service**: 6개 (인터페이스 2개 + 구현체 2개 + Publisher 1개 + SSE 1개)
- **Controller**: 2개
- **Worker**: 1개
- **총계**: **52개 클래스**

### 13.2 구현된 API 엔드포인트
- 생애주기 이벤트: 5개
- 로드맵: 6개
- **총계**: **11개 API**

## 14. 비고
- ✅ **모든 레이어 개발 완료**: Entity, Repository, DTO, Exception, Config, Client, Service, Controller, Worker
- ✅ **컴파일 및 빌드 정상 동작 확인**: BUILD SUCCESSFUL
- ✅ **비동기 처리 구현 완료**: RabbitMQ + Worker + SSE 스트리밍
- ⚠️ **테스트 코드 미작성**: 향후 Unit Test 및 Integration Test 개발 필요
- ⚠️ **LLM API Key 관리**: 환경변수로 관리 필요 (현재 하드코딩)
- ⚠️ **Circuit Breaker 테스트**: LLM API 장애 시 동작 검증 필요

## 15. 다음 단계 권장 사항
1. **테스트 코드 작성**
   - LifecycleEventService Unit Test
   - RoadmapService Unit Test
   - Controller Integration Test
   - RoadmapWorker Test (RabbitMQ 테스트 컨테이너)

2. **보안 강화**
   - LLM API Key 환경변수화
   - Rate Limiting 적용
   - Input Validation 강화

3. **모니터링 및 로깅**
   - Actuator 메트릭 추가
   - 로그 레벨 최적화
   - APM 연동 (Pinpoint, Zipkin 등)

4. **성능 최적화**
   - Redis 캐시 전략 튜닝
   - RabbitMQ 메시지 처리 성능 측정
   - Database 인덱스 최적화

---

**개발 완료일**: 2025-12-30
**최종 상태**: ✅ 전체 개발 완료 (테스트 제외)
**빌드 결과**: ✅ BUILD SUCCESSFUL
