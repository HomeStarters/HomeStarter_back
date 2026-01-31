# Loan Service - 데이터베이스 설계서

## 데이터설계 요약

### 데이터베이스 정보
- **데이터베이스명**: loan_db
- **DBMS**: PostgreSQL 15+
- **캐릭터셋**: UTF8
- **타임존**: Asia/Seoul

### 테이블 목록
| 테이블명 | 한글명 | 용도 | 예상 레코드 수 |
|---------|--------|------|--------------|
| loan_products | 대출상품 | 대출상품 정보 관리 | 100-500건 |

### 캐시 전략
- **Redis Database**: DB 3 (Loan 서비스 전용)
- **캐시 대상**: 대출상품 목록, 대출상품 상세정보
- **캐시 패턴**: Cache-Aside
- **TTL 정책**:
  - 목록 캐시: 1시간 (3600초)
  - 상세 캐시: 2시간 (7200초)

### 백업 전략
- **일일 백업**: 매일 02:00 AM
- **보관 기간**: 30일
- **백업 방식**: Full Backup

---

## 1. 테이블 설계

### 1.1 loan_products (대출상품)

#### 테이블 개요
- **목적**: 대출상품 정보 저장 및 관리
- **접근 패턴**: 조회 중심 (읽기:쓰기 = 95:5)
- **데이터 특성**: 정적 데이터, 변경 빈도 낮음

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 | 예시 |
|--------|------|---------|------|------|
| id | BIGSERIAL | PK | 대출상품 ID (자동증가) | 1 |
| name | VARCHAR(100) | NOT NULL | 대출이름 | 신혼부부 특별대출 |
| loan_limit | BIGINT | NOT NULL, CHECK >= 0 | 대출한도 (원 단위) | 300000000 |
| ltv_limit | DECIMAL(5,2) | NOT NULL, CHECK 0-100 | LTV 한도 (%) | 70.00 |
| dti_limit | DECIMAL(5,2) | NOT NULL, CHECK 0-100 | DTI 한도 (%) | 60.00 |
| dsr_limit | DECIMAL(5,2) | NOT NULL, CHECK 0-100 | DSR 한도 (%) | 40.00 |
| interest_rate | DECIMAL(5,2) | NOT NULL, CHECK 0-100 | 금리 (연 %) | 2.50 |
| target_housing | VARCHAR(200) | NOT NULL | 대상주택 | 6억 이하 아파트 |
| income_requirement | VARCHAR(200) | NULL | 소득요건 | 부부합산 연소득 7천만원 이하 |
| applicant_requirement | VARCHAR(200) | NULL | 신청자요건 | 무주택자, 생애최초 |
| remarks | TEXT | NULL | 비고 (특이사항) | 신혼부부 우대금리 적용 |
| active | BOOLEAN | NOT NULL, DEFAULT true | 활성화 여부 | true |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 등록일시 | 2025-01-15 10:30:00 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 수정일시 | 2025-01-20 14:00:00 |

#### 제약조건

**Primary Key**
```sql
CONSTRAINT pk_loan_products PRIMARY KEY (id)
```

**Check Constraints**
```sql
CONSTRAINT chk_loan_limit CHECK (loan_limit >= 0),
CONSTRAINT chk_ltv_limit CHECK (ltv_limit >= 0 AND ltv_limit <= 100),
CONSTRAINT chk_dti_limit CHECK (dti_limit >= 0 AND dti_limit <= 100),
CONSTRAINT chk_dsr_limit CHECK (dsr_limit >= 0 AND dsr_limit <= 100),
CONSTRAINT chk_interest_rate CHECK (interest_rate >= 0 AND interest_rate <= 100)
```

**Unique Constraints**
```sql
CONSTRAINT uk_loan_product_name UNIQUE (name, active)
-- 활성화된 대출상품 중 동일 이름 방지
```

#### 인덱스 설계

| 인덱스명 | 타입 | 컬럼 | 목적 | 예상 효과 |
|---------|------|------|------|----------|
| pk_loan_products | B-Tree (자동) | id | Primary Key | 단일 조회 최적화 |
| idx_active | B-Tree | active | 활성화 상품 조회 | 목록 조회 필터링 |
| idx_target_housing | B-Tree | target_housing | 주택유형 필터링 | WHERE 조건 성능 향상 |
| idx_interest_rate | B-Tree | interest_rate | 금리순 정렬 | ORDER BY 성능 향상 |
| idx_loan_limit | B-Tree | loan_limit | 한도순 정렬 | ORDER BY 성능 향상 |
| idx_created_at | B-Tree | created_at | 최근 등록순 정렬 | ORDER BY 성능 향상 |
| idx_name_search | GIN | name (gin_trgm_ops) | 대출이름 검색 | LIKE 검색 성능 향상 |

