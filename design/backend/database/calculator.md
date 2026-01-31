# Calculator 서비스 데이터베이스 설계서

## 개요
- **서비스명**: Calculator (재무 계산)
- **데이터베이스**: PostgreSQL 15
- **아키텍처 패턴**: Layered Architecture
- **설계 일시**: 2025-12-29

## 설계 참조 문서
- 클래스 설계서: `design/backend/class/calculator.md`
- API 명세서: `design/backend/api/calculator-service-api.yaml`
- ERD: `design/backend/database/calculator-erd.puml`
- 스키마 스크립트: `design/backend/database/calculator-schema.psql`

---

## 데이터베이스 전략

### 1. 전용 데이터베이스
- **데이터베이스명**: calculator_db
- **분리 이유**:
  - 마이크로서비스 독립성 보장
  - 계산 결과 데이터 독립적 관리
  - 스키마 변경 영향 격리
  - 성능 최적화 독립적 수행

### 2. 캐시 전략 (Cache-Aside Pattern)
- **캐시 레이어**: Redis
- **TTL 정책**:
  - 계산 결과 캐시: 3600초 (1시간)
  - 목록 캐시: 300초 (5분)
  - 상세 캐시: 3600초 (1시간)
- **캐시 키 패턴**:
  - 계산 결과: `calc:{userId}:{housingId}:{loanId}`
  - 목록: `calc:list:{userId}:{page}:{size}`
  - 상세: `calc:detail:{resultId}`
- **무효화 전략**:
  - Asset 변경 시: `calc:{userId}:*` 패턴 삭제
  - Housing 변경 시: `calc:*:{housingId}:*` 패턴 삭제
  - 신규 계산 시: 목록 캐시 무효화
  - 결과 삭제 시: 상세 및 목록 캐시 무효화

### 3. 이력 관리
- **저장 정책**: 모든 계산 결과 영구 저장
- **이력 목적**:
  - 사용자 계산 히스토리 추적
  - 주택별 계산 결과 비교 분석
  - 재무 상태 변화 추이 확인
- **삭제 정책**: 사용자 요청 시에만 삭제 (Soft Delete 미사용)

---

## 테이블 설계

### calculation_results (계산 결과)

**테이블 설명**: 입주 후 지출 계산 결과를 저장하는 핵심 테이블

#### 컬럼 정의

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 계산 결과 ID (PK) |
| user_id | VARCHAR(36) | NOT NULL | - | 사용자 ID |
| housing_id | VARCHAR(36) | NOT NULL | - | 주택 ID |
| housing_name | VARCHAR(200) | NOT NULL | - | 주택 이름 |
| loan_product_id | VARCHAR(36) | NOT NULL | - | 대출상품 ID |
| loan_product_name | VARCHAR(200) | NOT NULL | - | 대출상품 이름 |
| loan_amount | BIGINT | NOT NULL | - | 대출 금액 (원) |
| loan_term | INTEGER | NOT NULL | - | 대출 기간 (개월) |
| current_assets | BIGINT | NOT NULL | - | 현재 순자산 (원) |
| estimated_assets | BIGINT | NOT NULL | - | 예상자산 (원) |
| loan_required | BIGINT | NOT NULL | - | 대출필요금액 (원) |
| ltv | DECIMAL(5,2) | NOT NULL | - | 계산된 LTV (%) |
| dti | DECIMAL(5,2) | NOT NULL | - | 계산된 DTI (%) |
| dsr | DECIMAL(5,2) | NOT NULL | - | 계산된 DSR (%) |
| ltv_limit | DECIMAL(5,2) | NOT NULL | - | LTV 한도 (%) |
| dti_limit | DECIMAL(5,2) | NOT NULL | - | DTI 한도 (%) |
| dsr_limit | DECIMAL(5,2) | NOT NULL | - | DSR 한도 (%) |
| is_eligible | BOOLEAN | NOT NULL | - | 대출 적격 여부 |
| ineligibility_reasons | TEXT | NULL | - | 미충족 사유 (JSON) |
| monthly_payment | BIGINT | NOT NULL | - | 월 상환액 (원) |
| after_move_in_assets | BIGINT | NOT NULL | - | 입주 후 자산 (원) |
| after_move_in_monthly_expenses | BIGINT | NOT NULL | - | 입주 후 월지출 (원) |
| after_move_in_monthly_income | BIGINT | NOT NULL | - | 월소득 (원) |
| after_move_in_available_funds | BIGINT | NOT NULL | - | 여유자금 (원) |
| status | VARCHAR(20) | NOT NULL | - | 상태 (ELIGIBLE, INELIGIBLE) |
| calculated_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 계산 일시 |
| created_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 생성 일시 |
| updated_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 수정 일시 |

