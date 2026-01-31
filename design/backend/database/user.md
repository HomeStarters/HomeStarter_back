# User 서비스 데이터베이스 설계서

## 1. 설계 개요

### 1.1 설계 정보
- **서비스명**: User 서비스
- **데이터베이스**: PostgreSQL 15.x
- **스키마명**: user_service
- **캐시**: Redis 7.x
- **작성일**: 2025-12-29

### 1.2 참조 문서
- 클래스 설계서: `design/backend/class/class.md`
- ERD: `design/backend/database/user-erd.puml`
- 스키마 스크립트: `design/backend/database/user-schema.psql`

### 1.3 설계 원칙
- **데이터 독립성**: User 서비스 전용 데이터베이스 사용
- **정규화**: 3NF 정규화 적용
- **성능 최적화**: 적절한 인덱스 및 캐시 전략 적용
- **확장성**: 향후 확장 가능한 구조 설계

---

## 2. 데이터 설계 요약

### 2.1 테이블 목록

| 테이블명 | 설명 | 주요 용도 |
|---------|------|----------|
| users | 사용자 기본 정보 | 회원가입, 로그인, 인증 |
| user_profiles | 사용자 프로필 정보 | 개인정보, 선호도 관리 |

### 2.2 주요 특징
- **사용자 기본 정보와 프로필 분리**: 성능 최적화 및 관심사 분리
- **JSON 타입 활용**: 주소 정보를 JSON으로 저장하여 유연성 확보
- **Enum 타입 활용**: 성별, 투자 성향을 Enum으로 관리
- **캐시 전략**: 로그인 세션, 실패 횟수, 토큰 블랙리스트 Redis 저장

### 2.3 데이터 흐름
1. **회원가입**: users → user_profiles 순차 저장
2. **로그인**: users 조회 → Redis 세션 저장
3. **프로필 조회**: users + user_profiles JOIN 조회 → Redis 캐싱
4. **프로필 수정**: user_profiles 업데이트 → Redis 캐시 무효화

---

## 3. 테이블 상세 정의

### 3.1 users (사용자 기본 정보)

#### 테이블 설명
사용자 계정 기본 정보를 저장하는 핵심 테이블

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGSERIAL | PK | 기본키 (자동 증가) |
| user_id | VARCHAR(50) | NOT NULL, UNIQUE | 사용자 아이디 (로그인용) |
| name | VARCHAR(100) | NOT NULL | 사용자 이름 |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 이메일 주소 |
| phone_number | VARCHAR(20) | NOT NULL | 전화번호 (하이픈 포함) |
| password | VARCHAR(255) | NOT NULL | BCrypt 암호화된 비밀번호 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | 사용자 역할 (USER, ADMIN) |
| last_login_at | TIMESTAMP | NULL | 마지막 로그인 시간 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_users | id | PRIMARY KEY | 기본키 인덱스 |
| uk_users_user_id | user_id | UNIQUE | 아이디 중복 방지 및 로그인 성능 |
| uk_users_email | email | UNIQUE | 이메일 중복 방지 |
| idx_users_phone_number | phone_number | BTREE | 전화번호 검색 성능 |
| idx_users_last_login_at | last_login_at | BTREE | 활동 사용자 분석 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| chk_users_user_id_format | CHECK | user_id는 영문, 숫자만 허용 (4-20자) |
| chk_users_email_format | CHECK | 유효한 이메일 형식 |
| chk_users_role | CHECK | role IN ('USER', 'ADMIN') |
| chk_users_phone_format | CHECK | 전화번호 형식 (010-XXXX-XXXX) |

#### 비즈니스 규칙
- **아이디 규칙**: 4-20자, 영문 소문자, 숫자만 허용
- **비밀번호 규칙**: 최소 8자, 영문/숫자/특수문자 포함 (애플리케이션 레벨 검증)
- **이메일 규칙**: RFC 5322 표준 형식
- **전화번호 규칙**: 010-XXXX-XXXX 형식