#### 샘플 데이터

```sql
-- 신혼부부 특별대출
id: 1
name: '신혼부부 특별대출'
loan_limit: 300000000
ltv_limit: 70.00
dti_limit: 60.00
dsr_limit: 40.00
interest_rate: 2.50
target_housing: '6억 이하 아파트'
income_requirement: '부부합산 연소득 7천만원 이하'
applicant_requirement: '무주택자, 생애최초'
remarks: '신혼부부 우대금리 적용'
active: true
created_at: '2025-01-15 10:30:00'
updated_at: '2025-01-15 10:30:00'

-- 디딤돌대출
id: 2
name: '디딤돌대출'
loan_limit: 500000000
ltv_limit: 80.00
dti_limit: 60.00
dsr_limit: 50.00
interest_rate: 3.20
target_housing: '9억 이하 전 주택유형'
income_requirement: '연소득 6천만원 이하'
applicant_requirement: '무주택 또는 1주택자'
remarks: '서민·실수요자 주거안정 지원'
active: true
created_at: '2025-01-15 10:35:00'
updated_at: '2025-01-15 10:35:00'

-- 보금자리론
id: 3
name: '보금자리론'
loan_limit: 600000000
ltv_limit: 70.00
dti_limit: 60.00
dsr_limit: 40.00
interest_rate: 3.50
target_housing: '9억 이하 주택'
income_requirement: null
applicant_requirement: '실거주 목적'
remarks: '주택구입자금 대출'
active: true
created_at: '2025-01-15 10:40:00'
updated_at: '2025-01-15 10:40:00'
```

---

## 2. 캐시 설계

### 2.1 Redis Database 할당
- **Database 번호**: 3
- **용도**: Loan 서비스 전용 캐시 영역

### 2.2 캐시 키 패턴

#### 대출상품 목록 캐시
```
패턴: loans:list:{housingType}:{sortBy}:{sortOrder}:{keyword}:{page}:{size}
TTL: 3600초 (1시간)
데이터 타입: String (JSON)

예시:
- loans:list:아파트:interestRate:asc::0:20
- loans:list:::신혼:0:10
- loans:list:전체:loanLimit:desc::1:20
```

#### 대출상품 상세 캐시
```
패턴: loan:product:{id}
TTL: 7200초 (2시간)
데이터 타입: String (JSON)

예시:
- loan:product:1
- loan:product:2
- loan:product:3
```

### 2.3 캐시 무효화 전략

**등록 시**:
- `loans:list:*` 패턴 모든 캐시 삭제
- 목록 조회 결과가 변경되므로 전체 무효화

**수정 시**:
- `loan:product:{id}` 해당 상세 캐시 삭제
- `loans:list:*` 패턴 모든 캐시 삭제
- 목록/상세 모두 영향 받음

**삭제 시**:
- `loan:product:{id}` 해당 상세 캐시 삭제
- `loans:list:*` 패턴 모든 캐시 삭제
- active=false로 변경되므로 목록에서 제외

### 2.4 캐시 적용 로직

**조회 흐름 (Cache-Aside)**:
```
1. Redis 캐시 조회
2. 캐시 HIT → 캐시 데이터 반환
3. 캐시 MISS → DB 조회
4. DB 결과를 Redis에 저장 (TTL 설정)
5. 결과 반환
```

**변경 흐름 (Write-Through + Invalidation)**:
```
1. DB에 데이터 변경
2. 트랜잭션 커밋
3. 관련 캐시 무효화 (패턴 매칭 삭제)
4. 다음 조회 시 캐시 재생성
```

---

## 3. 데이터 접근 패턴

### 3.1 조회 패턴

#### 목록 조회 (UFR-LOAN-010)
```sql
-- 활성화된 전체 목록 조회 (페이징)
SELECT * FROM loan_products
WHERE active = true
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;

-- 주택유형 필터링 + 금리순 정렬
SELECT * FROM loan_products
WHERE active = true
  AND target_housing LIKE '%아파트%'
ORDER BY interest_rate ASC
LIMIT 20 OFFSET 0;

-- 키워드 검색 (대출이름 또는 대상주택)
SELECT * FROM loan_products
WHERE active = true
  AND (name LIKE '%신혼%' OR target_housing LIKE '%신혼%')
ORDER BY created_at DESC
LIMIT 20 OFFSET 0;
```

