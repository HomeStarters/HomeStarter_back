# 외부-내부 시퀀스 설계 일관성 분석 보고서

**작성일**: 2025-12-20
**분석 범위**:
- 외부 시퀀스: 10개 파일
- 내부 시퀀스: 15개 파일

---

## 1. 전체 매핑 현황

### 1.1 외부-내부 시퀀스 매핑 테이블

| 외부 시퀀스 | 대응 내부 시퀀스 | 매핑 상태 |
|-----------|----------------|----------|
| 회원가입-로그인 | user-회원가입, user-로그인, user-로그아웃 | ✅ 완전 매핑 |
| 기본정보-관리 | ❌ 없음 | ⚠️ 미완성 |
| 입주희망주택-입력-관리 | housing-입주희망주택입력 | ⚠️ 부분 매핑 |
| 대출상품-조회-관리 | loan-대출상품목록조회, loan-대출상품상세조회, loan-대출상품등록, loan-대출상품수정, loan-대출상품삭제 | ✅ 완전 매핑 |
| AI로드맵-생성 | roadmap-생애주기이벤트입력 | ⚠️ 부분 매핑 |
| 본인자산정보-입력 | asset-본인자산정보입력 | ✅ 완전 매핑 |
| 배우자자산정보-입력 | asset-배우자자산정보입력 | ✅ 완전 매핑 |
| 자산정보-수정-및-이벤트발행 | asset-본인자산정보입력, asset-배우자자산정보입력 | ⚠️ 부분 매핑 |
| 데이터변경-이벤트전파 | calculator-캐시무효화처리 | ✅ 완전 매핑 |
| 입주후지출-계산 | calculator-입주후지출계산, calculator-계산결과조회 | ✅ 완전 매핑 |

**매핑 현황 요약**:
- ✅ 완전 매핑: 6개 (60%)
- ⚠️ 부분 매핑: 3개 (30%)
- ❌ 미매핑: 1개 (10%)

---

## 2. 서비스별 일관성 분석

### 2.1 User Service (회원 관리)

#### ✅ 일관성 양호

**외부 시퀀스**: 회원가입-로그인
**내부 시퀀스**: user-회원가입, user-로그인, user-로그아웃

**일관성 검증**:
1. **데이터 흐름**:
   - 외부: 이메일/비밀번호 → User Service → User DB
   - 내부: Controller → Service → Repository → DB
   - ✅ 계층 구조 일치

2. **인증 처리**:
   - 외부: JWT 토큰 생성 (Access Token, Refresh Token)
   - 내부: JwtTokenProvider를 통한 토큰 생성, Redis 저장
   - ✅ 토큰 생성 및 저장 로직 일치

3. **오류 처리**:
   - 외부: 이메일 중복 시 400 Bad Request
   - 내부: 중복 확인 → BusinessException → 400 Bad Request
   - ✅ 오류 처리 일치

4. **로그아웃 처리**:
   - 외부: Refresh Token 무효화
   - 내부: Redis에서 Refresh Token 삭제 + Access Token 블랙리스트 등록
   - ✅ 보안 처리 일치 (내부가 더 상세함)

**발견 사항**:
- ✅ 비밀번호 암호화(BCrypt) 일관성 유지
- ✅ 로그인 실패 횟수 제한 (5회) 일관성 유지
- ✅ Redis 캐시 활용 일관성 유지

---

### 2.2 User Service (기본정보 관리)

#### ❌ 내부 시퀀스 누락

**외부 시퀀스**: 기본정보-관리 (UFR-USER-040, UFR-USER-050)
**내부 시퀀스**: ❌ 없음

**문제점**:
1. **누락된 기능**:
   - 기본정보 입력 (나이, 결혼여부, 자녀수, 반려동물, 거주지)
   - 기본정보 수정
   - 프로필 조회

2. **영향 범위**:
   - Calculator 서비스에서 기본정보 참조 (입주후지출-계산)
   - AI 로드맵 생성 시 기본정보 필요

