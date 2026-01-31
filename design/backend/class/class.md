# User 서비스 클래스 설계서

## 1. 개요

### 1.1 설계 정보
- **서비스명**: User 서비스
- **설계 패턴**: Layered Architecture
- **패키지 그룹**: com.dwj.homestarter.user
- **작성일**: 2025-12-29

### 1.2 참조 문서
- API 명세서: `design/backend/api/user-service-api.yaml`
- 내부 시퀀스: `design/backend/sequence/inner/user-*.puml`
- 패키지 구조 표준: `claude/standard_package_structure.md`

---

## 2. 레이어 구성

### 2.1 Controller Layer
**책임**: HTTP 요청/응답 처리, 입력 검증, 인증/인가

#### UserController
모든 API 엔드포인트를 처리하는 컨트롤러

**메소드**:
- `registerUser()`: POST /users/register - 회원가입
- `loginUser()`: POST /users/login - 로그인
- `logoutUser()`: POST /users/logout - 로그아웃
- `getUserProfile()`: GET /users/profile - 기본정보 조회
- `updateUserProfile()`: PUT /users/profile - 기본정보 수정

---

### 2.2 Service Layer
**책임**: 비즈니스 로직 처리, 트랜잭션 관리

#### UserService (Interface)
사용자 관련 비즈니스 로직 인터페이스

**메소드**:
- `register()`: 회원가입 처리
- `login()`: 로그인 처리 및 토큰 발급
- `logout()`: 로그아웃 처리 및 토큰 무효화
- `getUserProfile()`: 프로필 조회
- `updateUserProfile()`: 프로필 수정
- `existsByUserId()`: 아이디 중복 확인
- `existsByEmail()`: 이메일 중복 확인

#### UserServiceImpl
UserService 구현체

**주요 로직**:
- 비밀번호 암호화 (BCrypt)
- 로그인 실패 횟수 관리 (Redis)
- 계정 잠금 처리 (5회 실패 시 30분)
- JWT 토큰 생성 및 관리
- 토큰 블랙리스트 관리

#### JwtTokenProvider
JWT 토큰 생성 및 검증

**메소드**:
- `generateAccessToken()`: Access Token 생성 (1시간 유효)
- `generateRefreshToken()`: Refresh Token 생성 (7일 유효)
- `validateToken()`: 토큰 유효성 검증
- `getUserIdFromToken()`: 토큰에서 사용자 ID 추출
- `getExpirationTime()`: 토큰 만료 시간 조회
- `getRemainingTime()`: 토큰 남은 유효 시간 계산

---

### 2.3 Repository Layer
**책임**: 데이터 영속성 처리

#### UserRepository
사용자 기본 정보 저장소

**메소드**:
- `findByUserId()`: 아이디로 조회
- `findByEmail()`: 이메일로 조회
- `existsByUserId()`: 아이디 존재 확인
- `existsByEmail()`: 이메일 존재 확인

#### UserProfileRepository
사용자 프로필 정보 저장소

**메소드**:
- `findByUserId()`: 사용자 ID로 프로필 조회
- `deleteByUserId()`: 사용자 ID로 프로필 삭제

---

### 2.4 Entity Layer
**책임**: 데이터베이스 테이블 매핑

#### UserEntity
사용자 기본 정보 엔티티

**속성**:
- id (Long): 기본키
- userId (String): 사용자 아이디
- name (String): 이름
- email (String): 이메일
- phoneNumber (String): 전화번호
- password (String): 암호화된 비밀번호
- role (String): 역할
- lastLoginAt (LocalDateTime): 마지막 로그인 시간
- createdAt (LocalDateTime): 생성 시간
- updatedAt (LocalDateTime): 수정 시간

#### UserProfileEntity
사용자 프로필 정보 엔티티

**속성**:
- id (Long): 기본키
- userId (String): 사용자 아이디 (FK)
- birthDate (LocalDate): 생년월일
- gender (Gender): 성별 (MALE, FEMALE)
- currentAddress (AddressEmbeddable): 현재 거주지
- userWorkplaceAddress (AddressEmbeddable): 본인 직장 주소
- spouseWorkplaceAddress (AddressEmbeddable): 배우자 직장 주소
- investmentPropensity (InvestmentPropensity): 투자 성향 (HIGH, MEDIUM, LOW)
- createdAt (LocalDateTime): 생성 시간
- updatedAt (LocalDateTime): 수정 시간

#### AddressEmbeddable
주소 임베디드 타입

