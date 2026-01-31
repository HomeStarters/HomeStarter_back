# Asset 서비스 데이터베이스 설계서

## 1. 설계 개요

### 1.1 설계 정보
- **서비스명**: Asset 서비스
- **데이터베이스**: PostgreSQL 15.x
- **스키마명**: asset_service
- **캐시**: Redis 7.x (Database 2)
- **작성일**: 2025-12-29

### 1.2 참조 문서
- 클래스 설계서: `design/backend/class/asset-class-design.md`
- ERD: `design/backend/database/asset-erd.puml`
- 스키마 스크립트: `design/backend/database/asset-schema.psql`

### 1.3 설계 원칙
- **데이터 독립성**: Asset 서비스 전용 데이터베이스 사용
- **정규화**: 3NF 정규화 적용
- **1:N 관계**: 자산 항목별 복수 등록 지원
- **확장성**: 향후 자산 유형 추가 가능한 구조

---

## 2. 데이터 설계 요약

### 2.1 테이블 목록

| 테이블명 | 설명 | 주요 용도 |
|---------|------|------------|
| assets | 자산정보 메인 | 본인/배우자 자산 총액 관리 |
| asset_items | 자산 항목 | 예금, 주식 등 자산 상세 |
| loan_items | 대출 항목 | 대출 상세 내역 |
| income_items | 월소득 항목 | 월소득 상세 내역 |
| expense_items | 월지출 항목 | 월지출 상세 내역 |

### 2.2 주요 특징
- **본인/배우자 분리**: owner_type (SELF/SPOUSE)로 구분
- **복수 항목 관리**: 각 항목별 1:N 관계로 여러 개 등록 가능
- **자동 총액 계산**: 애플리케이션 레벨에서 총액 계산 후 저장
- **캐시 전략**: Redis에 자산 정보 및 총액 캐싱

### 2.3 데이터 흐름
1. **자산 생성**: assets 삽입 → 항목들 일괄 삽입
2. **총액 계산**: 애플리케이션에서 계산 → assets 테이블 업데이트
3. **항목 수정**: 항목 업데이트 → 총액 재계산 → assets 업데이트 → Redis 캐시 무효화
4. **조회**: Redis 캐시 확인 → 없으면 DB 조회 → 캐시 저장

---

## 3. 테이블 상세 정의

### 3.1 assets (자산정보 메인)

#### 테이블 설명
사용자(본인/배우자)별 자산 총액 정보를 저장하는 메인 테이블

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | VARCHAR(50) | PK | 자산정보 ID (UUID) |
| user_id | VARCHAR(50) | NOT NULL | 사용자 ID |
| owner_type | VARCHAR(10) | NOT NULL | 소유자 유형 (SELF, SPOUSE) |
| total_assets | BIGINT | NOT NULL, DEFAULT 0 | 총 자산액 (원) |
| total_loans | BIGINT | NOT NULL, DEFAULT 0 | 총 대출액 (원) |
| total_monthly_income | BIGINT | NOT NULL, DEFAULT 0 | 총 월소득 (원) |
| total_monthly_expense | BIGINT | NOT NULL, DEFAULT 0 | 총 월지출 (원) |
| net_assets | BIGINT | NOT NULL, DEFAULT 0 | 순자산 (총자산 - 총대출) |
| monthly_available_funds | BIGINT | NOT NULL, DEFAULT 0 | 월 가용자금 (월소득 - 월지출) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_assets | id | PRIMARY KEY | 기본키 인덱스 |
| uk_assets_user_owner | user_id, owner_type | UNIQUE | 사용자당 본인/배우자 각 1개씩 |
| idx_assets_user_id | user_id | BTREE | 사용자별 조회 성능 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| chk_assets_owner_type | CHECK | owner_type IN ('SELF', 'SPOUSE') |
| chk_assets_totals | CHECK | total_assets >= 0 AND total_loans >= 0 AND total_monthly_income >= 0 AND total_monthly_expense >= 0 |

#### 비즈니스 규칙
- **사용자당 2개**: 본인(SELF) 1개, 배우자(SPOUSE) 1개
- **총액 계산**: 애플리케이션에서 항목 합산 후 저장
- **순자산**: total_assets - total_loans
- **월 가용자금**: total_monthly_income - total_monthly_expense