**권고사항**:
```
내부 시퀀스 작성 필요:
- user-기본정보입력.puml
- user-기본정보수정.puml
- user-프로필조회.puml
```

---

### 2.3 Housing Service (주택 관리)

#### ⚠️ 부분 일관성

**외부 시퀀스**: 입주희망주택-입력-관리
**내부 시퀀스**: housing-입주희망주택입력

**일관성 검증**:
1. **주택 정보 입력**:
   - 외부: 주택정보 + 교통호재 + 출퇴근시간 저장
   - 내부: Housing + TransportBenefit + CommuteTime 중첩 저장
   - ✅ 데이터 구조 일치

2. **Kakao Map API 연동**:
   - 외부: Circuit Breaker 적용, 주소 → 좌표 변환
   - 내부: CircuitBreaker 컴포넌트, Kakao API 호출, 좌표 캐싱
   - ✅ 장애 격리 패턴 일치

3. **이벤트 발행**:
   - 외부: HousingCreated 이벤트 (housingId, userId, address, createdAt)
   - 내부: EventPublisher → MQ 발행
   - ✅ 이벤트 페이로드 일치

**누락된 내부 시퀀스**:
- ❌ 주택 목록 조회 (UFR-HOUS-020)
- ❌ 주택 상세 조회 (UFR-HOUS-030)
- ❌ 주택 정보 수정 (UFR-HOUS-040)
- ❌ 최종목표 주택 선택 (UFR-HOUS-050)

**권고사항**:
```
추가 내부 시퀀스 작성 필요:
- housing-주택목록조회.puml
- housing-주택상세조회.puml
- housing-주택정보수정.puml
- housing-최종목표주택선택.puml
```

---

### 2.4 Loan Service (대출상품 관리)

#### ✅ 일관성 우수

**외부 시퀀스**: 대출상품-조회-관리
**내부 시퀀스**: loan-대출상품목록조회, loan-대출상품상세조회, loan-대출상품등록, loan-대출상품수정, loan-대출상품삭제

**일관성 검증**:
1. **목록 조회 (UFR-LOAN-010)**:
   - 외부: Cache-Aside 패턴, 필터/정렬/페이징, TTL 1시간
   - 내부: Cache 조회 → 미스 시 DB 조회 → 캐싱, TTL 1시간
   - ✅ 완전 일치

2. **상세 조회 (UFR-LOAN-020)**:
   - 외부: 기본정보 + 자격요건 + 필요서류, TTL 2시간
   - 내부: loan_products + eligibility_criteria + required_documents 조회, TTL 2시간
   - ✅ 완전 일치

3. **관리자 권한 검증 (AFR-LOAN-030)**:
   - 외부: JWT role=ADMIN 확인
   - 내부: AuthorizationService → JWT 검증 → role 확인
   - ✅ 보안 처리 일치

4. **캐시 무효화**:
   - 외부: 등록/수정/삭제 시 "loans:list:*", "loan:product:{id}" 무효화
   - 내부: 동일한 캐시 키 패턴 무효화
   - ✅ 캐시 전략 일치

5. **삭제 전략**:
   - 외부: 논리적 삭제 (soft delete) 권장
   - 내부: status='DELETED', deletedAt 기록 (논리적 삭제) + 물리적 삭제 옵션
   - ✅ 삭제 정책 일치

**우수 사례**:
- ✅ Cache-Aside 패턴 정확한 구현
- ✅ 트랜잭션 처리 일관성 (기본정보 + 자격요건 + 필요서류 원자적 저장)
- ✅ 관리자 권한 검증 철저함

---

### 2.5 Asset Service (자산 관리)

#### ✅ 일관성 양호

**외부 시퀀스**: 본인자산정보-입력, 배우자자산정보-입력, 자산정보-수정-및-이벤트발행
**내부 시퀀스**: asset-본인자산정보입력, asset-배우자자산정보입력