---

### 3.2 user_profiles (사용자 프로필 정보)

#### 테이블 설명
사용자 상세 프로필 정보를 저장하는 테이블

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | BIGSERIAL | PK | 기본키 (자동 증가) |
| user_id | VARCHAR(50) | NOT NULL, UNIQUE, FK | 사용자 아이디 (users.user_id 참조) |
| birth_date | DATE | NOT NULL | 생년월일 |
| gender | VARCHAR(10) | NOT NULL | 성별 (MALE, FEMALE) |
| current_address | JSONB | NULL | 현재 거주지 주소 |
| user_workplace_address | JSONB | NULL | 본인 직장 주소 |
| spouse_workplace_address | JSONB | NULL | 배우자 직장 주소 |
| investment_propensity | VARCHAR(10) | NOT NULL | 투자 성향 (HIGH, MEDIUM, LOW) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### JSON 구조 (주소)

```json
{
  "roadAddress": "서울특별시 강남구 테헤란로 123",
  "jibunAddress": "서울특별시 강남구 역삼동 123-45",
  "postalCode": "06234",
  "latitude": 37.5012345,
  "longitude": 127.0398765
}
```

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_user_profiles | id | PRIMARY KEY | 기본키 인덱스 |
| uk_user_profiles_user_id | user_id | UNIQUE | 사용자당 하나의 프로필 보장 |
| idx_user_profiles_birth_date | birth_date | BTREE | 연령대별 분석 |
| idx_user_profiles_gender | gender | BTREE | 성별 분석 |
| idx_user_profiles_investment | investment_propensity | BTREE | 투자 성향별 분석 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| fk_user_profiles_user_id | FOREIGN KEY | user_id → users.user_id (CASCADE) |
| chk_user_profiles_gender | CHECK | gender IN ('MALE', 'FEMALE') |
| chk_user_profiles_investment | CHECK | investment_propensity IN ('HIGH', 'MEDIUM', 'LOW') |
| chk_user_profiles_birth_date | CHECK | birth_date >= '1900-01-01' AND birth_date <= CURRENT_DATE |

#### 비즈니스 규칙
- **1:1 관계**: 사용자당 하나의 프로필만 존재
- **생년월일**: 1900-01-01 ~ 현재 날짜
- **주소 정보**: 선택적 입력 (NULL 허용)
- **투자 성향**: 필수 입력 (회원가입 시 기본값 MEDIUM)

---

## 4. 관계 정의

### 4.1 ERD 관계

```
users (1) ─────< (1) user_profiles
  PK: id              PK: id
  UK: user_id         FK: user_id → users.user_id
```

### 4.2 관계 설명

| 관계 | 카디널리티 | 설명 |
|------|----------|------|
| users ↔ user_profiles | 1:1 | 사용자당 하나의 프로필 |

### 4.3 Cascade 정책

| 테이블 | FK 컬럼 | ON DELETE | ON UPDATE |
|--------|---------|-----------|-----------|
| user_profiles | user_id | CASCADE | CASCADE |

**설명**:
- 사용자 삭제 시 프로필도 함께 삭제
- 사용자 아이디 변경 시 프로필의 user_id도 자동 업데이트

---

## 5. 인덱스 전략

### 5.1 인덱스 목적별 분류

#### 5.1.1 성능 최적화 인덱스

| 테이블 | 인덱스명 | 컬럼 | 용도 |
|--------|---------|------|------|
| users | uk_users_user_id | user_id | 로그인 성능 (초당 1000+ 요청) |
| users | uk_users_email | email | 이메일 중복 확인 및 찾기 |
| users | idx_users_phone_number | phone_number | 전화번호 기반 검색 |
| user_profiles | uk_user_profiles_user_id | user_id | 프로필 조회 성능 |

#### 5.1.2 분석/통계 인덱스

