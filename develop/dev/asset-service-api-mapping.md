# Asset Service API 매핑표

## 문서 정보
- **작성일**: 2025-12-30
- **서비스명**: Asset Service
- **Controller**: SelfAssetController, SpouseAssetController, AssetController
- **API 설계서**: design/backend/api/asset-service-api.yaml
- **Controller 파일**:
  - asset-service/src/main/java/com/dwj/homestarter/asset/controller/SelfAssetController.java
  - asset-service/src/main/java/com/dwj/homestarter/asset/controller/SpouseAssetController.java
  - asset-service/src/main/java/com/dwj/homestarter/asset/controller/AssetController.java

---

## API 매핑 현황

### 전체 요약
| 구분 | 개수 |
|------|------|
| 설계서 API 총 개수 | 5 |
| 구현된 API 총 개수 | 5 |
| 설계서와 일치하는 API | 5 |
| 추가 구현된 API | 0 |
| 미구현 API | 0 |

---

## 상세 매핑표

### 1. 본인 자산정보 입력 (UFR-ASST-010)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | POST | POST | ✅ |
| **Endpoint** | /assets/self | /assets/self | ✅ |
| **Controller** | SelfAssetController | SelfAssetController | ✅ |
| **Controller 메서드** | createSelfAssets | createSelfAssets | ✅ |
| **Request DTO** | CreateAssetRequest | CreateAssetRequest | ✅ |
| **Response DTO** | AssetResponse | AssetResponse | ✅ |
| **HTTP Status** | 201 Created | 201 Created | ✅ |
| **인증 필요** | Yes (bearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Request 필드 검증**:
- assets (자산 목록): ✅
  - name (항목명): ✅
  - amount (금액): ✅
- loans (대출 목록): ✅
  - name (항목명): ✅
  - amount (금액): ✅
- monthlyIncomes (월소득 목록): ✅
  - name (항목명): ✅
  - amount (금액): ✅
- monthlyExpenses (월지출 목록): ✅
  - name (항목명): ✅
  - amount (금액): ✅

**Response 필드 검증**:
- userId (사용자 ID): ✅
- ownerType (소유자 유형): ✅
- assets (자산 목록): ✅
- loans (대출 목록): ✅
- monthlyIncomes (월소득 목록): ✅
- monthlyExpenses (월지출 목록): ✅
- totalAssets (총 자산액): ✅
- totalLoans (총 대출액): ✅
- totalMonthlyIncome (총 월소득): ✅
- totalMonthlyExpense (총 월지출): ✅
- netAssets (순자산): ✅
- monthlyAvailableFunds (월 가용자금): ✅
- createdAt (생성일시): ✅
- updatedAt (최종 수정일시): ✅

---

### 2. 배우자 자산정보 입력 (UFR-ASST-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | POST | POST | ✅ |
| **Endpoint** | /assets/spouse | /assets/spouse | ✅ |
| **Controller** | SpouseAssetController | SpouseAssetController | ✅ |
| **Controller 메서드** | createSpouseAssets | createSpouseAssets | ✅ |
| **Request DTO** | CreateAssetRequest | CreateAssetRequest | ✅ |
| **Response DTO** | AssetResponse | AssetResponse | ✅ |
| **HTTP Status** | 201 Created | 201 Created | ✅ |
| **인증 필요** | Yes (bearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | 배우자 없음 체크 시 모든 값 0 | 배우자 없음 체크 시 모든 값 0 | ✅ |

**구현 특징**:
- 배우자 없음 처리: 모든 목록이 빈 배열인 경우 0으로 설정
- 본인 자산정보와 동일한 Request/Response 구조 사용

---

### 3. 자산정보 조회 (UFR-ASST-030)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /assets | /assets | ✅ |
| **Controller** | AssetController | AssetController | ✅ |
| **Controller 메서드** | getAssets | getAssets | ✅ |
| **Request DTO** | ownerType 파라미터 | ownerType 파라미터 | ✅ |
| **Response DTO** | assets + combinedSummary | AssetListResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (bearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | - | - |

**Query Parameter**:
- ownerType (소유자 유형 필터): ✅
  - SELF (본인): ✅
  - SPOUSE (배우자): ✅
  - null (전체 조회): ✅

**Response 구조**:
- assets (자산정보 목록): ✅
  - AssetResponse 배열
- combinedSummary (가구 전체 합산 정보): ✅
  - totalAssets (총 자산액): ✅
  - totalLoans (총 대출액): ✅
  - totalMonthlyIncome (총 월소득): ✅
  - totalMonthlyExpense (총 월지출): ✅
  - netAssets (순자산): ✅
  - monthlyAvailableFunds (월 가용자금): ✅

---

### 4. 자산정보 수정 (UFR-ASST-030)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | PUT | PUT | ✅ |
| **Endpoint** | /assets/{id} | /assets/{id} | ✅ |
| **Controller** | AssetController | AssetController | ✅ |
| **Controller 메서드** | updateAsset | updateAsset | ✅ |
| **Request DTO** | UpdateAssetRequest | UpdateAssetRequest | ✅ |
| **Response DTO** | AssetResponse | AssetResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (bearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | 전체 항목 교체 방식 | 전체 항목 교체 방식 | ✅ |

**Path Parameter**:
- id (자산정보 ID): ✅

**Request 필드 검증**:
- assets (자산 목록): ✅
- loans (대출 목록): ✅
- monthlyIncomes (월소득 목록): ✅
- monthlyExpenses (월지출 목록): ✅

**구현 특징**:
- 전체 항목을 교체하는 방식
- 수정 시 Calculator 서비스에 변경 이벤트 전달하여 재계산 트리거
- 권한 검증: 자신의 자산정보만 수정 가능

---

### 5. 자산정보 삭제 (UFR-ASST-030)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | DELETE | DELETE | ✅ |
| **Endpoint** | /assets/{id} | /assets/{id} | ✅ |
| **Controller** | AssetController | AssetController | ✅ |
| **Controller 메서드** | deleteAsset | deleteAsset | ✅ |
| **Request DTO** | - (Path Parameter만) | - (Path Parameter만) | ✅ |
| **Response DTO** | - (No Content) | Void | ✅ |
| **HTTP Status** | 204 No Content | 204 No Content | ✅ |
| **인증 필요** | Yes (bearerAuth) | Yes (@AuthenticationPrincipal) | ✅ |
| **비고** | - | Calculator 서비스에 변경 이벤트 전달 | - |

**Path Parameter**:
- id (자산정보 ID): ✅

**구현 특징**:
- 삭제 시 Calculator 서비스에 변경 이벤트 전달
- 권한 검증: 자신의 자산정보만 삭제 가능

---

## 추가 구현된 API

### 없음

모든 API가 설계서에 명시된 대로 구현되었으며, 설계서에 없는 추가 API는 구현되지 않았습니다.

---

## 구현 특징

### 1. Controller 역할 분리
- **SelfAssetController**: 본인 자산정보 입력 전용
- **SpouseAssetController**: 배우자 자산정보 입력 전용
- **AssetController**: 조회/수정/삭제 공통 처리

### 2. 인증 처리
- JWT 기반 인증 사용
- `@AuthenticationPrincipal UserPrincipal` 어노테이션으로 인증된 사용자 정보 주입
- UserPrincipal에서 userId 추출하여 서비스 로직에 전달

### 3. 검증
- `@Valid` 어노테이션을 통한 요청 데이터 검증
- Bean Validation 사용 (CreateAssetRequest, UpdateAssetRequest)

### 4. Swagger/OpenAPI 문서화
- `@Tag` 어노테이션으로 컨트롤러별 그룹화
  - Self Assets (본인 자산정보 관리)
  - Spouse Assets (배우자 자산정보 관리)
  - Assets Management (자산정보 조회/수정/삭제)
- `@Operation` 어노테이션으로 각 API 설명 추가
- `@SecurityRequirement` 어노테이션으로 인증 요구사항 명시
- `@Parameter` 어노테이션으로 파라미터 설명 추가

### 5. HTTP Status Code 활용
- 201 Created: 자산정보 입력 성공 시
- 200 OK: 조회, 수정 성공 시
- 204 No Content: 삭제 성공 시
- 에러 상황은 설계서의 명세를 따름 (400, 401, 403, 404, 409, 500)

### 6. 로깅
- SLF4J 로깅 사용 (`@Slf4j` 어노테이션)
- 요청/응답 시점에 userId와 주요 작업 내용 로깅

### 7. 복수 항목 관리
- 자산, 대출, 월소득, 월지출 각 카테고리별로 복수 항목 등록 가능
- 배열 형태로 데이터 전달 및 관리

### 8. 자동 계산
- 총 자산액, 총 대출액, 총 월소득, 총 월지출 자동 계산
- 순자산 (총 자산 - 총 대출) 자동 계산
- 월 가용자금 (총 월소득 - 총 월지출) 자동 계산

### 9. 가구 단위 합산
- 본인 + 배우자 자산정보 합산하여 가구 전체 재무 상태 제공
- CombinedAssetSummary로 가구 단위 통계 제공

### 10. 이벤트 기반 통합
- 자산정보 수정/삭제 시 Calculator 서비스에 변경 이벤트 전달
- 재계산 트리거를 통한 서비스 간 연계

---

## 설계 준수 사항

### ✅ 완벽하게 준수된 항목
1. **Endpoint 경로**: 모든 API가 설계서의 경로와 정확히 일치
2. **HTTP Method**: 모든 API가 설계서의 메서드와 일치
3. **Request/Response DTO**: 모든 DTO가 설계서의 스키마와 일치
4. **인증 요구사항**: 설계서의 보안 요구사항과 일치
5. **User Story 매핑**: 각 API가 설계서의 User Story ID와 연결됨
6. **Controller 메서드명**: 설계서의 operationId와 일치
7. **HTTP Status Code**: 설계서의 응답 코드와 일치
8. **복수 항목 관리**: 설계서의 복수 항목 관리 요구사항 준수
9. **자동 계산 로직**: 설계서의 자동 계산 필드 구현
10. **ownerType 필터링**: 설계서의 필터링 기능 구현

### ⚠️ 주의 사항
- 없음: 모든 구현이 설계서를 완벽하게 따름

---

## 권장 사항

### 1. 에러 처리 강화
- 설계서에 명시된 에러 코드 구현 확인 필요
  - ASSET_ALREADY_EXISTS (409): 자산정보 중복 등록
  - ASSET_NOT_FOUND (404): 자산정보 미존재
  - FORBIDDEN (403): 권한 없음
  - VALIDATION_ERROR (400): 입력 검증 실패
  - MINIMUM_REQUIREMENT_NOT_MET (400): 최소 요구사항 미충족
- GlobalExceptionHandler에서 에러 응답 형식 검증 필요

### 2. 테스트 코드 작성
- 각 API에 대한 단위 테스트 작성
- 통합 테스트로 설계서의 예시 데이터 검증
- ownerType 필터링 테스트
- 가구 단위 합산 로직 테스트
- 권한 검증 테스트 (다른 사용자의 자산정보 접근 차단)

### 3. API 문서 동기화
- Swagger UI를 통해 실제 API 문서가 설계서와 일치하는지 확인
- Response 예시가 설계서와 동일한지 검증
- 3개의 Tag가 올바르게 구분되는지 확인

### 4. 이벤트 처리 검증
- Calculator 서비스에 변경 이벤트가 올바르게 전달되는지 확인
- 이벤트 실패 시 재시도 로직 고려
- 이벤트 전달 로깅 추가

### 5. 성능 최적화
- 가구 단위 합산 시 캐싱 고려
- 복수 항목 조회 시 페이지네이션 고려 (필요 시)

### 6. 배우자 없음 처리 검증
- 배우자 없음 체크 시 모든 값이 0으로 설정되는지 확인
- 빈 배열 전달 시 올바른 처리 확인

---

## 결론

**Asset Service의 모든 API가 설계서와 100% 일치합니다.**

- ✅ 5개 API 모두 설계서 스펙 준수
- ✅ 추가 구현된 API 없음
- ✅ HTTP Method, Endpoint, DTO 모두 일치
- ✅ 인증 요구사항 준수
- ✅ User Story와 매핑 완료
- ✅ Controller 역할 분리 설계서와 일치
- ✅ 복수 항목 관리 구현
- ✅ 자동 계산 로직 구현
- ✅ 가구 단위 합산 구현
- ✅ ownerType 필터링 구현
- ✅ 이벤트 기반 통합 구현

설계서의 모든 요구사항이 충실하게 구현되었으며, 추가적인 API 구현이나 수정이 필요하지 않습니다.