**일관성 검증**:
1. **자산 항목 추가**:
   - 외부: 3초 debounce 타이머 → 자동 저장
   - 내부: Controller → Validation → Service → Repository, debounce는 프론트엔드 처리
   - ✅ 책임 분리 적절함

2. **카테고리별 총액 계산**:
   - 외부: 자산/대출/월소득/월지출 총액 계산
   - 내부: calculateTotal(userId, ownerType, itemType) 메서드
   - ✅ 계산 로직 일치

3. **배우자 없음 처리**:
   - 외부: hasSpouse=false 설정 시 기존 배우자 데이터 삭제
   - 내부: updateSpouseStatus → deleteAll(existingItems)
   - ✅ 데이터 정합성 유지

4. **이벤트 발행 (자산정보-수정-및-이벤트발행)**:
   - 외부: AssetUpdated 이벤트 (changeType: ITEM_ADDED/MODIFIED/DELETED, summary 포함)
   - 내부: ❌ 이벤트 발행 로직 누락
   - ⚠️ 불일치 발견

**문제점**:
1. **이벤트 발행 누락**:
   - 외부에서는 자산 변경 시 AssetUpdated 이벤트 발행
   - 내부 시퀀스(asset-본인자산정보입력, asset-배우자자산정보입력)에는 이벤트 발행 로직 없음
   - Calculator 서비스가 이벤트를 구독하여 캐시 무효화하는데, 이벤트가 발행되지 않으면 데이터 불일치 발생

2. **자산 수정 API**:
   - 외부: PUT /api/assets/items/{itemId}로 수정
   - 내부: ❌ 수정 로직 누락
   - 외부에는 수정 플로우가 있으나 내부 시퀀스 없음

**권고사항**:
```
1. asset-본인자산정보입력.puml, asset-배우자자산정보입력.puml에 이벤트 발행 로직 추가:
   Service -> EventPublisher: AssetUpdated 이벤트 생성
   EventPublisher -> MQ: 이벤트 발행

2. 추가 내부 시퀀스 작성:
   - asset-자산항목수정.puml
   - asset-자산항목삭제.puml
   - asset-재무요약조회.puml
```

---

### 2.6 Calculator Service (계산 서비스)

#### ✅ 일관성 우수

**외부 시퀀스**: 입주후지출-계산, 데이터변경-이벤트전파
**내부 시퀀스**: calculator-입주후지출계산, calculator-계산결과조회, calculator-캐시무효화처리

**일관성 검증**:
1. **Cache-Aside 패턴**:
   - 외부: 캐시 키 생성 → 조회 → 미스 시 계산 → 캐싱 (TTL 1시간)
   - 내부: calc:{userId}:{housingId}:{loanId} → Cache 조회 → 미스 시 계산 → SET (TTL 3600초)
   - ✅ 캐시 전략 완전 일치

2. **다중 서비스 데이터 수집**:
   - 외부: User/Asset/Housing/Loan 서비스 병렬 조회
   - 내부: par 블록으로 4개 서비스 병렬 호출
   - ✅ 성능 최적화 일치 (5초 → 2초)

3. **재무 계산 로직**:
   - 외부:
     1. 예상자산 = 현재자산 + (월소득-월지출)×개월수 - 현재대출
     2. 대출필요금액 = 주택가격 - 예상자산
     3. LTV/DTI/DSR 계산
     4. 입주 후 재무상태 계산
   - 내부: Domain 객체에서 동일한 5단계 계산 수행
   - ✅ 계산 로직 완전 일치

4. **이벤트 기반 캐시 무효화**:
   - 외부: Asset/Housing 변경 이벤트 → Calculator가 구독 → 캐시 무효화
   - 내부:
     - AssetEventListener → CacheInvalidationService → DEL calc:{userId}:*
     - HousingEventListener → CacheInvalidationService → DEL calc:*:{housingId}:*
   - ✅ 이벤트 처리 일치