**속성**:
- roadAddress (String): 도로명 주소
- jibunAddress (String): 지번 주소
- postalCode (String): 우편번호
- latitude (Double): 위도
- longitude (Double): 경도

---

### 2.5 Domain Layer
**책임**: 비즈니스 도메인 모델

#### User
사용자 도메인 모델
- 비즈니스 로직 처리
- Entity와 DTO 간 변환

#### UserProfile
사용자 프로필 도메인 모델
- 프로필 관련 비즈니스 로직
- Entity와 DTO 간 변환

#### Address
주소 도메인 모델
- 주소 관련 비즈니스 로직
- Embeddable과 DTO 간 변환

---

### 2.6 DTO Layer
**책임**: 계층 간 데이터 전송

#### Request DTO
- **UserRegisterRequest**: 회원가입 요청
- **UserLoginRequest**: 로그인 요청
- **UserProfileUpdateRequest**: 프로필 수정 요청
- **AddressRequest**: 주소 요청

#### Response DTO
- **UserRegisterResponse**: 회원가입 응답
- **UserLoginResponse**: 로그인 응답 (토큰 포함)
- **UserProfileResponse**: 프로필 응답
- **AddressResponse**: 주소 응답

#### Service DTO
- **UserRegisterDto**: 회원가입 서비스 DTO
- **UserLoginDto**: 로그인 서비스 DTO
- **UserDto**: 사용자 서비스 DTO
- **UserProfileDto**: 프로필 서비스 DTO
- **UserProfileUpdateDto**: 프로필 수정 서비스 DTO
- **AddressDto**: 주소 서비스 DTO

---

### 2.7 Config Layer
**책임**: 설정 및 필터

#### SecurityConfig
Spring Security 설정
- 인증/인가 설정
- PasswordEncoder 빈 등록
- SecurityFilterChain 설정

#### JwtAuthenticationFilter
JWT 인증 필터
- 요청 헤더에서 토큰 추출
- 토큰 유효성 검증
- 블랙리스트 확인
- SecurityContext 설정

#### RedisConfig
Redis 설정
- RedisConnectionFactory 설정
- RedisTemplate 빈 등록

#### SwaggerConfig
Swagger/OpenAPI 설정
- API 문서 자동 생성

---

## 3. 공통 컴포넌트

### 3.1 Common DTO

#### ApiResponse<T>
표준 API 응답 형식
- success (boolean): 성공 여부
- message (String): 응답 메시지
- data (T): 응답 데이터

---

### 3.2 Common Exception

#### BusinessException
비즈니스 로직 예외
- errorCode: 에러 코드

#### UnauthorizedException
인증 실패 예외
- errorCode: AUTH_XXX

#### NotFoundException
리소스 없음 예외
- errorCode: NOT_FOUND

#### ValidationException
입력 검증 예외
- errorCode: VALIDATION_ERROR

#### GlobalExceptionHandler
전역 예외 처리
- 모든 예외를 ApiResponse 형식으로 변환

---

## 4. 에러 코드

| 코드 | 설명 |
|------|------|
| USER_001 | 아이디 중복 |
| USER_002 | 이메일 중복 |
| USER_003 | 비밀번호 불일치 |
| USER_004 | 비밀번호 강도 부족 |
| USER_005 | 기본정보 없음 |
| AUTH_001 | 인증 정보 불일치 |
| AUTH_002 | 계정 잠금 |
| AUTH_003 | 유효하지 않은 토큰 |
| VALIDATION_ERROR | 입력 데이터 검증 실패 |
| INTERNAL_ERROR | 서버 내부 오류 |

---

## 5. 패키지 구조도

```
com.dwj.homestarter.user
├── UserApplication.java
├── controller
│   └── UserController.java
├── service
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   └── JwtTokenProvider.java
├── repository
│   ├── jpa
│   │   ├── UserRepository.java
│   │   └── UserProfileRepository.java
│   └── entity
│       ├── UserEntity.java
│       ├── UserProfileEntity.java
│       ├── AddressEmbeddable.java
│       ├── Gender.java
│       └── InvestmentPropensity.java
├── domain
│   ├── User.java
│   ├── UserProfile.java
│   └── Address.java
├── dto
│   ├── request
│   │   ├── UserRegisterRequest.java
│   │   ├── UserLoginRequest.java
│   │   ├── UserProfileUpdateRequest.java
│   │   └── AddressRequest.java
│   ├── response
│   │   ├── UserRegisterResponse.java
│   │   ├── UserLoginResponse.java
│   │   ├── UserProfileResponse.java
│   │   ├── UserBasicInfo.java
│   │   └── AddressResponse.java
│   └── service
│       ├── UserRegisterDto.java
│       ├── UserLoginDto.java
│       ├── UserDto.java
│       ├── UserProfileDto.java
│       ├── UserProfileUpdateDto.java
│       └── AddressDto.java
└── config
    ├── SecurityConfig.java
    ├── JwtAuthenticationFilter.java
    ├── RedisConfig.java
    └── SwaggerConfig.java

com.dwj.homestarter.common
├── dto
│   └── ApiResponse.java
└── exception
    ├── BusinessException.java
    ├── UnauthorizedException.java
    ├── NotFoundException.java
    ├── ValidationException.java
    └── GlobalExceptionHandler.java
```