**성능 고려사항**:
- `idx_active` 인덱스로 활성화 필터링 최적화
- `idx_interest_rate`, `idx_loan_limit` 인덱스로 정렬 최적화
- `idx_name_search` GIN 인덱스로 LIKE 검색 최적화
- 캐시 적중률 목표: 80% 이상

#### 상세 조회 (UFR-LOAN-020)
```sql
-- ID로 단일 조회
SELECT * FROM loan_products
WHERE id = 1 AND active = true;
```

**성능 고려사항**:
- Primary Key 조회로 즉시 검색
- 캐시 우선 조회로 DB 부하 최소화
- 캐시 적중률 목표: 90% 이상

### 3.2 변경 패턴

#### 등록 (AFR-LOAN-030)
```sql
-- 대출상품 등록
INSERT INTO loan_products (
  name, loan_limit, ltv_limit, dti_limit, dsr_limit,
  interest_rate, target_housing, income_requirement,
  applicant_requirement, remarks, active
) VALUES (
  '신혼부부 특별대출', 300000000, 70.00, 60.00, 40.00,
  2.50, '6억 이하 아파트', '부부합산 연소득 7천만원 이하',
  '무주택자, 생애최초', '신혼부부 우대금리 적용', true
);
```

#### 수정 (AFR-LOAN-030)
```sql
-- 대출상품 수정
UPDATE loan_products
SET name = '신혼부부 특별대출 2025',
    loan_limit = 350000000,
    interest_rate = 2.30,
    updated_at = NOW()
WHERE id = 1;
```

#### 삭제 (AFR-LOAN-030)
```sql
-- 소프트 삭제 (활성화 해제)
UPDATE loan_products
SET active = false,
    updated_at = NOW()
WHERE id = 1;
```

---

## 4. 데이터베이스 최적화

### 4.1 쿼리 최적화

**Full Text Search를 위한 pg_trgm 확장**:
```sql
-- 삼중자(trigram) 기반 텍스트 검색 확장
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN 인덱스 생성
CREATE INDEX idx_name_search ON loan_products
USING gin (name gin_trgm_ops);
```

**EXPLAIN 분석 권장 쿼리**:
- 복잡한 검색 조건이 포함된 목록 조회
- 다중 컬럼 정렬이 포함된 쿼리

### 4.2 파티셔닝

현재는 필요 없음
- 예상 레코드 수: 100-500건 (소규모)
- 데이터 증가율: 연 50-100건
- 향후 1만 건 초과 시 파티셔닝 고려
  - 파티션 기준: active (true/false)
  - 활성화된 데이터만 주로 조회

### 4.3 통계 정보 갱신

```sql
-- 통계 정보 수동 갱신 (대량 데이터 변경 후)
ANALYZE loan_products;

-- 자동 VACUUM 설정 확인
SELECT * FROM pg_settings
WHERE name LIKE 'autovacuum%';
```

---

## 5. 보안 및 권한 관리

### 5.1 사용자 권한

**일반 사용자 (loan_user)**:
```sql
-- 조회 전용 권한
GRANT SELECT ON loan_products TO loan_user;
```

**관리자 (loan_admin)**:
```sql
-- 모든 권한
GRANT SELECT, INSERT, UPDATE, DELETE ON loan_products TO loan_admin;
GRANT USAGE, SELECT ON SEQUENCE loan_products_id_seq TO loan_admin;
```

### 5.2 행 수준 보안 (RLS)

현재는 미적용
- 모든 활성화된 대출상품은 모든 사용자에게 공개
- 향후 사용자별 맞춤 대출상품 제공 시 고려

### 5.3 감사 로그