#### 제약 조건

**Primary Key**:
```sql
PRIMARY KEY (id)
```

**Check Constraints**:
```sql
-- 금액 필드는 0 이상
CHECK (loan_amount >= 0)
CHECK (loan_required >= 0)
CHECK (current_assets >= 0)
CHECK (estimated_assets >= 0)
CHECK (after_move_in_assets >= 0)
CHECK (monthly_payment >= 0)
CHECK (after_move_in_monthly_expenses >= 0)
CHECK (after_move_in_monthly_income >= 0)

-- 비율 필드는 0~200% 범위
CHECK (ltv >= 0 AND ltv <= 200)
CHECK (dti >= 0 AND dti <= 200)
CHECK (dsr >= 0 AND dsr <= 200)
CHECK (ltv_limit >= 0 AND ltv_limit <= 200)
CHECK (dti_limit >= 0 AND dti_limit <= 200)
CHECK (dsr_limit >= 0 AND dsr_limit <= 200)

-- 대출 기간은 1~600개월 (최대 50년)
CHECK (loan_term >= 1 AND loan_term <= 600)

-- 상태 값 검증
CHECK (status IN ('ELIGIBLE', 'INELIGIBLE'))
```

#### 인덱스 전략

**조회 패턴 분석**:
1. 사용자별 계산 결과 목록 조회 (높은 빈도)
2. 사용자 + 주택별 계산 결과 조회 (중간 빈도)
3. 사용자 + 상태별 계산 결과 조회 (중간 빈도)
4. 최근 계산 결과 정렬 조회 (높은 빈도)
5. 주택별 영향받는 사용자 식별 (낮은 빈도, 캐시 무효화용)

**인덱스 설계**:
```sql
-- 1. 사용자별 조회 최적화 (가장 빈번한 패턴)
CREATE INDEX idx_user_id ON calculation_results(user_id);

-- 2. 주택별 조회 및 캐시 무효화 최적화
CREATE INDEX idx_housing_id ON calculation_results(housing_id);

-- 3. 사용자 + 상태 복합 조회 최적화
CREATE INDEX idx_user_status ON calculation_results(user_id, status);

-- 4. 계산일시 기준 정렬 최적화 (DESC로 최근 결과 우선)
CREATE INDEX idx_calculated_at ON calculation_results(calculated_at DESC);

-- 5. 사용자 + 주택 복합 조회 최적화
CREATE INDEX idx_user_housing ON calculation_results(user_id, housing_id);

-- 6. 사용자 + 주택 + 상태 복합 조회 (선택적)
CREATE INDEX idx_user_housing_status ON calculation_results(user_id, housing_id, status);
```

**인덱스 선택 기준**:
- **필수 인덱스**: idx_user_id, idx_calculated_at (조회 성능 핵심)
- **권장 인덱스**: idx_user_status, idx_user_housing (빈번한 복합 조회)
- **선택 인덱스**: idx_user_housing_status (데이터량에 따라 결정)

---

## 데이터 흐름

### 1. 계산 결과 저장 (Create)
```
1. 계산 결과 생성 요청
2. CalculatorDomain 계산 수행
3. CalculationResultEntity 생성
4. PostgreSQL 저장
5. Redis 캐시 저장 (TTL: 1시간)
6. 응답 반환
```

### 2. 계산 결과 조회 (Read)
```
1. 조회 요청 수신
2. Redis 캐시 확인
3-1. 캐시 히트: Redis 데이터 반환
3-2. 캐시 미스:
     - PostgreSQL 조회
     - Redis 캐시 저장
     - 데이터 반환
```

### 3. 계산 결과 목록 조회 (List)
```
1. 목록 조회 요청 (페이징)
2. Redis 목록 캐시 확인
3-1. 캐시 히트: Redis 데이터 반환
3-2. 캐시 미스:
     - PostgreSQL 페이징 조회 (인덱스 활용)
     - Redis 캐시 저장 (TTL: 5분)
     - 목록 반환
```

