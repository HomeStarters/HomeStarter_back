# Calculator Service API 매핑표

## 문서 정보
- **작성일**: 2025-12-30
- **서비스명**: Calculator Service
- **Controller**: CalculatorController
- **API 설계서**: design/backend/api/calculator-service-api.yaml
- **Controller 파일**: calculator-service/src/main/java/com/dwj/homestarter/calculator/controller/CalculatorController.java

---

## API 매핑 현황

### 전체 요약
| 구분 | 개수 |
|------|------|
| 설계서 API 총 개수 | 4 |
| 구현된 API 총 개수 | 4 |
| 설계서와 일치하는 API | 4 |
| 추가 구현된 API | 0 |
| 미구현 API | 0 |

---

## 상세 매핑표

### 1. 입주 후 지출 계산 (UFR-CALC-010)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | POST | POST | ✅ |
| **Endpoint** | /calculator/housing-expenses | /calculator/housing-expenses | ✅ |
| **Controller 메서드** | calculateHousingExpenses | calculateHousingExpenses | ✅ |
| **Request DTO** | HousingExpensesRequest | HousingExpensesRequest | ✅ |
| **Response DTO** | CalculationResultResponse | CalculationResultResponse | ✅ |
| **HTTP Status** | 201 Created | 201 Created | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (Authentication) | ✅ |
| **비고** | - | - | - |

**Request 필드 검증**:
- housingId (주택 ID): ✅
- loanProductId (대출상품 ID): ✅
- loanAmount (대출 금액): ✅
- loanTerm (대출 기간): ✅

**Response 필드 검증**:
- id (계산 결과 ID): ✅
- userId (사용자 ID): ✅
- housingId (주택 ID): ✅
- housingName (주택 이름): ✅
- loanProductId (대출상품 ID): ✅
- loanProductName (대출상품 이름): ✅
- calculatedAt (계산 일시): ✅
- financialStatus (재무 현황): ✅
- loanAnalysis (대출 분석): ✅
- afterMoveIn (입주 후 재무상태): ✅
- status (대출 가능 여부): ✅

---

### 2. 계산 결과 목록 조회 (UFR-CALC-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /calculator/results | /calculator/results | ✅ |
| **Controller 메서드** | getCalculationResults | getCalculationResults | ✅ |
| **Request DTO** | Query Parameters | Query Parameters | ✅ |
| **Response DTO** | CalculationResultListResponse | CalculationResultListResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (Authentication) | ✅ |
| **비고** | - | - | - |

**Query Parameters 검증**:
- housingId (주택 ID 필터): ✅
- status (충족 여부 필터): ✅
- sortBy (정렬 기준): ✅
- sortOrder (정렬 순서): ✅
- page (페이지 번호): ✅
- size (페이지 크기): ✅

**Response 필드 검증**:
- results (계산 결과 목록): ✅
  - id (계산 결과 ID): ✅
  - housingName (주택 이름): ✅
  - loanProductName (대출상품 이름): ✅
  - calculatedAt (계산 일시): ✅
  - status (대출 가능 여부): ✅
  - availableFunds (월 여유자금): ✅
- page (페이지 번호): ✅
- size (페이지 크기): ✅
- total (전체 개수): ✅

---

### 3. 계산 결과 상세 조회 (UFR-CALC-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | GET | GET | ✅ |
| **Endpoint** | /calculator/results/{id} | /calculator/results/{id} | ✅ |
| **Controller 메서드** | getCalculationResult | getCalculationResult | ✅ |
| **Request DTO** | Path Variable (id) | @PathVariable String id | ✅ |
| **Response DTO** | CalculationResultResponse | CalculationResultResponse | ✅ |
| **HTTP Status** | 200 OK | 200 OK | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (Authentication) | ✅ |
| **비고** | - | - | - |

**Path Variable 검증**:
- id (계산 결과 ID): ✅

**Response 필드 검증**:
- CalculationResultResponse와 동일 (1번 API 참조): ✅

---

### 4. 계산 결과 삭제 (UFR-CALC-020)

| 항목 | API 설계서 | Controller 구현 | 일치 여부 |
|------|-----------|----------------|----------|
| **HTTP Method** | DELETE | DELETE | ✅ |
| **Endpoint** | /calculator/results/{id} | /calculator/results/{id} | ✅ |
| **Controller 메서드** | deleteCalculationResult | deleteCalculationResult | ✅ |
| **Request DTO** | Path Variable (id) | @PathVariable String id | ✅ |
| **Response DTO** | - (204 No Content) | ResponseEntity&lt;Void&gt; | ✅ |
| **HTTP Status** | 204 No Content | 204 No Content | ✅ |
| **인증 필요** | Yes (BearerAuth) | Yes (Authentication) | ✅ |
| **비고** | - | - | - |