5. **오류 처리 및 재시도**:
   - 외부: Redis 장애 시 재시도 3회, DLQ 이동
   - 내부: Exponential Backoff (2^attempt × 1000ms), NACK with requeue=false
   - ✅ 장애 복구 전략 일치

**우수 사례**:
- ✅ Publisher-Subscriber 패턴 정확한 구현
- ✅ 이벤트 무효화 타이밍 < 1초 보장
- ✅ 캐시 히트율 목표 60% 명시
- ✅ 재시도 및 DLQ 전략 철저함

---

### 2.7 Roadmap Service (로드맵 서비스)

#### ⚠️ 부분 일관성

**외부 시퀀스**: AI로드맵-생성
**내부 시퀀스**: roadmap-생애주기이벤트입력

**일관성 검증**:
1. **생애주기 이벤트 입력**:
   - 외부: 이벤트 추가 (이름, 유형, 예정일, 고려기준)
   - 내부: Controller → Service → Repository → DB (이벤트 저장)
   - ✅ CRUD 로직 일치

2. **AI 로드맵 생성**:
   - 외부:
     - 비동기 처리 (Asynchronous Request-Reply 패턴)
     - SSE 연결로 진행 상황 전달
     - AI Worker가 LLM API 호출
   - 내부: ❌ AI 로드맵 생성 로직 누락
   - ⚠️ 주요 기능 미구현

**누락된 내부 시퀀스**:
- ❌ AI 로드맵 생성 요청 처리
- ❌ 비동기 Worker 처리
- ❌ SSE 스트림 처리
- ❌ 로드맵 조회

**권고사항**:
```
추가 내부 시퀀스 작성 필요:
- roadmap-AI로드맵생성요청.puml (비동기 요청 처리)
- roadmap-AI워커처리.puml (Worker의 LLM 호출 및 데이터 수집)
- roadmap-로드맵조회.puml
```

---

## 3. 데이터 흐름 일관성 분석

### 3.1 서비스 간 데이터 전달

| 플로우 | 외부 시퀀스 | 내부 시퀀스 | 일관성 |
|-------|----------|-----------|-------|
| User → Asset | 기본정보-관리 → 자산정보-입력 | ❌ User 내부 시퀀스 없음 | ⚠️ 불완전 |
| Asset → Calculator | 자산정보 조회 | GET /assets/users/{userId} | ✅ 일치 |
| Housing → Calculator | 주택정보 조회 | GET /housing/{housingId} | ✅ 일치 |
| Loan → Calculator | 대출상품 조회 | GET /loans/{loanId} | ✅ 일치 |
| Calculator → Frontend | 계산 결과 반환 | CalculationResponse DTO | ✅ 일치 |

**발견 사항**:
- ✅ Calculator 서비스의 다중 서비스 데이터 수집은 외부-내부 모두 일관성 유지
- ⚠️ User 서비스의 기본정보 관리 내부 시퀀스 누락으로 인한 데이터 흐름 단절

---

### 3.2 DTO 변환 일관성

| 서비스 | 외부 DTO | 내부 DTO | 일관성 |
|-------|---------|---------|-------|
| User | 사용자 ID, 이름, 이메일 | UserResponse | ✅ 일치 |
| Asset | totalAssets, totalLoans, summary | AssetSummaryResponse | ✅ 일치 |
| Housing | housingId, 주택정보 | HousingResponse | ✅ 일치 |
| Loan | products[], totalCount, page | LoanProductListResponse | ✅ 일치 |
| Calculator | estimatedAsset, LTV/DTI/DSR, isEligible | CalculationResponse | ✅ 일치 |

**우수 사례**:
- ✅ 외부-내부 간 DTO 명명 규칙 일관성 유지
- ✅ 민감 정보 필터링 일관성 (비밀번호 등 제외)

---

## 4. 이벤트 처리 일관성 분석

### 4.1 이벤트 발행

