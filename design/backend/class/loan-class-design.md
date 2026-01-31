# Loan Service - 클래스 설계서

## 1. 개요

### 1.1 목적
Loan 서비스의 대출상품 관리 기능을 Layered Architecture 패턴으로 설계

### 1.2 설계 패턴
**Layered Architecture**
- Controller Layer: HTTP 요청/응답 처리
- Service Layer: 비즈니스 로직, 트랜잭션 관리
- Domain Layer: JPA 엔티티
- Repository Layer: 데이터 접근
- DTO Layer: 데이터 전송 객체

### 1.3 참조 문서
- API 명세서: `design/backend/api/loan-service-api.yaml`
- 내부 시퀀스 다이어그램: `design/backend/sequence/inner/loan-*.puml`

## 2. 클래스 다이어그램

### 2.1 전체 클래스 설계
- 파일: `design/backend/class/loan.puml`
- 내용: 모든 클래스의 상세 속성 및 메소드 포함

### 2.2 간소화 버전 + API 매핑
- 파일: `design/backend/class/loan-simple.puml`
- 내용: 간소화된 클래스 구조 + Controller 메소드별 API 매핑표

## 3. 레이어별 클래스 구성

### 3.1 Controller Layer
**패키지**: `com.dwj.homestarter.loan.controller`

#### 3.1.1 LoanProductController
일반 사용자용 대출상품 조회 API

**메소드**:
1. `getLoanProducts()` - 대출상품 목록 조회 (필터/정렬/페이징)
   - Endpoint: `GET /api/v1/loans`
   - User Story: UFR-LOAN-010
   - Parameters: housingType, sortBy, sortOrder, keyword, page, size
   - Response: `LoanProductListResponse`

2. `getLoanProductDetail()` - 대출상품 상세 조회
   - Endpoint: `GET /api/v1/loans/{id}`
   - User Story: UFR-LOAN-020
   - Response: `LoanProductResponse`

#### 3.1.2 LoanProductAdminController
관리자용 대출상품 관리 API

**메소드**:
1. `createLoanProduct()` - 대출상품 등록
   - Endpoint: `POST /api/v1/loans`
   - User Story: AFR-LOAN-030
   - Request: `CreateLoanProductRequest`
   - Response: `LoanProductResponse` (201 Created)

2. `updateLoanProduct()` - 대출상품 수정
   - Endpoint: `PUT /api/v1/loans/{id}`
   - User Story: AFR-LOAN-030
   - Request: `UpdateLoanProductRequest`
   - Response: `LoanProductResponse`

3. `deleteLoanProduct()` - 대출상품 삭제 (소프트 삭제)
   - Endpoint: `DELETE /api/v1/loans/{id}`
   - User Story: AFR-LOAN-030
   - Response: `ApiResponse`

### 3.2 Service Layer
**패키지**: `com.dwj.homestarter.loan.service`

#### 3.2.1 LoanProductService
대출상품 비즈니스 로직 처리

**주요 책임**:
- 대출상품 CRUD 작업
- 데이터 유효성 검증
- 캐시 관리 (Cache-Aside 패턴)
- DTO 변환

**메소드**:
- `getLoanProducts()` - 목록 조회 (캐시 우선)
- `getLoanProductById()` - 상세 조회 (캐시 우선)
- `createLoanProduct()` - 등록 (캐시 무효화)
- `updateLoanProduct()` - 수정 (캐시 무효화)
- `deleteLoanProduct()` - 삭제 (캐시 무효화)
- `validateLoanProductData()` - 데이터 검증 (오버로드)
- `generateCacheKey()` - 캐시 키 생성
- `invalidateListCache()` - 목록 캐시 무효화
- `invalidateProductCache()` - 상세 캐시 무효화

#### 3.2.2 AuthorizationService
JWT 인증 및 권한 확인

**메소드**:
- `verifyToken()` - JWT 토큰 검증
- `checkAdminRole()` - 관리자 권한 확인

#### 3.2.3 CacheService
Redis 캐시 관리

**메소드**:
- `get()` - 캐시 조회
- `set()` - 캐시 저장 (TTL 지정)
- `delete()` - 캐시 삭제
- `deletePattern()` - 패턴 매칭 캐시 삭제