---

### 3.2 asset_items (자산 항목)

#### 테이블 설명
자산의 상세 항목 (예금, 적금, 주식 등)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | VARCHAR(50) | PK | 자산 항목 ID (UUID) |
| asset_id | VARCHAR(50) | NOT NULL, FK | 자산정보 ID |
| name | VARCHAR(100) | NOT NULL | 자산명 (예: 국민은행 예금) |
| amount | BIGINT | NOT NULL | 금액 (원) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_asset_items | id | PRIMARY KEY | 기본키 인덱스 |
| idx_asset_items_asset_id | asset_id | BTREE | 자산정보별 조회 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| fk_asset_items_asset_id | FOREIGN KEY | asset_id → assets.id (CASCADE) |
| chk_asset_items_amount | CHECK | amount >= 0 |

---

### 3.3 loan_items (대출 항목)

#### 테이블 설명
대출의 상세 항목 (주택담보대출, 신용대출 등)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | VARCHAR(50) | PK | 대출 항목 ID (UUID) |
| asset_id | VARCHAR(50) | NOT NULL, FK | 자산정보 ID |
| name | VARCHAR(100) | NOT NULL | 대출명 (예: 주택담보대출) |
| amount | BIGINT | NOT NULL | 대출 잔액 (원) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_loan_items | id | PRIMARY KEY | 기본키 인덱스 |
| idx_loan_items_asset_id | asset_id | BTREE | 자산정보별 조회 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| fk_loan_items_asset_id | FOREIGN KEY | asset_id → assets.id (CASCADE) |
| chk_loan_items_amount | CHECK | amount >= 0 |

---

### 3.4 income_items (월소득 항목)

#### 테이블 설명
월소득의 상세 항목 (급여, 부업 등)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | VARCHAR(50) | PK | 월소득 항목 ID (UUID) |
| asset_id | VARCHAR(50) | NOT NULL, FK | 자산정보 ID |
| name | VARCHAR(100) | NOT NULL | 소득명 (예: 회사 급여) |
| amount | BIGINT | NOT NULL | 월 금액 (원) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_income_items | id | PRIMARY KEY | 기본키 인덱스 |
| idx_income_items_asset_id | asset_id | BTREE | 자산정보별 조회 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| fk_income_items_asset_id | FOREIGN KEY | asset_id → assets.id (CASCADE) |
| chk_income_items_amount | CHECK | amount >= 0 |

---

### 3.5 expense_items (월지출 항목)

#### 테이블 설명
월지출의 상세 항목 (생활비, 교육비 등)

#### 컬럼 정의

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|---------|------|
| id | VARCHAR(50) | PK | 월지출 항목 ID (UUID) |
| asset_id | VARCHAR(50) | NOT NULL, FK | 자산정보 ID |
| name | VARCHAR(100) | NOT NULL | 지출명 (예: 생활비) |
| amount | BIGINT | NOT NULL | 월 금액 (원) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 생성 시간 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 수정 시간 |

#### 인덱스

| 인덱스명 | 컬럼 | 타입 | 설명 |
|---------|------|------|------|
| pk_expense_items | id | PRIMARY KEY | 기본키 인덱스 |
| idx_expense_items_asset_id | asset_id | BTREE | 자산정보별 조회 |

#### 제약조건

| 제약조건명 | 타입 | 내용 |
|-----------|------|------|
| fk_expense_items_asset_id | FOREIGN KEY | asset_id → assets.id (CASCADE) |
| chk_expense_items_amount | CHECK | amount >= 0 |

---

## 4. 관계 정의

### 4.1 ERD 관계

```
assets (1) ─────< (N) asset_items
       (1) ─────< (N) loan_items
       (1) ─────< (N) income_items
       (1) ─────< (N) expense_items
```

### 4.2 Cascade 정책

| 테이블 | FK 컬럼 | ON DELETE | ON UPDATE |
|--------|---------|-----------|-----------|
| asset_items | asset_id | CASCADE | CASCADE |
| loan_items | asset_id | CASCADE | CASCADE |
| income_items | asset_id | CASCADE | CASCADE |
| expense_items | asset_id | CASCADE | CASCADE |