### 4. 계산 결과 삭제 (Delete)
```
1. 삭제 요청 수신
2. 권한 확인 (user_id 일치)
3. PostgreSQL 삭제
4. Redis 캐시 무효화:
   - calc:detail:{resultId}
   - calc:list:{userId}:*
5. 응답 반환
```

### 5. 캐시 무효화 (Asset/Housing 변경 시)
```
[Asset 변경]
1. AssetUpdatedEvent 수신
2. userId 추출
3. Redis 패턴 삭제: calc:{userId}:*
4. 목록 캐시 삭제: calc:list:{userId}:*

[Housing 변경]
1. HousingUpdatedEvent 수신
2. housingId 추출
3. PostgreSQL 조회: 영향받는 userId 목록
4. Redis 패턴 삭제: calc:*:{housingId}:*
5. 사용자별 목록 캐시 삭제
```

---

## 성능 최적화

### 1. 인덱스 활용
- **조회 최적화**: userId, housingId, status 인덱스로 빠른 필터링
- **정렬 최적화**: calculated_at DESC 인덱스로 최신 데이터 우선 조회
- **복합 조건**: 복합 인덱스로 다중 조건 필터링 최적화

### 2. 캐시 전략
- **히트율 목표**: 60% 이상
- **응답 시간**:
  - 캐시 히트: ~0.1초
  - 캐시 미스: ~2초 (DB 조회 + 캐시 저장)
- **메모리 효율**: TTL로 자동 메모리 관리

### 3. 쿼리 최적화
```sql
-- 사용자별 최근 계산 결과 (인덱스: idx_user_id, idx_calculated_at)
SELECT * FROM calculation_results
WHERE user_id = :userId
ORDER BY calculated_at DESC
LIMIT 20;

-- 사용자 + 주택별 필터링 (인덱스: idx_user_housing)
SELECT * FROM calculation_results
WHERE user_id = :userId AND housing_id = :housingId
ORDER BY calculated_at DESC;

-- 사용자 + 상태별 필터링 (인덱스: idx_user_status)
SELECT * FROM calculation_results
WHERE user_id = :userId AND status = :status
ORDER BY calculated_at DESC;
```

### 4. 연결 풀 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 백업 및 복구

### 1. 백업 전략
- **Full Backup**: 매일 02:00 (KST)
- **Incremental Backup**: 매 6시간
- **보관 기간**: 30일
- **백업 도구**: pg_dump, pg_basebackup

### 2. 복구 전략
- **Point-in-Time Recovery (PITR)**: WAL 로그 기반
- **Recovery Time Objective (RTO)**: 1시간
- **Recovery Point Objective (RPO)**: 6시간

### 3. 백업 스크립트 예시
```bash
#!/bin/bash
BACKUP_DIR="/var/backups/postgres/calculator"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="calculator_db_${TIMESTAMP}.sql.gz"

pg_dump -U postgres calculator_db | gzip > ${BACKUP_DIR}/${BACKUP_FILE}

# 30일 이상 된 백업 삭제
find ${BACKUP_DIR} -name "calculator_db_*.sql.gz" -mtime +30 -delete
```

---

## 모니터링

### 1. 성능 지표
- **응답 시간**:
  - 조회: < 100ms (캐시 히트), < 1000ms (캐시 미스)
  - 생성: < 2000ms
- **처리량**:
  - 조회: > 1000 TPS
  - 생성: > 100 TPS
- **캐시 히트율**: > 60%

### 2. 데이터베이스 지표
- **연결 풀 사용률**: < 80%
- **쿼리 응답 시간**: < 500ms (95 percentile)
- **인덱스 효율**: > 90%
- **테이블 크기**: 정기 모니터링

### 3. 알림 조건
- 연결 풀 사용률 > 90% (1분 이상 지속)
- 쿼리 응답 시간 > 1초 (5분 평균)
- 캐시 히트율 < 40% (10분 평균)
- 디스크 사용률 > 80%

---

## 데이터 보안

### 1. 접근 제어
```sql
-- Calculator 서비스 전용 사용자 생성
CREATE USER calculator_user WITH PASSWORD 'secure_password';

-- 데이터베이스 권한 부여
GRANT CONNECT ON DATABASE calculator_db TO calculator_user;
GRANT USAGE ON SCHEMA public TO calculator_user;

-- 테이블 권한 부여 (최소 권한 원칙)
GRANT SELECT, INSERT, UPDATE, DELETE ON calculation_results TO calculator_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO calculator_user;
```