### 3.3 Domain Layer
**패키지**: `com.dwj.homestarter.loan.domain`

#### 3.3.1 LoanProduct
JPA 엔티티, loan_products 테이블 매핑

**속성**:
- `id`: Long - 대출상품 ID (PK)
- `name`: String - 대출이름
- `loanLimit`: Long - 대출한도 (원)
- `ltvLimit`: Double - LTV 한도 (%)
- `dtiLimit`: Double - DTI 한도 (%)
- `dsrLimit`: Double - DSR 한도 (%)
- `interestRate`: Double - 금리 (연 %)
- `targetHousing`: String - 대상주택
- `incomeRequirement`: String - 소득요건
- `applicantRequirement`: String - 신청자요건
- `remarks`: String - 비고
- `active`: boolean - 활성화 여부
- `createdAt`: LocalDateTime - 등록일시
- `updatedAt`: LocalDateTime - 수정일시

### 3.4 Repository Layer
**패키지**: `com.dwj.homestarter.loan.repository`

#### 3.4.1 LoanProductRepository
JpaRepository 확장 인터페이스

**메소드**:
- `findAll()` - 전체 조회 (페이징)
- `findById()` - ID로 조회
- `findByActiveTrue()` - 활성화된 상품 조회
- `findByActiveTrueAndTargetHousingContaining()` - 대상주택 필터링
- `findByActiveTrueAndNameContainingOrTargetHousingContaining()` - 키워드 검색
- `save()` - 저장/수정
- `deleteById()` - 삭제
- `existsById()` - 존재 여부 확인
- `countByActiveTrue()` - 활성화된 상품 수

### 3.5 DTO Layer
**패키지**: `com.dwj.homestarter.loan.dto`

#### 응답 DTO
1. **LoanProductListResponse** - 목록 조회 응답
   - success: boolean
   - data: LoanProductListData
   - message: String

2. **LoanProductListData** - 목록 데이터
   - content: List<LoanProductDTO>
   - pageable: PageInfo

3. **LoanProductResponse** - 단일 상품 응답
   - success: boolean
   - data: LoanProductDTO
   - message: String

4. **LoanProductDTO** - 대출상품 정보
   - 모든 LoanProduct 필드 포함
   - `from(LoanProduct)` - Entity → DTO 변환 (static)

5. **PageInfo** - 페이징 정보
   - pageNumber: int
   - pageSize: int
   - totalElements: long
   - totalPages: int
   - `from(Page<?>)` - Page → PageInfo 변환 (static)

6. **ApiResponse** - 공통 응답
   - success: boolean
   - data: Object
   - message: String
   - `success()` - 성공 응답 생성 (static, 오버로드)

#### 요청 DTO
1. **CreateLoanProductRequest** - 대출상품 등록 요청
   - 필수 필드: name, loanLimit, ltvLimit, dtiLimit, dsrLimit, interestRate, targetHousing
   - 선택 필드: incomeRequirement, applicantRequirement, remarks
   - `toEntity()` - DTO → Entity 변환

2. **UpdateLoanProductRequest** - 대출상품 수정 요청
   - CreateLoanProductRequest 필드 + active: boolean

#### 에러 DTO
1. **ErrorResponse** - 에러 응답
   - success: boolean (항상 false)
   - error: ErrorDetail
   - `of()` - 에러 응답 생성 (static)

2. **ErrorDetail** - 에러 상세
   - code: String - 에러 코드
   - message: String - 에러 메시지
   - details: List<FieldError> - 필드별 에러

3. **FieldError** - 필드 에러
   - field: String - 필드명
   - message: String - 에러 메시지

#### 기타 DTO
1. **UserInfo** - 사용자 정보
   - userId: String
   - username: String
   - role: String

## 4. 설계 특징

### 4.1 캐시 전략
**Cache-Aside 패턴 적용**
- 목록 조회: TTL 1시간, 키 형식 `loans:list:{filters}`
- 상세 조회: TTL 2시간, 키 형식 `loan:product:{productId}`
- 등록/수정/삭제 시 관련 캐시 무효화

### 4.2 트랜잭션 관리
- 등록/수정/삭제 작업은 Service Layer에서 트랜잭션 관리
- Repository에서 데이터베이스 작업 수행

