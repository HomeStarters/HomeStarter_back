# 외부 시퀀스 설계서

**작성일**: 2025-12-16
**작성자**: 아키텍트 홍길동

---

## 📋 개요

내집마련 도우미 플랫폼의 외부 시퀀스 다이어그램입니다. 서비스 간 API 호출 흐름을 시각화하여 시스템 통합 방식을 명확히 보여줍니다.

### 설계 원칙

- **유저스토리 기반**: 모든 플로우는 유저스토리와 1:1 매칭
- **논리아키텍처 준수**: 논리아키텍처에 정의된 참여자와 일치
- **End-to-End 표현**: Frontend → API Gateway → Services → Database
- **한글 설명**: 모든 API 호출에 한글 설명 추가
- **PlantUML 표준**: `!theme mono` 적용, 동기/비동기 구분

---

## 📁 파일 목록

### 1. User 서비스 (독립 플로우)

#### 회원가입-로그인.puml
- **유저스토리**: UFR-USER-010, UFR-USER-020, UFR-USER-030
- **플로우**:
  - 회원가입 (이메일 중복 확인)
  - 로그인 (JWT 토큰 발급)
  - 로그아웃 (Refresh Token 무효화)
- **주요 특징**:
  - JWT Access Token + Refresh Token 발급
  - 비밀번호 암호화 처리
  - 토큰 기반 인증

#### 기본정보-관리.puml
- **유저스토리**: UFR-USER-040, UFR-USER-050
- **플로우**:
  - 기본정보 입력 (생년월일, 성별, 거주지 등)
  - 기본정보 조회 및 수정
- **주요 특징**:
  - JWT 토큰 검증
  - 401 Unauthorized 처리
  - 프로필 업데이트

---

### 2. Asset 서비스 (독립 플로우)

#### 본인자산정보-입력.puml
- **유저스토리**: UFR-ASST-010
- **플로우**:
  - 자산정보 입력 화면 진입
  - 자산 항목 추가 (자산, 대출, 월소득, 월지출)
  - 항목 수정/삭제
- **주요 특징**:
  - **3초 debounce 자동 저장**
  - 카테고리별 총액 실시간 계산
  - 복수 항목 관리

#### 배우자자산정보-입력.puml
- **유저스토리**: UFR-ASST-020
- **플로우**:
  - 배우자 없음 체크박스 처리
  - 배우자 자산 항목 추가
- **주요 특징**:
  - ownerType = 'SPOUSE' 구분
  - 배우자 없음 선택 시 데이터 삭제
  - 본인 자산정보와 동일한 구조

#### 자산정보-수정-및-이벤트발행.puml
- **유저스토리**: UFR-ASST-030
- **플로우**:
  - 자산정보 관리 화면 조회
  - 본인/배우자 탭 구분
  - 항목 추가/수정/삭제
  - **AssetUpdated 이벤트 발행**
- **주요 특징**:
  - 재무 요약 정보 자동 계산
  - Message Queue에 이벤트 발행
  - Calculator 서비스 연동 (재계산 트리거)

---

### 3. Loan 서비스 (독립 플로우)

#### 대출상품-조회-관리.puml
- **유저스토리**: UFR-LOAN-010, UFR-LOAN-020, AFR-LOAN-030
- **플로우**:
  - 대출상품 목록 조회 (필터/정렬)
  - 대출상품 상세 조회
  - 대출상품 관리 (관리자 전용)
- **주요 특징**:
  - Redis 캐시 활용 (TTL 1-2시간)
  - 페이징 및 검색 기능
  - 관리자 권한 검증 (JWT role=ADMIN)
  - 캐시 무효화 전략

---

### 4. Housing 서비스 (독립 플로우)

#### 입주희망주택-입력-관리.puml
- **유저스토리**: UFR-HOUS-010, UFR-HOUS-020, UFR-HOUS-030, UFR-HOUS-040, UFR-HOUS-050
- **플로우**:
  - 주택정보 입력 (중첩 데이터)
  - 주택 목록 조회
  - 주택 상세 조회
  - 주택정보 수정
  - 최종목표 주택 선택
- **주요 특징**:
  - **Kakao Map API 연동** (Circuit Breaker 적용)
  - 교통호재, 출퇴근 시간 중첩 데이터 관리
  - HousingUpdated 이벤트 발행
  - 최종목표 주택 1개만 선택

---

### 5. Calculator 서비스 (통합 플로우)

#### 입주후지출-계산.puml
- **유저스토리**: UFR-CALC-010
- **플로우**:
  - 입주 후 지출 계산 요청
  - **Cache-Aside 패턴** 적용
  - 다중 서비스 데이터 수집 (병렬 호출)
  - 재무 계산 수행 (LTV/DTI/DSR)
  - 계산 결과 캐싱
- **주요 특징**:
  - **4개 서비스 병렬 조회** (User, Asset, Housing, Loan)
  - Redis 캐시 조회 → 히트 시 0.1초, 미스 시 5초
  - 캐시 히트율 60% 예상 → 성능 80% 개선
  - 대출 충족여부 판단

---

### 6. Roadmap 서비스 (통합 플로우 - 비동기)