| 테이블 | 인덱스명 | 컬럼 | 용도 |
|--------|---------|------|------|
| users | idx_users_last_login_at | last_login_at | 활성 사용자 분석 |
| user_profiles | idx_user_profiles_birth_date | birth_date | 연령대별 통계 |
| user_profiles | idx_user_profiles_gender | gender | 성별 통계 |
| user_profiles | idx_user_profiles_investment | investment_propensity | 투자 성향 분석 |

### 5.2 인덱스 설계 원칙
- **선택도(Selectivity) 우선**: 고유값이 많은 컬럼 우선 인덱싱
- **카디널리티 고려**: user_id, email (높음), gender (낮음)
- **복합 인덱스 최소화**: 현재 요구사항에서는 단일 컬럼 인덱스로 충분
- **주기적 재구성**: VACUUM ANALYZE 정기 실행

### 5.3 인덱스 유지보수
- **통계 수집**: 주 1회 ANALYZE 실행
- **인덱스 재구성**: 월 1회 REINDEX 실행
- **모니터링**: pg_stat_user_indexes 뷰로 사용률 모니터링

---

## 6. 캐시 전략

### 6.1 Redis 캐시 설계

#### 6.1.1 세션 관리 (Session)

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 로그인 사용자 세션 |
| **키 패턴** | `session:{userId}` |
| **값 구조** | JSON {accessToken, refreshToken, lastAccessTime} |
| **TTL** | 7일 (Refresh Token 만료 시간과 동일) |
| **용도** | 로그인 상태 유지, 토큰 검증 |

**예시**:
```
키: session:john123
값: {"accessToken":"eyJhbGc...", "refreshToken":"eyJhbGc...", "lastAccessTime":"2025-12-29T10:00:00"}
TTL: 604800초 (7일)
```

#### 6.1.2 로그인 실패 횟수 (Login Failure)

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 로그인 실패 횟수 |
| **키 패턴** | `login:fail:{userId}` |
| **값 구조** | INTEGER (실패 횟수) |
| **TTL** | 1800초 (30분) |
| **용도** | 5회 실패 시 계정 잠금 |

**예시**:
```
키: login:fail:john123
값: 3
TTL: 1800초 (30분)
```

**비즈니스 로직**:
- 로그인 실패 시: INCR login:fail:{userId}, EXPIRE 1800
- 5회 도달 시: 계정 잠금 (30분간 로그인 차단)
- 로그인 성공 시: DEL login:fail:{userId}

#### 6.1.3 토큰 블랙리스트 (Token Blacklist)

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 로그아웃된 토큰 |
| **키 패턴** | `token:blacklist:{tokenHash}` |
| **값 구조** | STRING "1" |
| **TTL** | 토큰 남은 유효 시간 |
| **용도** | 로그아웃 토큰 무효화 |

**예시**:
```
키: token:blacklist:a3b2c1d4...
값: "1"
TTL: 3600초 (Access Token 남은 시간)
```

**비즈니스 로직**:
- 로그아웃 시: SET token:blacklist:{tokenHash} "1" EX {remainingTime}
- 토큰 검증 시: EXISTS token:blacklist:{tokenHash} 확인

#### 6.1.4 사용자 프로필 캐시 (User Profile)

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 자주 조회되는 사용자 프로필 |
| **키 패턴** | `profile:{userId}` |
| **값 구조** | JSON (UserProfileDto) |
| **TTL** | 3600초 (1시간) |
| **용도** | 프로필 조회 성능 최적화 |

**예시**:
```
키: profile:john123
값: {"userId":"john123", "name":"홍길동", "email":"hong@example.com", ...}
TTL: 3600초 (1시간)
```

**무효화 전략**:
- 프로필 수정 시: DEL profile:{userId}
- 다음 조회 시 자동 재캐싱

### 6.2 캐시 정책 요약

