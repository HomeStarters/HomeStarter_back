# Roadmap Service API 매핑표

## 문서 정보
- **작성일**: 2025-12-30
- **서비스명**: Roadmap Service
- **Controller**: LifecycleEventController, RoadmapController
- **API 설계서**: design/backend/api/roadmap-service-api.yaml
- **Controller 파일**:
  - roadmap-service/src/main/java/com/dwj/homestarter/roadmap/controller/LifecycleEventController.java
  - roadmap-service/src/main/java/com/dwj/homestarter/roadmap/controller/RoadmapController.java

---

## API 매핑 현황

### 전체 요약
| 구분 | 개수 |
|------|------|
| 설계서 API 총 개수 | 10 |
| 구현된 API 총 개수 | 11 |
| 설계서와 일치하는 API | 10 |
| 추가 구현된 API | 1 |
| 미구현 API | 0 |

---

## 상세 매핑표

## LifecycleEvents 그룹

### 1. 생애주기 이벤트 등록 (UFR-ROAD-010)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | POST | POST | ✅ |
| **Endpoint** | /lifecycle-events | /lifecycle-events | ✅ |
| **Controller 메서드** | createLifecycleEvent | createLifecycleEvent | ✅ |
| **Request DTO** | LifecycleEventRequest | LifecycleEventRequest | ✅ |
| **Response DTO** | LifecycleEventResponse | LifecycleEventResponse | ✅ |
| **HTTP Status** | 201 Created | 201 Created | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Request 필드 검증**:
- name (이벤트 이름): ✅
- eventType (이벤트 유형): ✅
- eventDate (이벤트 예정일): ✅
- housingCriteria (주택선택 고려기준): ✅

**Response 필드 검증**:
- id (이벤트 ID): ✅
- userId (사용자 ID): ✅
- name (이벤트 이름): ✅
- eventType (이벤트 유형): ✅
- eventDate (이벤트 예정일): ✅
- housingCriteria (주택선택 고려기준): ✅
- createdAt (생성 일시): ✅
- updatedAt (수정 일시): ✅ (설계서에 명시되지 않았으나 구현됨)

---

### 2. 생애주기 이벤트 목록 조회 (UFR-ROAD-010)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /lifecycle-events | /lifecycle-events | ✅ |
| **Controller 메서드** | getLifecycleEvents | getLifecycleEvents | ✅ |
| **Request DTO** | - (Query Param: eventType) | - (Query Param: eventType) | ✅ |
| **Response DTO** | {events: [...], total: int} | LifecycleEventListResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Query Parameter 검증**:
- eventType (이벤트 유형 필터, Optional): ✅

**Response 필드 검증**:
- events (이벤트 목록): ✅
- total (총 개수): ✅

---

### 3. 생애주기 이벤트 상세 조회 (UFR-ROAD-010)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /lifecycle-events/{id} | /lifecycle-events/{id} | ✅ |
| **Controller 메서드** | getLifecycleEvent | getLifecycleEvent | ✅ |
| **Request DTO** | - (Path Variable: id) | - (Path Variable: id) | ✅ |
| **Response DTO** | LifecycleEventResponse | LifecycleEventResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

---

### 4. 생애주기 이벤트 수정 (UFR-ROAD-040)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | PUT | PUT | ✅ |
| **Endpoint** | /lifecycle-events/{id} | /lifecycle-events/{id} | ✅ |
| **Controller 메서드** | updateLifecycleEvent | updateLifecycleEvent | ✅ |
| **Request DTO** | LifecycleEventRequest | LifecycleEventRequest | ✅ |
| **Response DTO** | LifecycleEventResponse | LifecycleEventResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

---

### 5. 생애주기 이벤트 삭제 (UFR-ROAD-040)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | DELETE | DELETE | ✅ |
| **Endpoint** | /lifecycle-events/{id} | /lifecycle-events/{id} | ✅ |
| **Controller 메서드** | deleteLifecycleEvent | deleteLifecycleEvent | ✅ |
| **Request DTO** | - (Path Variable: id) | - (Path Variable: id) | ✅ |
| **Response DTO** | - (204 No Content) | Void | ✅ |
| **HTTP Status** | 204 No Content | 204 No Content | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

---

## Roadmap 그룹

### 6. 장기주거 로드맵 생성 요청 (UFR-ROAD-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | POST | POST | ✅ |
| **Endpoint** | /roadmaps | /roadmaps | ✅ |
| **Controller 메서드** | createRoadmap | createRoadmap | ✅ |
| **Request DTO** | - (인증 헤더만) | - (인증 헤더 + Authorization) | ✅ |
| **Response DTO** | {requestId, status, message, estimatedCompletionTime} | RoadmapTaskResponse | ✅ |
| **HTTP Status** | 202 Accepted | 202 Accepted | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | Authorization 헤더 추가 전달 (외부 서비스 호출용) | - |