### 2. 네트워크 보안
- **접근 제한**: VPC 내부에서만 접근 허용
- **SSL/TLS**: 암호화된 연결 강제
- **포트**: 기본 5432 변경 권장

### 3. 데이터 암호화
- **저장 암호화**: PostgreSQL Transparent Data Encryption (TDE)
- **전송 암호화**: SSL/TLS 1.2+
- **민감 정보**: 금융 데이터는 평문 저장 (계산 결과이므로 암호화 불필요)

---

## 스키마 마이그레이션

### 1. 마이그레이션 도구
- **도구**: Flyway
- **버전 관리**: V{version}__{description}.sql 형식
- **롤백 전략**: 각 마이그레이션마다 롤백 스크립트 준비

### 2. 마이그레이션 예시
```sql
-- V1__create_calculation_results.sql
CREATE TABLE calculation_results (
    id VARCHAR(36) PRIMARY KEY,
    ...
);

CREATE INDEX idx_user_id ON calculation_results(user_id);
...
```

### 3. 롤백 스크립트
```sql
-- U1__create_calculation_results.sql
DROP TABLE IF EXISTS calculation_results CASCADE;
```

---

## 용량 추정

### 1. 레코드 크기 추정
- **평균 레코드 크기**: ~800 bytes (JSON 포함)
- **인덱스 오버헤드**: 레코드당 ~200 bytes

### 2. 성장 예측
- **초기 사용자**: 10,000명
- **월 계산 횟수**: 사용자당 평균 5회
- **월 레코드 증가**: 50,000건
- **연 데이터 증가**: ~480MB

### 3. 용량 계획
| 기간 | 예상 레코드 수 | 데이터 크기 | 인덱스 크기 | 총 용량 |
|------|---------------|-------------|-------------|---------|
| 6개월 | 300K | ~240MB | ~60MB | ~300MB |
| 1년 | 600K | ~480MB | ~120MB | ~600MB |
| 2년 | 1.2M | ~960MB | ~240MB | ~1.2GB |
| 5년 | 3M | ~2.4GB | ~600MB | ~3GB |

### 4. 파티셔닝 고려
- **파티셔닝 기준**: calculated_at (연도별)
- **적용 시점**: 레코드 수 > 1M 또는 테이블 크기 > 2GB
- **파티셔닝 전략**: Range Partitioning (PARTITION BY RANGE)

---

## 산출물

### 1. ERD (Entity Relationship Diagram)
- **파일**: `design/backend/database/calculator-erd.puml`
- **도구**: PlantUML
- **내용**: 테이블 구조, 컬럼, 제약조건, 인덱스

### 2. 스키마 스크립트
- **파일**: `design/backend/database/calculator-schema.psql`
- **내용**:
  - 데이터베이스 생성
  - 사용자 생성 및 권한 부여
  - 테이블 생성
  - 인덱스 생성
  - 제약 조건 정의
  - 초기 데이터 (선택)

### 3. 데이터베이스 설계서
- **파일**: `design/backend/database/calculator.md` (본 문서)
- **내용**: 설계 원칙, 테이블 정의, 성능 최적화, 백업 전략 등

---

## 구현 체크리스트

- [ ] PostgreSQL 15 설치 및 설정
- [ ] calculator_db 데이터베이스 생성
- [ ] calculator_user 생성 및 권한 부여
- [ ] calculation_results 테이블 생성
- [ ] 인덱스 생성 (필수 및 권장)
- [ ] 제약 조건 검증
- [ ] Redis 연결 설정 (캐시)
- [ ] Flyway 마이그레이션 스크립트 작성
- [ ] 백업 스크립트 작성 및 cron 등록
- [ ] 모니터링 설정 (응답 시간, 캐시 히트율)
- [ ] 성능 테스트 (조회, 생성, 캐시 히트율)
- [ ] 보안 검토 (접근 제어, 암호화)

---

## 참고 문서
- [PostgreSQL Documentation](https://www.postgresql.org/docs/15/)
- [Redis Cache-Aside Pattern](https://redis.io/docs/manual/patterns/cache-aside/)
- [Flyway Migration Guide](https://flywaydb.org/documentation/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