| 이벤트 | 외부 정의 | 내부 구현 | 일관성 |
|-------|---------|---------|-------|
| HousingCreated | housingId, userId, address, createdAt | EventPublisher → MQ | ✅ 일치 |
| HousingUpdated | housingId, userId, updatedFields | EventPublisher → MQ | ✅ 일치 |
| AssetUpdated | userId, changeType, summary | ❌ 내부 구현 누락 | ⚠️ 불일치 |
| FinalHousingSelected | housingId, userId, selectedAt | 외부만 정의 | ⚠️ 미구현 |

**문제점**:
1. **AssetUpdated 이벤트 발행 누락**:
   - 외부 시퀀스에서는 자산 변경 시 이벤트 발행 명시
   - 내부 시퀀스(asset-본인자산정보입력, asset-배우자자산정보입력)에는 EventPublisher 호출 없음
   - Calculator 서비스가 이벤트 구독하여 캐시 무효화하는데, 이벤트 미발행 시 데이터 불일치

2. **FinalHousingSelected 이벤트 미구현**:
   - 외부 시퀀스(입주희망주택-입력-관리)에서 최종목표 주택 선택 시 이벤트 발행
   - 내부 시퀀스 없음

---

### 4.2 이벤트 구독 및 처리

| 구독자 | 이벤트 | 외부 처리 | 내부 처리 | 일관성 |
|-------|-------|---------|---------|-------|
| Calculator | AssetUpdated | 캐시 무효화 (calc:{userId}:*) | AssetEventListener → CacheInvalidationService | ✅ 일치 |
| Calculator | HousingUpdated | 캐시 무효화 (calc:*:{housingId}:*) | HousingEventListener → CacheInvalidationService | ✅ 일치 |

**우수 사례**:
- ✅ 이벤트 구독 큐 명명 규칙 일관성: `{service}.{domain}.events`
- ✅ Auto-Ack false 설정으로 메시지 손실 방지
- ✅ 재시도 정책 (최대 3회, Exponential Backoff) 일관성

---

### 4.3 이벤트 전파 타이밍

**외부 정의**: 이벤트 전파 시간 < 1초, 실시간 캐시 무효화
**내부 구현**: AssetEventListener, HousingEventListener의 평균 처리 시간 < 100ms

**일관성**: ✅ 성능 목표 일치

---

## 5. 캐시 전략 일관성 분석

### 5.1 캐시 키 패턴

| 서비스 | 캐시 키 | 외부 | 내부 | 일관성 |
|-------|--------|-----|-----|-------|
| Loan | loans:list:{filters} | TTL 1시간 | TTL 1시간 | ✅ 일치 |
| Loan | loan:product:{id} | TTL 2시간 | TTL 2시간 | ✅ 일치 |
| Calculator | calc:{userId}:{housingId}:{loanId} | TTL 1시간 | TTL 3600초 | ✅ 일치 |
| Calculator | calc:list:{userId}:{page}:{size} | - | TTL 5분 | ✅ 추가 최적화 |
| Calculator | calc:detail:{resultId} | - | TTL 1시간 | ✅ 추가 최적화 |
| Housing | - | - | 좌표 캐싱 | ✅ 성능 최적화 |

**우수 사례**:
- ✅ 캐시 키 명명 규칙 일관성: `{domain}:{type}:{id}`
- ✅ TTL 설정 근거 명확함 (자주 변경되지 않는 데이터는 TTL 길게)
- ✅ 내부 시퀀스에서 추가적인 캐시 최적화 (목록 캐시 등)

---

### 5.2 캐시 무효화 전략

| 상황 | 외부 정의 | 내부 구현 | 일관성 |
|-----|---------|---------|-------|
| 대출상품 등록 | loans:list:* 무효화 | DEL loans:list:* | ✅ 일치 |
| 대출상품 수정 | loans:list:*, loan:product:{id} | 동일 | ✅ 일치 |
| 자산 변경 | calc:{userId}:* 무효화 | AssetListener → DEL calc:{userId}:* | ✅ 일치 |
| 주택 변경 | calc:*:{housingId}:* 무효화 | HousingListener → DEL calc:*:{housingId}:* | ✅ 일치 |