### 4.3 보안
- JWT Bearer Token 인증
- 일반 사용자: 조회 API만 접근 가능
- 관리자(ROLE_ADMIN): 모든 API 접근 가능

### 4.4 데이터 검증
- Controller: 파라미터 유효성 검증 (@Valid)
- Service: 비즈니스 룰 검증
  - 필수 필드 확인
  - 값 범위 검증 (금리: 0-100%, 한도: 0-100%)
  - 대출한도 최소값 검증

### 4.5 소프트 삭제
- 물리적 삭제 대신 `active = false`로 논리적 삭제
- 삭제된 데이터는 조회에서 제외
- 데이터 이력 추적 가능

## 5. API 엔드포인트 요약

| HTTP Method | Endpoint | Controller 메소드 | User Story | 인증 |
|-------------|----------|------------------|-----------|------|
| GET | /api/v1/loans | getLoanProducts() | UFR-LOAN-010 | 일반 사용자 |
| GET | /api/v1/loans/{id} | getLoanProductDetail() | UFR-LOAN-020 | 일반 사용자 |
| POST | /api/v1/loans | createLoanProduct() | AFR-LOAN-030 | 관리자 |
| PUT | /api/v1/loans/{id} | updateLoanProduct() | AFR-LOAN-030 | 관리자 |
| DELETE | /api/v1/loans/{id} | deleteLoanProduct() | AFR-LOAN-030 | 관리자 |

## 6. 에러 처리

### 6.1 HTTP 상태 코드
- 200 OK: 성공
- 201 Created: 생성 성공
- 400 Bad Request: 유효성 검증 실패
- 401 Unauthorized: 인증 실패
- 403 Forbidden: 권한 없음
- 404 Not Found: 리소스 없음
- 409 Conflict: 사용 중인 대출상품 삭제 시도
- 500 Internal Server Error: 서버 오류

### 6.2 에러 코드
- `VALIDATION_ERROR`: 입력값 검증 실패
- `UNAUTHORIZED`: 인증 필요
- `FORBIDDEN`: 권한 부족
- `NOT_FOUND`: 리소스 없음
- `LOAN_PRODUCT_IN_USE`: 사용 중인 대출상품 삭제 불가
- `INTERNAL_SERVER_ERROR`: 서버 오류

## 7. 패키지 구조

```
com.dwj.homestarter.loan
├── controller
│   ├── LoanProductController.java
│   └── LoanProductAdminController.java
├── service
│   ├── LoanProductService.java
│   ├── AuthorizationService.java
│   └── CacheService.java
├── domain
│   └── LoanProduct.java
├── repository
│   └── LoanProductRepository.java
└── dto
    ├── LoanProductListResponse.java
    ├── LoanProductListData.java
    ├── LoanProductResponse.java
    ├── LoanProductDTO.java
    ├── CreateLoanProductRequest.java
    ├── UpdateLoanProductRequest.java
    ├── PageInfo.java
    ├── ApiResponse.java
    ├── ErrorResponse.java
    ├── ErrorDetail.java
    ├── FieldError.java
    └── UserInfo.java
```

## 8. 다음 단계

1. **백엔드 개발**: 클래스 설계 기반 Java 코드 구현
2. **데이터베이스 설계**: loan_products 테이블 DDL 작성
3. **단위 테스트**: 각 레이어별 테스트 코드 작성
4. **통합 테스트**: API 엔드포인트 테스트
5. **성능 테스트**: 캐시 효율성 검증

## 9. 참고 사항

### 9.1 캐시 키 패턴
- 목록: `loans:list:{housingType}:{sortBy}:{sortOrder}:{keyword}:{page}:{size}`
- 상세: `loan:product:{id}`

### 9.2 데이터베이스 인덱스 권장
- `idx_active` ON loan_products(active)
- `idx_target_housing` ON loan_products(target_housing)
- `idx_interest_rate` ON loan_products(interest_rate)
- `idx_loan_limit` ON loan_products(loan_limit)
- `idx_created_at` ON loan_products(created_at)

### 9.3 JPA 연관관계
현재 설계에서는 LoanProduct 단일 엔티티만 존재
추후 확장 시 고려 사항:
- EligibilityRequirement 엔티티 (자격요건)
- RequiredDocument 엔티티 (필요서류)
- LoanProductHistory 엔티티 (변경 이력)