---

## 6. 클래스 관계

### 6.1 주요 의존성
- UserController → UserService
- UserController → JwtTokenProvider
- UserServiceImpl → UserRepository
- UserServiceImpl → UserProfileRepository
- UserServiceImpl → JwtTokenProvider
- UserServiceImpl → PasswordEncoder
- UserServiceImpl → RedisTemplate
- JwtAuthenticationFilter → JwtTokenProvider

### 6.2 Entity-Domain 관계
- UserEntity ↔ User
- UserProfileEntity ↔ UserProfile
- AddressEmbeddable ↔ Address

### 6.3 DTO 변환 흐름
Request → Service DTO → Domain → Entity → DB
DB → Entity → Domain → Service DTO → Response

---

## 7. 보안 고려사항

### 7.1 비밀번호 보안
- BCrypt 알고리즘 사용
- Salt 자동 생성
- 최소 8자, 영문/숫자/특수문자 포함

### 7.2 토큰 보안
- JWT Access Token: 1시간 유효
- JWT Refresh Token: 7일 유효
- 로그아웃 시 블랙리스트 등록
- Redis에 Refresh Token 저장

### 7.3 계정 보안
- 5회 로그인 실패 시 30분 잠금
- Redis에 실패 횟수 저장
- 성공 시 실패 횟수 초기화

---

## 8. 기술 스택

### 8.1 프레임워크
- Spring Boot 3.x
- Spring Security
- Spring Data JPA

### 8.2 데이터베이스
- PostgreSQL (사용자 정보)
- Redis (토큰, 캐시)

### 8.3 보안
- JWT (jjwt)
- BCrypt

### 8.4 문서화
- SpringDoc OpenAPI (Swagger)

---

## 9. API - Controller 메소드 매핑표

| Controller 메소드 | HTTP 메소드 | API 경로 | 설명 | User Story |
|------------------|------------|---------|------|------------|
| registerUser | POST | /users/register | 회원가입 | UFR-USER-010 |
| loginUser | POST | /users/login | 로그인 | UFR-USER-020 |
| logoutUser | POST | /users/logout | 로그아웃 | UFR-USER-030 |
| getUserProfile | GET | /users/profile | 기본정보 조회 | UFR-USER-040 |
| updateUserProfile | PUT | /users/profile | 기본정보 수정 | UFR-USER-050 |

---

## 10. 설계 검증

### 10.1 API 명세서 일치성
✅ 모든 API 엔드포인트가 Controller에 메소드로 정의됨
✅ Request/Response DTO가 API 명세서와 일치
✅ 에러 코드가 API 명세서와 일치

### 10.2 내부 시퀀스 일치성
✅ 회원가입 시퀀스: UserController → UserService → UserRepository
✅ 로그인 시퀀스: 비밀번호 검증, 토큰 생성, Redis 저장
✅ 로그아웃 시퀀스: 토큰 무효화, 블랙리스트 등록
✅ 프로필 조회/수정 시퀀스: 인증 후 처리

### 10.3 패턴 준수
✅ Layered Architecture 패턴 적용
✅ 계층별 책임 분리
✅ 인터페이스 기반 설계

---

## 11. 결과물

### 11.1 클래스 다이어그램
- **전체 버전**: `design/backend/class/user.puml`
- **간소화 버전**: `design/backend/class/user-simple.puml`

### 11.2 문서
- **설계 문서**: `design/backend/class/class.md` (본 문서)

---

## 12. 다음 단계

1. **구현**: 패키지 구조에 따라 클래스 구현
2. **테스트**: 단위 테스트 및 통합 테스트 작성
3. **문서화**: JavaDoc 및 API 문서 작성
4. **검토**: 코드 리뷰 및 아키텍처 검증