| 캐시 유형 | TTL | 갱신 전략 | 무효화 조건 |
|----------|-----|---------|-----------|
| 세션 | 7일 | Refresh Token 갱신 시 | 로그아웃, 토큰 만료 |
| 로그인 실패 | 30분 | 실패 시마다 증가 | 로그인 성공, 30분 경과 |
| 토큰 블랙리스트 | 토큰 유효 시간 | 로그아웃 시 생성 | 토큰 만료 |
| 사용자 프로필 | 1시간 | 조회 시 자동 | 프로필 수정 |

### 6.3 캐시 모니터링
- **키 수 모니터링**: DBSIZE 명령으로 키 개수 추적
- **메모리 사용량**: INFO memory로 메모리 사용률 확인
- **Hit/Miss 비율**: Redis Insight로 캐시 효율성 분석
- **만료 키 정리**: 자동 만료 정책 활용 (eviction policy: volatile-ttl)

---

## 7. 데이터 무결성

### 7.1 트랜잭션 전략

#### 회원가입 트랜잭션
```sql
BEGIN;
  INSERT INTO users (...) VALUES (...);
  INSERT INTO user_profiles (...) VALUES (...);
COMMIT;
```

**격리 수준**: READ COMMITTED (PostgreSQL 기본값)

#### 프로필 수정 트랜잭션
```sql
BEGIN;
  UPDATE user_profiles SET ... WHERE user_id = ?;
  -- Redis 캐시 무효화 (애플리케이션 레벨)
COMMIT;
```

### 7.2 동시성 제어

| 작업 | 동시성 이슈 | 해결 방법 |
|------|-----------|---------|
| 회원가입 | 아이디/이메일 중복 | UNIQUE 제약조건 |
| 프로필 수정 | Lost Update | Optimistic Lock (version 컬럼 추가 시) |
| 로그인 | 동시 로그인 | Redis 세션 관리 |

### 7.3 백업 및 복구

| 항목 | 전략 |
|------|------|
| **백업 주기** | 일 1회 (새벽 3시) |
| **백업 방법** | pg_dump (논리 백업) |
| **보관 기간** | 30일 |
| **복구 시간** | 목표 1시간 이내 (RTO) |
| **데이터 손실** | 목표 24시간 이내 (RPO) |

---

## 8. 성능 최적화

### 8.1 쿼리 최적화 가이드

#### 8.1.1 로그인 쿼리
```sql
-- 최적화된 쿼리 (인덱스 활용)
SELECT id, user_id, password, role
FROM users
WHERE user_id = ?
LIMIT 1;

-- 실행 계획: Index Scan using uk_users_user_id
```

#### 8.1.2 프로필 조회 쿼리
```sql
-- 최적화된 쿼리 (JOIN + 인덱스)
SELECT
  u.user_id, u.name, u.email, u.phone_number,
  p.birth_date, p.gender, p.current_address,
  p.user_workplace_address, p.spouse_workplace_address,
  p.investment_propensity
FROM users u
INNER JOIN user_profiles p ON u.user_id = p.user_id
WHERE u.user_id = ?
LIMIT 1;

-- 실행 계획:
-- 1. Index Scan on uk_users_user_id
-- 2. Index Scan on uk_user_profiles_user_id
-- 3. Nested Loop Join
```

### 8.2 예상 성능 지표

| 작업 | 예상 응답 시간 | 근거 |
|------|--------------|------|
| 로그인 (DB) | < 10ms | UNIQUE 인덱스 활용 |
| 로그인 (캐시) | < 5ms | Redis 메모리 조회 |
| 프로필 조회 (DB) | < 20ms | JOIN + 인덱스 |
| 프로필 조회 (캐시) | < 5ms | Redis 메모리 조회 |
| 회원가입 | < 50ms | 2개 테이블 INSERT |

### 8.3 용량 계획

#### 초기 용량 (1년 기준)

