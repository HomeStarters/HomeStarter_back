# 내집마련 도우미 플랫폼 - 논리아키텍처 설계서

**작성일**: 2025-12-16
**작성자**: 아키텍트 홍길동

---

## 목차
1. [개요](#1-개요)
2. [서비스 아키텍처](#2-서비스-아키텍처)
3. [주요 사용자 플로우](#3-주요-사용자-플로우)
4. [데이터 흐름 및 캐싱 전략](#4-데이터-흐름-및-캐싱-전략)
5. [확장성 및 성능 고려사항](#5-확장성-및-성능-고려사항)
6. [보안 고려사항](#6-보안-고려사항)
7. [논리아키텍처 다이어그램](#7-논리아키텍처-다이어그램)

---

## 1. 개요

### 1.1 설계 원칙

**Context Map 스타일 논리 아키텍처**
- 서비스 내부 구조는 생략하고 **서비스 간 관계와 통신 전략**에 집중
- 클라우드 디자인 패턴을 적용한 마이크로서비스 아키텍처
- 사용자 관점의 컴포넌트 중심 설계

**핵심 설계 원칙**
1. **서비스 독립성**: 캐시를 통한 직접 의존성 최소화
2. **선택적 비동기**: 장시간 작업(AI 로드맵)만 비동기 처리
3. **캐시 우선**: Redis를 통한 성능 최적화
4. **이벤트 기반**: 데이터 변경 시 느슨한 결합으로 전파
5. **외부 API 보호**: Circuit Breaker와 Retry 패턴 적용

### 1.2 핵심 컴포넌트 정의

**API Gateway Layer**
- 단일 진입점 (Gateway Routing 패턴)
- JWT 인증, 로깅, Rate Limiting 중앙 처리
- 서비스별 라우팅 및 로드밸런싱

**마이크로서비스**
- **User**: 사용자 인증/인가, 기본정보 관리
- **Asset**: 본인/배우자 자산정보 관리 (복수 항목)
- **Loan**: 대출상품 정보 관리
- **Housing**: 입주희망 주택정보 관리 (중첩 데이터)
- **Calculator**: 재무계산, 다중 서비스 데이터 통합 (Cache-Aside)
- **Roadmap**: AI 기반 장기주거 로드맵 설계 (Async Request-Reply)

**인프라 레이어**
- **Redis Cache**: 계산 결과 캐싱, 성능 최적화
- **Message Queue (RabbitMQ)**: 비동기 처리, 이벤트 전파
- **Database (PostgreSQL)**: 서비스별 독립 DB

**외부 서비스**
- **카카오맵 API**: 주소 검색 (Circuit Breaker 적용)
- **LLM API (Claude/GPT)**: AI 로드맵 생성 (Circuit Breaker 적용)

---

## 2. 서비스 아키텍처

### 2.1 서비스별 책임

#### User 서비스
**책임**
- 회원가입, 로그인/로그아웃 처리
- JWT 토큰 발급 및 검증
- 사용자 기본정보 관리 (생년월일, 성별, 거주지, 직장위치, 투자성향)

**주요 API**
- `POST /users/register`: 회원가입
- `POST /users/login`: 로그인
- `POST /users/logout`: 로그아웃
- `GET /users/profile`: 기본정보 조회
- `PUT /users/profile`: 기본정보 수정

#### Asset 서비스
**책임**
- 본인/배우자 자산정보 관리
- 복수 항목 관리 (자산, 대출, 월소득, 월지출)
- 자산정보 변경 시 이벤트 발행 (Publisher-Subscriber)

**주요 API**
- `POST /assets/self`: 본인 자산정보 등록
- `POST /assets/spouse`: 배우자 자산정보 등록
- `GET /assets`: 자산정보 조회
- `PUT /assets/{id}`: 자산정보 수정
- `DELETE /assets/{id}`: 자산정보 삭제

**이벤트**
- `AssetUpdated`: 자산정보 변경 이벤트

#### Loan 서비스
**책임**
- 대출상품 정보 관리 (CRUD)
- 대출상품 조회 및 필터링
- 관리자 대출상품 관리

**주요 API**
- `GET /loans`: 대출상품 목록 조회
- `GET /loans/{id}`: 대출상품 상세 조회
- `POST /loans`: 대출상품 등록 (관리자)
- `PUT /loans/{id}`: 대출상품 수정 (관리자)
- `DELETE /loans/{id}`: 대출상품 삭제 (관리자)

#### Housing 서비스
**책임**
- 입주희망 주택정보 관리
- 복수 교통호재 및 출퇴근 시간 관리 (중첩 데이터)
- 최종목표 주택 선택
- 주택정보 변경 시 이벤트 발행

**주요 API**
- `POST /housing`: 주택정보 등록
- `GET /housing`: 주택 목록 조회
- `GET /housing/{id}`: 주택 상세 조회
- `PUT /housing/{id}`: 주택정보 수정
- `DELETE /housing/{id}`: 주택정보 삭제
- `PUT /housing/{id}/target`: 최종목표 설정

**이벤트**
- `HousingUpdated`: 주택정보 변경 이벤트

#### Calculator 서비스
**책임**
- 입주 후 지출 계산 (LTV/DTI/DSR)
- 대출 충족여부 판단
- 다중 서비스 데이터 통합 (User, Asset, Housing, Loan)
- 계산 결과 캐싱 (Cache-Aside)
- Asset/Housing 변경 이벤트 구독하여 캐시 무효화

**주요 API**
- `POST /calculator/expenses`: 입주 후 지출 계산
- `GET /calculator/results/{id}`: 계산 결과 조회
- `GET /calculator/history`: 계산 이력 조회

**Cache 전략**
- 캐시 키: `calc:{userId}:{housingId}:{loanId}:{assetHash}`
- TTL: 1시간
- 무효화: Asset/Housing 변경 이벤트 수신 시

#### Roadmap 서비스
**책임**
- 생애주기 이벤트 관리
- AI 기반 장기주거 로드맵 설계 (비동기)
- 전체 서비스 데이터 통합 및 LLM API 호출
- 로드맵 조회 및 수정

**주요 API**
- `POST /roadmap/events`: 생애주기 이벤트 등록
- `GET /roadmap/events`: 이벤트 목록 조회
- `POST /roadmap/generate`: 로드맵 생성 요청 (비동기)
- `GET /roadmap/tasks/{taskId}`: 작업 상태 조회
- `GET /roadmap/tasks/{taskId}/stream`: SSE 진행 상황 스트림
- `GET /roadmap`: 로드맵 조회
- `PUT /roadmap`: 로드맵 수정 (재생성)

**비동기 처리 (Asynchronous Request-Reply)**
- 로드맵 생성 요청 → 즉시 taskId 반환
- Message Queue에 작업 등록
- AI Worker가 비동기로 LLM API 호출
- SSE로 실시간 진행 상황 전달

### 2.2 서비스 간 통신 전략

#### 동기 통신 (REST API)
**사용 시나리오**: 즉시 응답이 필요한 단순 조회

- Calculator → User: 사용자 기본정보 조회
- Calculator → Asset: 자산정보 조회
- Calculator → Housing: 주택정보 조회
- Calculator → Loan: 대출상품 조회

**특징**
- HTTP REST API 호출
- Circuit Breaker 적용 (외부 API 장애 보호)
- Retry 패턴 적용 (일시적 오류 복구)

#### 비동기 통신 (Message Queue)
**사용 시나리오**: 장시간 작업 또는 이벤트 전파

1. **AI 로드맵 생성 (Asynchronous Request-Reply)**
   - Roadmap → Message Queue: 작업 등록
   - AI Worker → LLM API: 비동기 처리
   - SSE/WebSocket: 실시간 진행 상황 전달

2. **데이터 변경 이벤트 전파 (Publisher-Subscriber)**
   - Asset/Housing → Message Queue: 변경 이벤트 발행
   - Calculator → Message Queue: 이벤트 구독 및 캐시 무효화

#### 캐시 우선 (Cache-Aside)
**사용 시나리오**: 자주 조회되는 계산 결과

- Calculator: Redis 캐시 조회 → 히트 시 즉시 반환
- 캐시 미스 시 계산 수행 후 캐시 저장
- 캐시 히트율 60% 예상 → 성능 80% 개선

---

## 3. 주요 사용자 플로우

### 3.1 사용자 등록 및 정보 입력 플로우

**단계**
1. 회원가입 (User)
2. 로그인 (User)
3. 기본정보 입력 (User)
4. 본인 자산정보 입력 (Asset)
5. 배우자 자산정보 입력 (Asset)
6. 입주희망 주택 입력 (Housing)

**서비스 의존성**
- 순차적 단계별 진행
- 각 단계는 독립적으로 저장
- 이전 단계 완료 후 다음 단계 진행

### 3.2 입주 후 지출 계산 플로우

**단계**
1. 사용자가 주택 상세정보 화면에서 "계산하기" 클릭
2. 대출상품 선택 UI
3. Calculator 서비스 계산 요청
   - 캐시 확인 (Cache-Aside)
   - 캐시 미스 시 데이터 수집:
     - User: 기본정보 (동기)
     - Asset: 자산정보 (동기)
     - Housing: 주택정보 (동기)
     - Loan: 대출상품 (동기)
   - 재무계산 수행 (LTV/DTI/DSR)
   - 결과 캐싱 (Redis)
4. 계산 결과 표시

**성능 최적화**
- 캐시 히트 시: 5초 → 0.1초 (98% 개선)
- 캐시 미스 시: 5초 (4개 서비스 조회 + 계산)

### 3.3 AI 로드맵 생성 플로우 (비동기)

**단계**
1. 최종목표 주택 선택 (Housing)
2. 생애주기 이벤트 입력 (Roadmap)
3. 로드맵 생성 요청
   - 즉시 taskId 반환 (응답 시간 < 3초)
   - SSE 연결로 진행 상황 수신
4. Message Queue에 작업 등록
5. AI Worker 비동기 처리
   - 데이터 수집: User, Asset, Housing, Loan, Calculator 결과
   - LLM API 호출 (Circuit Breaker 적용)
   - 로드맵 생성 및 결과 저장
6. SSE로 실시간 진행 상황 전달
   - 30%: "데이터 수집 완료"
   - 60%: "AI 분석 중..."
   - 100%: "완료"
7. 로드맵 결과 표시

**비동기 처리 효과**
- 사용자 체감 응답 시간: 60초 → 3초 (95% 개선)
- 진행 상황 표시로 사용자 이탈률 감소

### 3.4 데이터 변경 이벤트 전파 플로우

**시나리오**: 자산정보 수정 후 계산 결과 무효화

**단계**
1. 사용자가 자산정보 수정 (Asset)
2. Asset 서비스 데이터 업데이트
3. `AssetUpdated` 이벤트 발행 (Publisher-Subscriber)
4. Message Queue에 이벤트 전달
5. Calculator 서비스 이벤트 구독
6. 해당 사용자의 캐시 무효화 (Redis)
7. 다음 계산 시 재계산 수행

**데이터 일관성**
- 이벤트 전파 시간: < 1초
- 실시간 캐시 무효화 보장

---

## 4. 데이터 흐름 및 캐싱 전략

### 4.1 데이터 흐름 원칙

**서비스별 독립 데이터베이스**
- 각 마이크로서비스는 자신의 데이터베이스 소유
- 서비스 간 데이터 공유 시 API 호출
- 데이터 중복 허용 (성능 우선)

**데이터 통합 지점**
- **Calculator 서비스**: 4개 서비스 데이터 통합 (User, Asset, Housing, Loan)
- **Roadmap 서비스**: 6개 서비스 데이터 통합 (전체)

### 4.2 캐싱 전략 (Cache-Aside)

#### Calculator 결과 캐싱

**캐시 키 전략**
```
calc:{userId}:{housingId}:{loanId}:{assetHash}
```

**캐시 TTL**
- 1시간 (자동 만료)

**캐시 무효화 전략**
1. **Asset 변경 시**: 해당 사용자의 모든 계산 결과 무효화
2. **Housing 변경 시**: 해당 주택의 모든 계산 결과 무효화
3. **이벤트 기반 무효화**: Publisher-Subscriber 패턴 활용

**캐시 히트율 목표**
- 60% 이상 (캐시 히트 시 98% 성능 개선)

#### 외부 API 응답 캐싱

**주소 검색 결과**
- 캐시 키: `addr:{query}`
- TTL: 24시간
- Circuit Breaker와 함께 사용

**대출상품 목록**
- 캐시 키: `loans:list:{filter}`
- TTL: 1시간
- 관리자 변경 시 무효화

### 4.3 데이터 일관성 보장

**이벤트 기반 아키텍처 (Publisher-Subscriber)**
- Asset/Housing 변경 → 이벤트 발행 → Calculator 캐시 무효화
- 전파 시간: < 1초
- 최종 일관성 (Eventual Consistency) 모델

**트랜잭션 경계**
- 각 서비스 내부: 강한 일관성 (ACID)
- 서비스 간: 최종 일관성 (이벤트 전파)

---

## 5. 확장성 및 성능 고려사항

### 5.1 수평 확장 (Scale-Out)

**상태 비저장 (Stateless) 서비스**
- 모든 마이크로서비스 인스턴스 추가 가능
- API Gateway 로드밸런싱
- 세션 정보 Redis 공유

**비동기 워커 확장**
- AI Worker 인스턴스 추가
- Message Queue를 통한 작업 분산
- 부하 증가 시 자동 스케일 아웃

### 5.2 성능 최적화

**캐싱 효과**
- Calculator 조회: 5초 → 0.1초 (캐시 히트 시)
- 계산 비용 80% 절감

**비동기 처리 효과**
- AI 로드맵 체감 응답 시간: 60초 → 3초
- 동시 연결 수 80% 감소

**데이터베이스 최적화**
- 서비스별 독립 DB → 병목 분산
- 읽기 복제본 추가 가능
- 인덱스 최적화 (조회 성능)

### 5.3 병목 지점 및 해결방안

**병목 1: Calculator 서비스 (다중 서비스 조회)**
- 해결: Cache-Aside 패턴으로 조회 횟수 60% 감소

**병목 2: AI 로드맵 생성 (장시간 처리)**
- 해결: Asynchronous Request-Reply 패턴으로 비동기 처리

**병목 3: 외부 API 장애**
- 해결: Circuit Breaker + Retry 패턴으로 장애 전파 방지

---

## 6. 보안 고려사항

### 6.1 인증 및 인가

**JWT 토큰 기반 인증**
- API Gateway에서 중앙화된 JWT 검증
- 토큰 만료 시간: 1시간
- Refresh Token: 14일

**서비스 간 통신**
- 내부 네트워크: Private Subnet
- 서비스 간 mTLS (상호 TLS) 권장

### 6.2 데이터 보안

**민감 데이터 암호화**
- 재무정보 (자산, 소득, 대출) DB 암호화
- 전송 중 데이터: HTTPS 필수

**개인정보 보호**
- 주민등록번호 미수집 (생년월일만 수집)
- GDPR/개인정보보호법 준수

### 6.3 API 보안

**Rate Limiting**
- 사용자당 분당 100회 제한
- API Gateway에서 중앙 관리

**DDoS 방어**
- API Gateway Rate Limiting
- Circuit Breaker로 과부하 방지

**입력 검증**
- 모든 API 입력값 검증
- SQL Injection, XSS 방어

---

## 7. 논리아키텍처 다이어그램

논리아키텍처 다이어그램은 별도 파일로 작성되었습니다:
- 파일: `design/backend/logical/logical-architecture.mmd`
- Mermaid 형식으로 작성
- 서비스 간 의존성 및 통신 전략 표현

### 7.1 다이어그램 주요 요소

**컴포넌트**
- 클라이언트 (Web/Mobile)
- API Gateway Layer
- 마이크로서비스 6개 (User, Asset, Loan, Housing, Calculator, Roadmap)
- 인프라 (Redis, Message Queue, Database)
- 외부 서비스 (카카오맵 API, LLM API)

**의존성 표현**
- 실선 화살표 (→): 동기적 의존성 (REST API)
- 비동기 화살표 (->>): 비동기 의존성 (Message Queue)
- 점선 화살표 (-->): 선택적 의존성 (Circuit Breaker)

**통신 패턴**
- Gateway Routing: 클라이언트 → API Gateway → 서비스
- Cache-Aside: Calculator ↔ Redis
- Asynchronous Request-Reply: Roadmap → Message Queue → AI Worker
- Publisher-Subscriber: Asset/Housing → Message Queue → Calculator
- Circuit Breaker: API Gateway/Roadmap -.-> 외부 API

---

## 8. 다음 단계

논리아키텍처 설계 완료 후 다음 설계 단계로 진행:

1. **API 설계**: OpenAPI 3.0 명세 작성
2. **외부 시퀀스 설계**: 서비스 간 API 호출 흐름
3. **내부 시퀀스 설계**: 서비스 내부 처리 흐름
4. **클래스 설계**: 도메인 모델 및 엔티티
5. **데이터 설계**: 서비스별 DB 스키마

---

**문서 버전**: 1.0
**최종 업데이트**: 2025-12-16