**Path Variable 검증**:
- id (계산 결과 ID): ✅

---

## 추가 구현된 API

### 없음

모든 API가 설계서에 명시된 대로 구현되었으며, 설계서에 없는 추가 API는 구현되지 않았습니다.

---

## 구현 특징

### 1. 인증 처리
- Spring Security의 `Authentication` 객체를 통한 인증 처리
- `authentication.getName()`으로 userId 추출
- 모든 API에서 인증된 사용자만 접근 가능

### 2. 검증
- `@Valid` 어노테이션을 통한 요청 데이터 검증
- Bean Validation 사용 (HousingExpensesRequest)

### 3. Swagger/OpenAPI 문서화
- `@Tag` 어노테이션으로 컨트롤러 그룹화 ("Calculator")
- `@Operation` 어노테이션으로 각 API 설명 추가
- `@Parameter` 어노테이션으로 파라미터 설명 추가
- API 설계서의 operationId와 일치

### 4. HTTP Status Code 활용
- 201 Created: 입주 후 지출 계산 성공 시
- 200 OK: 조회 성공 시
- 204 No Content: 삭제 성공 시
- 에러 상황은 설계서의 명세를 따름 (400, 401, 404)

### 5. 페이징 처리
- Spring Data의 `Pageable` 인터페이스 활용
- `PageRequest.of(page, size, sort)` 사용
- sortBy, sortOrder를 통한 동적 정렬 지원

### 6. 로깅
- Slf4j를 통한 로깅 처리
- 각 API 호출 시 userId, 주요 파라미터 로깅

---

## 설계 준수 사항

### ✅ 완벽하게 준수된 항목
1. **Endpoint 경로**: 모든 API가 설계서의 경로와 정확히 일치
2. **HTTP Method**: 모든 API가 설계서의 메서드와 일치
3. **Request/Response DTO**: 모든 DTO가 설계서의 스키마와 일치
4. **인증 요구사항**: 설계서의 보안 요구사항과 일치 (BearerAuth)
5. **User Story 매핑**: 각 API가 설계서의 User Story ID와 연결됨
6. **Controller 메서드명**: 설계서의 operationId와 일치
7. **HTTP Status Code**: 설계서의 응답 코드와 일치

### ⚠️ 주의 사항
- 없음: 모든 구현이 설계서를 완벽하게 따름

---

## 권장 사항

### 1. 에러 처리 강화
- 설계서에 명시된 에러 응답 형식 구현 확인 필요
- GlobalExceptionHandler에서 ErrorResponse 형식 검증 필요
- 404 Not Found: 주택 또는 대출상품을 찾을 수 없는 경우
- 401 Unauthorized: 인증 실패 시
- 400 Bad Request: 잘못된 요청 데이터

### 2. 테스트 코드 작성
- 각 API에 대한 단위 테스트 작성
- 통합 테스트로 설계서의 예시 데이터 검증
- 인증 처리 테스트 (userId 추출 검증)
- 페이징 및 정렬 기능 테스트

### 3. API 문서 동기화
- Swagger UI를 통해 실제 API 문서가 설계서와 일치하는지 확인
- Response 예시가 설계서와 동일한지 검증
- DTO 필드가 설계서 스키마와 일치하는지 확인

### 4. 비즈니스 로직 검증
- FinancialStatus, LoanAnalysis, AfterMoveIn 계산 로직 정확성 검증
- LTV, DTI, DSR 계산 로직 확인
- 대출 가능 여부 판단 로직 검증
- 월 대출상환액 계산 정확성 확인

### 5. 권한 검증
- 계산 결과 조회/삭제 시 본인 소유 데이터인지 확인 필요
- Service 레이어에서 userId 기반 권한 검증 구현 확인

---

## 결론

**Calculator Service의 모든 API가 설계서와 100% 일치합니다.**

- ✅ 4개 API 모두 설계서 스펙 준수
- ✅ 추가 구현된 API 없음
- ✅ HTTP Method, Endpoint, DTO 모두 일치
- ✅ 인증 요구사항 준수
- ✅ User Story와 매핑 완료
- ✅ 페이징 및 정렬 기능 구현
- ✅ Swagger 문서화 완료

설계서의 모든 요구사항이 충실하게 구현되었으며, 추가적인 API 구현이나 수정이 필요하지 않습니다.