| 항목 | 예상 값 |
|------|--------|
| 예상 사용자 수 | 100,000명 |
| users 테이블 크기 | ~30MB |
| user_profiles 테이블 크기 | ~50MB (JSON 포함) |
| 총 데이터베이스 크기 | ~100MB (인덱스 포함) |
| Redis 메모리 사용량 | ~500MB |

#### 확장 계획 (5년 기준)

| 항목 | 예상 값 |
|------|--------|
| 예상 사용자 수 | 1,000,000명 |
| users 테이블 크기 | ~300MB |
| user_profiles 테이블 크기 | ~500MB |
| 총 데이터베이스 크기 | ~1GB |
| Redis 메모리 사용량 | ~5GB |

---

## 9. 보안 고려사항

### 9.1 데이터 암호화

| 데이터 | 암호화 방법 | 보관 위치 |
|--------|-----------|----------|
| 비밀번호 | BCrypt (애플리케이션) | users.password |
| 토큰 | JWT (서명) | Redis session |
| 개인정보 | TLS 전송 암호화 | PostgreSQL |

### 9.2 접근 제어

| 역할 | 권한 | 설명 |
|------|------|------|
| app_user | SELECT, INSERT, UPDATE | 애플리케이션 계정 |
| admin_user | ALL | 관리자 계정 (제한적 사용) |
| readonly_user | SELECT | 분석/모니터링 계정 |

### 9.3 감사 로그

| 이벤트 | 로그 내용 | 저장 위치 |
|--------|---------|----------|
| 회원가입 | user_id, 가입 시간, IP | 애플리케이션 로그 |
| 로그인 | user_id, 로그인 시간, IP | 애플리케이션 로그 |
| 프로필 수정 | user_id, 변경 내용, 시간 | 애플리케이션 로그 |

---

## 10. 마이그레이션 전략

### 10.1 초기 구축
1. **스키마 생성**: `user-schema.psql` 실행
2. **권한 설정**: 애플리케이션 계정 권한 부여
3. **인덱스 생성**: 자동 생성 (스크립트 포함)
4. **검증**: 테스트 데이터 삽입 및 조회

### 10.2 향후 스키마 변경
- **Flyway 또는 Liquibase 사용**: 버전 관리
- **무중단 마이그레이션**: Blue-Green 배포
- **롤백 계획**: 각 마이그레이션마다 롤백 스크립트 준비

---

## 11. 모니터링 및 유지보수

### 11.1 모니터링 항목

| 항목 | 도구 | 임계값 |
|------|------|--------|
| 연결 수 | pg_stat_activity | > 80% max_connections |
| 쿼리 응답 시간 | pg_stat_statements | > 100ms |
| 테이블 크기 | pg_relation_size | 예상치 대비 200% 초과 |
| 인덱스 사용률 | pg_stat_user_indexes | < 50% |
| Redis 메모리 | INFO memory | > 80% maxmemory |

### 11.2 정기 유지보수

| 작업 | 주기 | 설명 |
|------|------|------|
| VACUUM ANALYZE | 주 1회 | 테이블 통계 갱신 |
| REINDEX | 월 1회 | 인덱스 재구성 |
| 백업 검증 | 월 1회 | 복구 테스트 |
| 로그 정리 | 주 1회 | 30일 이상 로그 삭제 |

---

## 12. 결과물

### 12.1 산출물 목록
- **설계 문서**: `design/backend/database/user.md` (본 문서)
- **ERD**: `design/backend/database/user-erd.puml`
- **스키마 스크립트**: `design/backend/database/user-schema.psql`

### 12.2 다음 단계
1. **ERD 작성**: PlantUML로 시각화
2. **스키마 스크립트 작성**: PostgreSQL DDL 생성
3. **문법 검사**: PlantUML 검증
4. **데이터베이스 구축**: 개발 환경 적용

---

## 13. 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|---------|
| 1.0 | 2025-12-29 | 길동 (아키텍트) | 초기 설계 완료 |