**Response 필드 검증**:
- requestId (로드맵 생성 요청 ID): ✅
- status (처리 상태): ✅
- message (처리 상태 메시지): ✅
- estimatedCompletionTime (예상 완료 시간): ✅

---

### 7. 장기주거 로드맵 조회 (UFR-ROAD-030)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /roadmaps | /roadmaps | ✅ |
| **Controller 메서드** | getRoadmap | getRoadmap | ✅ |
| **Request DTO** | - (Query Param: version) | - (Query Param: version) | ✅ |
| **Response DTO** | RoadmapResponse | RoadmapResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Query Parameter 검증**:
- version (로드맵 버전, Optional): ✅

**Response 필드 검증**:
- id (로드맵 ID): ✅
- userId (사용자 ID): ✅
- version (로드맵 버전): ✅
- status (로드맵 상태): ✅
- goalHousing (최종목표 주택 정보): ✅
- stages (단계별 계획): ✅
- executionGuide (실행 가이드): ✅
- createdAt (생성 일시): ✅
- updatedAt (수정 일시): ✅

---

### 8. 장기주거 로드맵 재설계 (UFR-ROAD-040)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | PUT | PUT | ✅ |
| **Endpoint** | /roadmaps | /roadmaps | ✅ |
| **Controller 메서드** | updateRoadmap | updateRoadmap | ✅ |
| **Request DTO** | - (인증 헤더만) | - (인증 헤더 + Authorization) | ✅ |
| **Response DTO** | {requestId, status, message, estimatedCompletionTime} | RoadmapTaskResponse | ✅ |
| **HTTP Status** | 202 Accepted | 202 Accepted | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | Authorization 헤더 추가 전달 (외부 서비스 호출용) | - |

---

### 9. 로드맵 생성/재설계 상태 조회 (UFR-ROAD-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /roadmaps/status/{requestId} | /roadmaps/status/{requestId} | ✅ |
| **Controller 메서드** | getRoadmapStatus | getRoadmapStatus | ✅ |
| **Request DTO** | - (Path Variable: requestId) | - (Path Variable: requestId) | ✅ |
| **Response DTO** | {requestId, status, progress, message, roadmapId?, error?} | RoadmapStatusResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Response 필드 검증**:
- requestId (로드맵 요청 ID): ✅
- status (처리 상태): ✅
- progress (진행률 0-100): ✅
- message (상태 메시지): ✅
- roadmapId (완료 시 로드맵 ID): ✅
- error (실패 시 에러 메시지): ✅

---

### 10. 로드맵 버전 이력 조회 (UFR-ROAD-040)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /roadmaps/versions | /roadmaps/versions | ✅ |
| **Controller 메서드** | getRoadmapVersions | getRoadmapVersions | ✅ |
| **Request DTO** | - (인증 헤더만) | - (인증 헤더만) | ✅ |
| **Response DTO** | {versions: [...]} | RoadmapVersionListResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | 최대 3개 | 최대 3개 | ✅ |

**Response 필드 검증**:
- versions (버전 목록): ✅
  - version (버전 번호): ✅
  - createdAt (생성 일시): ✅
  - changeDescription (변경 설명): ✅

---

## 추가 구현된 API

### 11. 로드맵 생성 진행 상황 스트리밍 (SSE) ⭐

| 항목 | 구현 내용 |
|------|---------|
| **HTTP Method** | GET |
| **Endpoint** | /roadmaps/tasks/{taskId}/stream |
| **Controller 메서드** | streamRoadmapProgress |
| **Request DTO** | - (Path Variable: taskId) |
| **Response Type** | Server-Sent Events (text/event-stream) |
| **HTTP Status** | 200 OK |
| **인증 필요** | Yes (@AuthenticationPrincipal) |
| **비고** | 실시간 진행 상황 스트리밍 기능 |

**추가 이유**:
- 비동기 로드맵 생성 과정의 실시간 진행 상황을 사용자에게 제공
- 폴링 방식의 `/roadmaps/status/{requestId}` API 대비 효율적인 실시간 업데이트
- 사용자 경험 개선: 로드맵 생성 과정을 실시간으로 확인 가능
- SSE(Server-Sent Events) 프로토콜을 통한 단방향 실시간 통신
- SseService와 SseEmitter를 활용한 구현

**설계서에 추가 권장 사항**:
- 향후 API 설계서에 SSE 엔드포인트를 정식으로 추가하여 문서화 필요
- 프론트엔드에서 EventSource API를 통해 스트리밍 데이터 수신

---

## 구현 특징

### 1. 비동기 처리 패턴
- 로드맵 생성/재설계는 비동기로 처리 (202 Accepted 반환)
- 요청 ID를 통한 상태 조회 메커니즘 제공
- SSE를 통한 실시간 진행 상황 스트리밍 (추가 구현)