**설명**:
- 자산정보 삭제 시 모든 항목도 함께 삭제
- 자산정보 ID 변경 시 모든 항목의 FK도 자동 업데이트

---

## 5. 인덱스 전략

### 5.1 성능 최적화 인덱스

| 테이블 | 인덱스명 | 컬럼 | 용도 |
|--------|---------|------|------|
| assets | uk_assets_user_owner | user_id, owner_type | 중복 방지 및 조회 성능 |
| assets | idx_assets_user_id | user_id | 사용자별 자산 조회 |
| asset_items | idx_asset_items_asset_id | asset_id | 자산별 항목 조회 |
| loan_items | idx_loan_items_asset_id | asset_id | 자산별 대출 조회 |
| income_items | idx_income_items_asset_id | asset_id | 자산별 소득 조회 |
| expense_items | idx_expense_items_asset_id | asset_id | 자산별 지출 조회 |

---

## 6. 캐시 전략

### 6.1 Redis 캐시 설계 (Database 2)

#### 전체 자산 정보 캐시

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 사용자별 전체 자산 정보 |
| **키 패턴** | `asset:{userId}` |
| **값 구조** | JSON (자산 + 모든 항목 포함) |
| **TTL** | 3600초 (1시간) |
| **용도** | 전체 자산정보 조회 성능 최적화 |

**예시**:
```
키: asset:john123
값: {
  "self": {
    "id": "asset-123",
    "totalAssets": 50000000,
    "totalLoans": 10000000,
    "totalMonthlyIncome": 5000000,
    "totalMonthlyExpense": 3000000,
    "netAssets": 40000000,
    "monthlyAvailableFunds": 2000000,
    "assetItems": [...],
    "loanItems": [...],
    "incomeItems": [...],
    "expenseItems": [...]
  },
  "spouse": {...}
}
TTL: 3600초
```

#### 자산 요약 정보 캐시

| 항목 | 내용 |
|------|------|
| **캐시 대상** | 사용자별 자산 총액 요약 |
| **키 패턴** | `asset:summary:{userId}` |
| **값 구조** | JSON (총액 정보만) |
| **TTL** | 3600초 (1시간) |
| **용도** | 빠른 총액 조회 |

**예시**:
```
키: asset:summary:john123
값: {
  "combinedTotalAssets": 80000000,
  "combinedTotalLoans": 20000000,
  "combinedNetAssets": 60000000,
  "combinedMonthlyIncome": 8000000,
  "combinedMonthlyExpense": 5000000,
  "combinedAvailableFunds": 3000000
}
TTL: 3600초
```

### 6.2 캐시 무효화 전략
- **자산 추가 시**: DEL asset:{userId}, DEL asset:summary:{userId}
- **항목 수정 시**: DEL asset:{userId}, DEL asset:summary:{userId}
- **항목 삭제 시**: DEL asset:{userId}, DEL asset:summary:{userId}
- **자동 재캐싱**: 다음 조회 시 DB에서 조회 후 캐시 저장

---

## 7. 성능 최적화

### 7.1 예상 성능 지표

| 작업 | 예상 응답 시간 | 근거 |
|------|--------------|------|
| 자산 생성 (DB) | < 50ms | 5개 테이블 INSERT (트랜잭션) |
| 자산 조회 (캐시) | < 5ms | Redis 메모리 조회 |
| 자산 조회 (DB) | < 30ms | JOIN 4개 테이블 + 인덱스 |
| 항목 수정 (DB) | < 30ms | UPDATE + 총액 재계산 |
| 항목 삭제 (DB) | < 20ms | DELETE + 총액 재계산 |

### 7.2 용량 계획

#### 초기 용량 (1년 기준)

| 항목 | 예상 값 |
|------|--------|
| 예상 사용자 수 | 100,000명 |
| assets 테이블 크기 | ~20MB |
| 항목 테이블 총 크기 | ~80MB (평균 10개 항목/사용자) |
| 총 데이터베이스 크기 | ~120MB (인덱스 포함) |
| Redis 메모리 사용량 | ~100MB |

---

## 8. 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0 | 2025-12-29 | 길동 (아키텍트) | 초기 설계 완료 |