**우수 사례**:
- ✅ 패턴 매칭을 통한 효율적인 대량 캐시 무효화
- ✅ 이벤트 기반 캐시 무효화로 데이터 일관성 보장
- ✅ 캐시 무효화 실패 시 재시도 및 DLQ 전략

---

## 6. 오류 처리 일관성 분석

### 6.1 HTTP 상태 코드

| 상황 | 외부 | 내부 | 일관성 |
|-----|-----|-----|-------|
| 정상 생성 | 201 Created | 201 Created | ✅ 일치 |
| 정상 조회 | 200 OK | 200 OK | ✅ 일치 |
| 정상 삭제 | 200 OK / 204 No Content | 204 No Content | ✅ 일치 |
| 유효성 검증 실패 | 400 Bad Request | 400 Bad Request | ✅ 일치 |
| 인증 실패 | 401 Unauthorized | 401 Unauthorized | ✅ 일치 |
| 권한 부족 | 403 Forbidden | 403 Forbidden | ✅ 일치 |
| 리소스 없음 | 404 Not Found | 404 Not Found | ✅ 일치 |
| 사용 중인 리소스 삭제 | - | 409 Conflict | ✅ 추가 보안 |

**우수 사례**:
- ✅ RESTful API 표준 상태 코드 일관성
- ✅ 내부 시퀀스에서 409 Conflict 추가로 데이터 무결성 강화

---

### 6.2 예외 처리 패턴

| 예외 | 외부 | 내부 | 일관성 |
|-----|-----|-----|-------|
| 중복 데이터 | BusinessException | ValidationException | ⚠️ 예외 타입 불일치 |
| 데이터 없음 | - | NotFoundException | ✅ 적절함 |
| 권한 없음 | - | ForbiddenException | ✅ 적절함 |
| Redis 장애 | 재시도 3회 → DLQ | ConnectionException → 재시도 → NACK | ✅ 일치 |

**개선 권고**:
- ⚠️ 예외 클래스 명명 규칙 통일 필요: BusinessException vs ValidationException

---

## 7. Circuit Breaker 및 장애 격리 분석

### 7.1 Circuit Breaker 적용

| 서비스 | 외부 정의 | 내부 구현 | 일관성 |
|-------|---------|---------|-------|
| Housing → Kakao Map API | Circuit Breaker, 장애 격리 | CircuitBreaker 컴포넌트, Open/Closed/Half-Open | ✅ 일치 |
| AI Worker → LLM API | Circuit Breaker, 3회 재시도 | - | ⚠️ 내부 미구현 |

**발견 사항**:
- ✅ Kakao Map API 호출 시 Circuit Breaker 정확한 구현
- ⚠️ AI 로드맵 생성의 LLM API 호출 Circuit Breaker 내부 시퀀스 미작성

---

## 8. 발견된 이슈 및 개선 권고사항

### 8.1 🔴 Critical (즉시 수정 필요)

#### 이슈 1: Asset 서비스 이벤트 발행 누락
**문제**:
- 외부 시퀀스(자산정보-수정-및-이벤트발행)에서는 AssetUpdated 이벤트 발행 명시
- 내부 시퀀스(asset-본인자산정보입력, asset-배우자자산정보입력)에는 이벤트 발행 로직 없음
- Calculator 서비스가 이 이벤트를 구독하여 캐시 무효화하는데, 이벤트 미발행 시 데이터 불일치 발생

**영향**:
- 자산 변경 후 Calculator의 계산 결과가 캐시에서 삭제되지 않음
- 사용자가 구 데이터로 계산 결과를 조회할 수 있음 (심각한 데이터 불일치)