### 2. 인증 처리
- JWT 기반 인증 사용
- `@AuthenticationPrincipal String userId` 어노테이션으로 사용자 ID 주입
- Authorization 헤더를 외부 서비스 호출 시 전달 (로드맵 생성/재설계 시)

### 3. 검증
- `@Valid` 어노테이션을 통한 요청 데이터 검증
- Bean Validation 사용 (LifecycleEventRequest)

### 4. Swagger/OpenAPI 문서화
- `@Tag` 어노테이션으로 컨트롤러 그룹화
  - LifecycleEvents: 생애주기 이벤트 관리
  - Roadmap: 장기주거 로드맵
- `@Operation` 어노테이션으로 각 API 설명 추가
- `@ApiResponses` 어노테이션으로 응답 상태 코드 문서화
- API 설계서의 operationId와 일치

### 5. HTTP Status Code 활용
- 201 Created: 생애주기 이벤트 등록 성공 시
- 202 Accepted: 비동기 로드맵 생성/재설계 요청 접수 시
- 200 OK: 조회, 수정, 상태 조회 성공 시
- 204 No Content: 생애주기 이벤트 삭제 성공 시
- 에러 상황은 설계서의 명세를 따름 (400, 401, 404)

### 6. 외부 서비스 통합
- Authorization 헤더를 @RequestHeader로 수신하여 외부 서비스 호출 시 전달
- 로드맵 생성/재설계 시 User Service 및 AI Service와 통신

### 7. 버전 관리
- 로드맵 버전 이력 관리 (최대 3개)
- 특정 버전 조회 가능 (Query Parameter: version)

---

## 설계 준수 사항

### ✅ 완벽하게 준수된 항목
1. **Endpoint 경로**: 모든 API가 설계서의 경로와 정확히 일치
2. **HTTP Method**: 모든 API가 설계서의 메서드와 일치
3. **Request/Response DTO**: 모든 DTO가 설계서의 스키마와 일치
4. **인증 요구사항**: 설계서의 보안 요구사항과 일치
5. **User Story 매핑**: 각 API가 설계서의 User Story ID와 연결됨
6. **Controller 메서드명**: 설계서의 operationId와 일치
7. **비동기 처리**: 로드맵 생성/재설계의 비동기 처리 패턴 준수
8. **버전 관리**: 최대 3개 버전 유지 정책 준수
9. **HTTP Status Code**: 설계서의 상태 코드 정의 준수

### ⚠️ 설계서와의 차이점
1. **Authorization 헤더 전달**: 로드맵 생성/재설계 시 Authorization 헤더를 명시적으로 수신하여 외부 서비스 호출에 사용
   - 이는 구현 상세사항으로, 설계서에 명시되지 않았으나 실제 구현을 위해 필수적인 요소
2. **SSE 엔드포인트 추가**: `/roadmaps/tasks/{taskId}/stream` 엔드포인트 추가 구현
   - 설계서에 없는 기능이나, 사용자 경험 개선을 위한 추가 구현

---

## 권장 사항

### 1. API 설계서 업데이트
- SSE 스트리밍 엔드포인트를 정식 API로 설계서에 추가
- Authorization 헤더 전달 요구사항을 명시적으로 문서화

### 2. 에러 처리 강화
- 설계서에 명시된 에러 응답 형식 검증 필요
- GlobalExceptionHandler에서 일관된 에러 응답 구조 제공 확인

### 3. 테스트 코드 작성
- 각 API에 대한 단위 테스트 작성
- 비동기 처리 로직에 대한 통합 테스트 작성
- SSE 스트리밍 기능에 대한 테스트 작성

### 4. 문서화 개선
- Swagger UI를 통해 실제 API 문서가 설계서와 일치하는지 확인
- Response 예시가 설계서와 동일한지 검증
- SSE 엔드포인트 사용 예시 및 EventSource API 사용법 문서화

### 5. 비동기 처리 모니터링
- 로드맵 생성 작업의 타임아웃 처리 확인
- 실패 시 재시도 로직 검토
- 작업 큐 모니터링 체계 구축

---

## 결론

**Roadmap Service의 모든 설계서 API가 완벽하게 구현되었으며, 사용자 경험 개선을 위한 SSE 스트리밍 기능이 추가되었습니다.**

- ✅ 10개 설계서 API 모두 100% 준수
- ✅ 1개 추가 API 구현 (SSE 스트리밍)
- ✅ HTTP Method, Endpoint, DTO 모두 일치
- ✅ 인증 요구사항 준수
- ✅ User Story와 매핑 완료
- ✅ 비동기 처리 패턴 완벽 구현
- ✅ 버전 관리 정책 준수

설계서의 모든 요구사항이 충실하게 구현되었으며, 실시간 진행 상황 스트리밍 기능을 통해 사용자 경험이 한층 개선되었습니다. 추가된 SSE 엔드포인트는 향후 API 설계서에 정식으로 반영하는 것을 권장합니다.