**변경 이력 추적을 위한 트리거 (선택사항)**:
```sql
-- 변경 이력 테이블
CREATE TABLE loan_products_audit (
  audit_id BIGSERIAL PRIMARY KEY,
  loan_product_id BIGINT NOT NULL,
  operation VARCHAR(10) NOT NULL,
  changed_by VARCHAR(100),
  changed_at TIMESTAMP DEFAULT NOW(),
  old_data JSONB,
  new_data JSONB
);

-- 변경 이력 트리거
CREATE OR REPLACE FUNCTION loan_products_audit_trigger()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    INSERT INTO loan_products_audit (loan_product_id, operation, new_data)
    VALUES (NEW.id, 'INSERT', row_to_json(NEW));
  ELSIF (TG_OP = 'UPDATE') THEN
    INSERT INTO loan_products_audit (loan_product_id, operation, old_data, new_data)
    VALUES (OLD.id, 'UPDATE', row_to_json(OLD), row_to_json(NEW));
  ELSIF (TG_OP = 'DELETE') THEN
    INSERT INTO loan_products_audit (loan_product_id, operation, old_data)
    VALUES (OLD.id, 'DELETE', row_to_json(OLD));
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER loan_products_audit
AFTER INSERT OR UPDATE OR DELETE ON loan_products
FOR EACH ROW EXECUTE FUNCTION loan_products_audit_trigger();
```

---

## 6. 백업 및 복구

### 6.1 백업 전략

**일일 백업 스크립트**:
```bash
#!/bin/bash
# daily_backup.sh

DATE=$(date +%Y%m%d)
BACKUP_DIR="/var/backups/postgresql/loan_db"
RETENTION_DAYS=30

# Full Backup
pg_dump -U postgres -d loan_db -F c -f "$BACKUP_DIR/loan_db_$DATE.dump"

# 압축
gzip "$BACKUP_DIR/loan_db_$DATE.dump"

# 오래된 백업 삭제
find "$BACKUP_DIR" -name "*.dump.gz" -mtime +$RETENTION_DAYS -delete
```

**cron 설정**:
```cron
0 2 * * * /usr/local/bin/daily_backup.sh
```

### 6.2 복구 절차

**전체 복구**:
```bash
# 백업 파일 압축 해제
gunzip loan_db_20250115.dump.gz

# 데이터베이스 복구
pg_restore -U postgres -d loan_db -c loan_db_20250115.dump
```

**특정 테이블 복구**:
```bash
pg_restore -U postgres -d loan_db -t loan_products loan_db_20250115.dump
```

---

## 7. 모니터링

### 7.1 성능 메트릭

**모니터링 대상**:
- 쿼리 응답 시간 (목표: < 100ms)
- 캐시 적중률 (목표: > 80%)
- 동시 접속 수
- 테이블 크기 증가율
- 인덱스 사용률

**슬로우 쿼리 로그 설정**:
```sql
-- 1초 이상 쿼리 로그 기록
ALTER DATABASE loan_db SET log_min_duration_statement = 1000;
```

### 7.2 알림 설정

**알림 조건**:
- 쿼리 응답 시간 > 500ms
- 캐시 적중률 < 70%
- 테이블 크기 > 10GB
- 연결 풀 고갈 (> 90%)

---

## 8. 확장성 고려사항

### 8.1 수평 확장

**Read Replica 구성** (읽기 부하 분산):
```
Master DB (Write) → Replica 1, 2 (Read)
- 목록/상세 조회 → Replica
- 등록/수정/삭제 → Master
```

### 8.2 수직 확장

**성능 향상 시점**:
- 동시 사용자 > 1,000명
- 쿼리 응답 시간 > 200ms
- CPU 사용률 > 70%

**권장 사양**:
- CPU: 4 Core → 8 Core
- Memory: 8GB → 16GB
- Storage: SSD 100GB → 200GB

---

## 9. 마이그레이션 전략

### 9.1 초기 데이터 로드

**대출상품 초기 데이터 삽입**:
```sql
-- 주요 대출상품 10-20개 사전 등록
-- 신혼부부, 디딤돌, 보금자리, 적격대출 등
```

### 9.2 스키마 변경 절차

**무중단 스키마 변경**:
1. 새 컬럼 추가 (NULL 허용)
2. 애플리케이션 배포 (새 컬럼 사용)
3. 기존 데이터 마이그레이션
4. NOT NULL 제약조건 추가
5. 구 컬럼 제거

---

## 10. 참고 문서

- **클래스 설계서**: `/design/backend/class/loan-class-design.md`
- **API 명세서**: `/design/backend/api/loan-service-api.yaml`
- **ERD 다이어그램**: `/design/backend/database/loan-erd.puml`
- **스키마 스크립트**: `/design/backend/database/loan-schema.psql`
- **캐시 설계서**: `/design/backend/database/cache-db-design.md`