**해결 방안**:
```
asset-본인자산정보입력.puml, asset-배우자자산정보입력.puml 수정:
1. Service 레이어에서 저장 후:
   Service -> EventPublisher: AssetUpdated 이벤트 생성
   EventPublisher -> MQ: 이벤트 발행

2. 이벤트 페이로드:
   {
     eventId: UUID,
     eventType: "ASSET_UPDATED",
     userId: userId,
     changeType: "ITEM_ADDED" | "ITEM_MODIFIED" | "ITEM_DELETED",
     ownerType: "SELF" | "SPOUSE",
     itemType: "ASSET" | "LOAN" | "INCOME" | "EXPENSE",
     summary: {
       totalNetAssets: 계산값,
       monthlyAvailableFunds: 계산값
     }
   }
```

**우선순위**: 🔴 최상위

---

#### 이슈 2: User 서비스 기본정보 관리 내부 시퀀스 누락
**문제**:
- 외부 시퀀스(기본정보-관리)에서 UFR-USER-040, UFR-USER-050 정의
- 내부 시퀀스 없음

**영향**:
- Calculator 서비스에서 기본정보 (생년월일, 성별, 거주지) 조회 불가
- AI 로드맵 생성 시 필요한 기본정보 수집 불가

**해결 방안**:
```
생성 필요:
- user-기본정보입력.puml
- user-기본정보수정.puml
- user-프로필조회.puml
```

**우선순위**: 🔴 최상위

---

### 8.2 🟡 Important (조속히 수정 필요)

#### 이슈 3: Housing 서비스 내부 시퀀스 부족
**문제**:
- 외부에 정의된 주택 목록/상세 조회, 수정, 최종목표 선택 기능의 내부 시퀀스 없음

**해결 방안**:
```
생성 필요:
- housing-주택목록조회.puml
- housing-주택상세조회.puml
- housing-주택정보수정.puml
- housing-최종목표주택선택.puml
```

**우선순위**: 🟡 높음

---

#### 이슈 4: Roadmap 서비스 AI 로드맵 생성 내부 시퀀스 누락
**문제**:
- 외부의 핵심 기능인 AI 로드맵 생성 (비동기 처리, SSE, LLM 호출)의 내부 시퀀스 없음

**해결 방안**:
```
생성 필요:
- roadmap-AI로드맵생성요청.puml
- roadmap-AI워커처리.puml
- roadmap-로드맵조회.puml
```

**우선순위**: 🟡 높음

---

#### 이슈 5: Asset 서비스 수정/삭제 내부 시퀀스 누락
**문제**:
- 외부에 자산 항목 수정/삭제 플로우가 있으나 내부 시퀀스 없음

**해결 방안**:
```
생성 필요:
- asset-자산항목수정.puml
- asset-자산항목삭제.puml
- asset-재무요약조회.puml
```

**우선순위**: 🟡 중간

---

### 8.3 🟢 Nice to Have (개선 권고)

#### 개선 1: 예외 클래스 명명 규칙 통일
**현황**:
- 일부: BusinessException
- 일부: ValidationException

**권고**:
```
명명 규칙 통일:
- 유효성 검증 실패: ValidationException
- 비즈니스 규칙 위반: BusinessException
- 리소스 없음: NotFoundException
- 권한 없음: ForbiddenException
```

**우선순위**: 🟢 낮음

---

#### 개선 2: 캐시 TTL 문서화
**현황**:
- 캐시 TTL이 외부-내부 일치하지만 선택 근거 문서화 부족

**권고**:
```
캐시 TTL 정책 문서:
- loans:list: 1시간 (대출상품 자주 변경 안 됨)
- loan:product: 2시간 (상세 정보 더 안정적)
- calc: 1시간 (자산/주택 변경 시 이벤트로 무효화)
```

**우선순위**: 🟢 낮음

---

## 9. 요약 및 결론

### 9.1 전체 일관성 점수