#### AI로드맵-생성.puml
- **유저스토리**: UFR-ROAD-010, UFR-ROAD-020
- **플로우**:
  - 생애주기 이벤트 입력
  - AI 로드맵 생성 요청 (비동기)
  - **Asynchronous Request-Reply 패턴**
  - SSE 실시간 진행 상황 전달
  - LLM API 호출 (Claude/GPT)
  - 로드맵 조회
- **주요 특징**:
  - 즉시 taskId 반환 (응답 시간 < 3초)
  - Message Queue에 작업 등록
  - AI Worker 비동기 처리
  - **SSE로 진행률 전달** (0% → 30% → 60% → 100%)
  - Circuit Breaker 적용 (LLM API 장애 보호)

---

### 7. 이벤트 기반 아키텍처 (통합 플로우)

#### 데이터변경-이벤트전파.puml
- **유저스토리**: UFR-ASST-030, UFR-HOUS-040와 연동
- **플로우**:
  - 자산정보 수정 → 캐시 무효화
  - 주택정보 수정 → 캐시 무효화
  - **Publisher-Subscriber 패턴**
  - Calculator 서비스 이벤트 구독
- **주요 특징**:
  - AssetUpdated, HousingUpdated 이벤트 발행
  - Message Queue (RabbitMQ) 활용
  - Redis 캐시 무효화
  - **이벤트 전파 시간 < 1초**
  - 최종적 일관성 (Eventual Consistency)

---

## 🔧 기술 스택

### API 통신
- **동기 호출**: REST API (HTTP/HTTPS)
- **비동기 호출**: Message Queue (RabbitMQ)
- **실시간 통신**: SSE (Server-Sent Events)

### 인프라 컴포넌트
- **API Gateway**: 단일 진입점, JWT 인증, Rate Limiting
- **Redis Cache**: 계산 결과 캐싱, 세션 관리
- **Message Queue (RabbitMQ)**: 비동기 처리, 이벤트 전파
- **PostgreSQL**: 서비스별 독립 DB

### 외부 서비스
- **Kakao Map API**: 주소 검색 (Circuit Breaker 적용)
- **LLM API (Claude/GPT)**: AI 로드맵 생성 (Circuit Breaker 적용)

---

## 🎯 핵심 패턴

### 1. Cache-Aside (입주후지출-계산)
- Redis 캐시 조회 → 히트 시 즉시 반환
- 캐시 미스 시 계산 수행 후 캐시 저장
- TTL 1시간

### 2. Asynchronous Request-Reply (AI로드맵-생성)
- 즉시 taskId 반환
- Message Queue에 작업 등록
- AI Worker 비동기 처리
- SSE로 실시간 진행 상황 전달

### 3. Publisher-Subscriber (데이터변경-이벤트전파)
- Asset/Housing 서비스가 이벤트 발행
- Calculator 서비스가 이벤트 구독
- 캐시 무효화 및 재계산 트리거

### 4. Circuit Breaker (외부 API 보호)
- Kakao Map API: 주소 검색 실패 시 기본값 사용
- LLM API: 3회 재시도 후 실패 처리
- 장애 격리 및 복구 전략

---

## 📊 성능 최적화

### 캐시 전략
- **Calculator 결과**: 캐시 히트율 60% → 성능 80% 개선
- **Loan 상품 목록**: TTL 1시간
- **Loan 상품 상세**: TTL 2시간

### 병렬 처리
- **입주 후 지출 계산**: 4개 서비스 병렬 조회 → 5초 → 2초 (60% 개선)
- **독립 플로우**: 서비스별 병렬 설계 및 구현

### 비동기 처리
- **AI 로드맵 생성**: 사용자 체감 응답 시간 60초 → 3초 (95% 개선)
- **이벤트 전파**: 데이터 변경 이벤트 전파 시간 < 1초

---

## ✅ 검증 항목

### 설계 원칙 준수
- [x] 유저스토리와 매칭
- [x] 논리아키텍처 참여자 일치
- [x] UI/UX설계서 플로우 참조
- [x] End-to-End 호출 순서 표현
- [x] 한글 설명 추가

### PlantUML 표준
- [x] `!theme mono` 적용
- [x] 동기/비동기 통신 구분
- [x] 참여자 정의 (Frontend, Gateway, Services, DB)
- [x] alt/else 분기 처리
- [x] note 추가로 상세 설명

### 아키텍처 패턴
- [x] Cache-Aside 패턴
- [x] Asynchronous Request-Reply 패턴
- [x] Publisher-Subscriber 패턴
- [x] Circuit Breaker 패턴

---

## 🔄 다음 단계

1. **내부 시퀀스 설계**: 각 서비스 내부 처리 흐름 설계
2. **클래스 설계**: 엔티티, 서비스, 레포지토리 클래스 설계
3. **데이터 설계**: 서비스별 데이터베이스 스키마 설계
4. **API 명세서 작성**: OpenAPI 3.0 기반 API 문서 작성

---

## 📝 참고 문서

- [유저스토리](../../userstory.md)
- [논리아키텍처](../logical/logical-architecture.md)
- [UI/UX설계서](../../uiux/uiux.md)
- [아키텍처 패턴](../../pattern/architecture-pattern.md)

---

**작성 완료일**: 2025-12-16
**담당 아키텍트**: 홍길동