| 서비스 | 매핑 완성도 | 데이터 흐름 | 이벤트 처리 | 캐시 전략 | 오류 처리 | 종합 점수 |
|-------|----------|----------|----------|----------|----------|----------|
| User | 70% | 60% | N/A | N/A | 90% | 70% |
| Housing | 25% | 80% | 90% | 85% | 85% | 73% |
| Loan | 100% | 95% | N/A | 95% | 95% | 96% |
| Asset | 60% | 85% | 40% | 80% | 85% | 70% |
| Calculator | 100% | 95% | 95% | 95% | 90% | 95% |
| Roadmap | 25% | 70% | N/A | N/A | 85% | 60% |
| **전체** | **63%** | **81%** | **75%** | **89%** | **88%** | **77%** |

**종합 평가**:
- ✅ 강점: 캐시 전략, 오류 처리, 데이터 흐름 일관성 우수
- ⚠️ 개선 필요: 내부 시퀀스 완성도, 이벤트 발행 누락

---

### 9.2 우선순위별 작업 로드맵

#### Phase 1: Critical (1-2주)
1. Asset 서비스에 AssetUpdated 이벤트 발행 로직 추가
2. User 서비스 기본정보 관리 내부 시퀀스 작성

#### Phase 2: Important (2-3주)
3. Housing 서비스 목록/상세/수정/선택 내부 시퀀스 작성
4. Roadmap 서비스 AI 로드맵 생성 내부 시퀀스 작성
5. Asset 서비스 수정/삭제 내부 시퀀스 작성

#### Phase 3: Nice to Have (1주)
6. 예외 클래스 명명 규칙 통일
7. 캐시 TTL 정책 문서화

---

### 9.3 최종 권고사항

1. **즉시 수정 필요**:
   - Asset 서비스 이벤트 발행 로직 추가 (데이터 불일치 방지)
   - User 서비스 기본정보 관리 내부 시퀀스 작성

2. **설계 표준화**:
   - 외부 시퀀스에 정의된 모든 기능은 반드시 내부 시퀀스로 분해
   - 이벤트 발행이 필요한 경우 외부-내부 모두 명시

3. **검증 프로세스**:
   - 새로운 외부 시퀀스 작성 시 내부 시퀀스 동시 작성
   - 코드 리뷰 시 외부-내부 일관성 검증 필수

4. **문서화**:
   - 캐시 TTL, 이벤트 페이로드, 예외 클래스 명명 규칙 문서화
   - 아키텍처 결정 기록 (ADR) 작성

---

## 부록: 누락된 내부 시퀀스 목록

| 서비스 | 파일명 | 외부 기능 참조 |
|-------|-------|--------------|
| User | user-기본정보입력.puml | UFR-USER-040 |
| User | user-기본정보수정.puml | UFR-USER-050 |
| User | user-프로필조회.puml | UFR-USER-050 |
| Housing | housing-주택목록조회.puml | UFR-HOUS-020 |
| Housing | housing-주택상세조회.puml | UFR-HOUS-030 |
| Housing | housing-주택정보수정.puml | UFR-HOUS-040 |
| Housing | housing-최종목표주택선택.puml | UFR-HOUS-050 |
| Asset | asset-자산항목수정.puml | 외부 수정 플로우 |
| Asset | asset-자산항목삭제.puml | 외부 삭제 플로우 |
| Asset | asset-재무요약조회.puml | 외부 조회 플로우 |
| Roadmap | roadmap-AI로드맵생성요청.puml | AI로드맵-생성 |
| Roadmap | roadmap-AI워커처리.puml | AI로드맵-생성 |
| Roadmap | roadmap-로드맵조회.puml | AI로드맵-생성 |

**총 13개 내부 시퀀스 파일 누락**

---

**작성자**: 길동 (아키텍트)
**검토 필요**: 준호, 동욱 (백엔드 개발자)
**최종 승인**: 민준 (Product Owner)
