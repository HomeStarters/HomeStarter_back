# Roadmap Service - 데이터베이스 설계서

**작성일**: 2025-12-29
**작성자**: 길동 (아키텍트)

## 1. 개요

### 1.1 목적
Roadmap 서비스의 데이터베이스 설계 및 스키마 정의

### 1.2 범위
- 생애주기 이벤트 관리
- AI 로드맵 생성 및 버전 관리
- 비동기 작업 추적

### 1.3 데이터베이스 정보
- **DBMS**: PostgreSQL 16.x
- **데이터베이스명**: roadmap_db
- **문자셋**: UTF-8
- **타임존**: Asia/Seoul

## 2. 데이터베이스 구조

### 2.1 테이블 목록

| 테이블명 | 설명 | 관계 |
|---------|------|------|
| lifecycle_events | 생애주기 이벤트 | 사용자 1:N |
| roadmaps | 장기주거 로드맵 | 사용자 1:N (버전별) |
| roadmap_stages | 로드맵 단계별 계획 | 로드맵 1:N |
| execution_guides | 실행 가이드 | 로드맵 1:1 |
| roadmap_tasks | 비동기 작업 추적 | 사용자 1:N |

### 2.2 ERD
- ERD 다이어그램: `roadmap-erd.puml`
- 주요 관계:
  - User (외부) ← 1:N → LifecycleEvent
  - User (외부) ← 1:N → Roadmap (최대 3개 버전)
  - Roadmap ← 1:N → RoadmapStage
  - Roadmap ← 1:1 → ExecutionGuide
  - User (외부) ← 1:N → RoadmapTask

## 3. 테이블 상세 설계

### 3.1 lifecycle_events (생애주기 이벤트)

**설명**: 사용자의 생애주기 이벤트 정보 관리

| 컬럼명 | 데이터 타입 | NULL | 기본값 | 설명 |
|--------|------------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 기본키 |
| user_id | VARCHAR(36) | NOT NULL | - | 사용자 ID (외부 참조) |
| name | VARCHAR(100) | NOT NULL | - | 이벤트 이름 |
| event_type | VARCHAR(20) | NOT NULL | - | 이벤트 유형 (MARRIAGE, BIRTH, CHILD_EDUCATION, RETIREMENT, OTHER) |
| event_date | VARCHAR(7) | NOT NULL | - | 이벤트 예정일 (yyyy-MM 형식) |
| housing_criteria | VARCHAR(200) | NULL | - | 주택선택 고려기준 |
| created_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 수정일시 |

**제약조건**:
- PRIMARY KEY: id
- INDEX: idx_lifecycle_events_user_id (user_id)
- INDEX: idx_lifecycle_events_event_type (event_type)
- CHECK: event_type IN ('MARRIAGE', 'BIRTH', 'CHILD_EDUCATION', 'RETIREMENT', 'OTHER')
- CHECK: event_date ~ '^\d{4}-\d{2}$' (yyyy-MM 형식 검증)

**비즈니스 규칙**:
- 사용자당 이벤트 개수 제한 없음
- 동일 이벤트 중복 등록 가능 (예: 자녀 2명)
- 과거 날짜 이벤트 등록 가능 (기록 목적)

---

### 3.2 roadmaps (장기주거 로드맵)

**설명**: AI 생성 장기주거 로드맵 정보

| 컬럼명 | 데이터 타입 | NULL | 기본값 | 설명 |
|--------|------------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 기본키 |
| user_id | VARCHAR(36) | NOT NULL | - | 사용자 ID (외부 참조) |
| version | INT | NOT NULL | - | 로드맵 버전 (1부터 시작) |
| status | VARCHAR(20) | NOT NULL | PROCESSING | 상태 (PROCESSING, COMPLETED, FAILED) |
| task_id | VARCHAR(36) | NOT NULL | - | 작업 ID (roadmap_tasks 참조) |
| final_housing_id | VARCHAR(36) | NOT NULL | - | 최종목표 주택 ID (외부 참조) |
| created_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 수정일시 |

**제약조건**:
- PRIMARY KEY: id
- UNIQUE: uk_roadmaps_user_version (user_id, version)
- INDEX: idx_roadmaps_user_id (user_id)
- INDEX: idx_roadmaps_task_id (task_id)
- CHECK: version >= 1
- CHECK: status IN ('PROCESSING', 'COMPLETED', 'FAILED')

**비즈니스 규칙**:
- 사용자당 최대 3개 버전 유지 (4번째 생성 시 가장 오래된 버전 삭제)
- 버전은 1부터 시작하여 순차 증가
- 최신 버전 = MAX(version)

---

### 3.3 roadmap_stages (로드맵 단계별 계획)

**설명**: 로드맵의 단계별 주택 계획 및 재무 목표

| 컬럼명 | 데이터 타입 | NULL | 기본값 | 설명 |
|--------|------------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 기본키 |
| roadmap_id | VARCHAR(36) | NOT NULL | - | 로드맵 ID (roadmaps 참조) |
| stage_number | INT | NOT NULL | - | 단계 번호 (1부터 시작) |
| stage_name | VARCHAR(100) | NOT NULL | - | 단계명 (예: "신혼기 전세", "육아기 매매") |
| move_in_date | VARCHAR(7) | NOT NULL | - | 입주 시기 (yyyy-MM 형식) |
| duration | INT | NOT NULL | - | 거주 기간 (개월 수) |
| estimated_price | BIGINT | NOT NULL | - | 예상 가격 (원) |
| location | VARCHAR(200) | NOT NULL | - | 위치 |
| type | VARCHAR(50) | NOT NULL | - | 주택 타입 (전세, 매매, 월세 등) |
| features | TEXT | NULL | - | 특징 (JSON 배열) |
| target_savings | BIGINT | NOT NULL | - | 목표 저축액 (원) |
| monthly_savings | BIGINT | NOT NULL | - | 월 저축액 (원) |
| loan_amount | BIGINT | NULL | 0 | 대출 금액 (원) |
| loan_product | VARCHAR(100) | NULL | - | 대출 상품명 |
| strategy | TEXT | NOT NULL | - | 실행 전략 |
| tips | TEXT | NULL | - | 팁 (JSON 배열) |

**제약조건**:
- PRIMARY KEY: id
- FOREIGN KEY: fk_roadmap_stages_roadmap (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE
- UNIQUE: uk_roadmap_stages_roadmap_stage (roadmap_id, stage_number)
- INDEX: idx_roadmap_stages_roadmap_id (roadmap_id)
- CHECK: stage_number >= 1
- CHECK: duration > 0
- CHECK: estimated_price >= 0
- CHECK: target_savings >= 0
- CHECK: monthly_savings >= 0
- CHECK: loan_amount >= 0
- CHECK: move_in_date ~ '^\d{4}-\d{2}$'

**비즈니스 규칙**:
- 한 로드맵당 최소 1개, 최대 5개 단계
- stage_number는 1부터 순차 증가
- features, tips는 JSON 배열 형식으로 저장

**JSON 데이터 예시**:
```json
// features
["남향", "초등학교 인근", "주차 가능"]

// tips
["월급날에 자동이체 설정", "비상금 300만원 별도 유지"]
```

---

### 3.4 execution_guides (실행 가이드)

**설명**: 로드맵 실행을 위한 가이드 정보

| 컬럼명 | 데이터 타입 | NULL | 기본값 | 설명 |
|--------|------------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 기본키 |
| roadmap_id | VARCHAR(36) | NOT NULL | - | 로드맵 ID (roadmaps 참조) |
| monthly_savings_plan | TEXT | NOT NULL | - | 월별 저축 플랜 (JSON 배열) |
| warnings | TEXT | NULL | - | 주의사항 (JSON 배열) |
| tips | TEXT | NULL | - | 팁 (JSON 배열) |

**제약조건**:
- PRIMARY KEY: id
- FOREIGN KEY: fk_execution_guides_roadmap (roadmap_id) REFERENCES roadmaps(id) ON DELETE CASCADE
- UNIQUE: uk_execution_guides_roadmap (roadmap_id)

**비즈니스 규칙**:
- 로드맵당 1개의 실행 가이드
- JSON 형식으로 구조화된 데이터 저장

**JSON 데이터 예시**:
```json
// monthly_savings_plan
[
  {"period": "2025-01 ~ 2027-12", "amount": 2000000, "purpose": "전세 보증금 마련"},
  {"period": "2028-01 ~ 2032-12", "amount": 3000000, "purpose": "매매 자금 마련"}
]

// warnings
["금리 상승 시 월 저축액 재검토", "생애주기 이벤트 발생 시 재설계 권장"]

// tips
["적금 만기 시 즉시 재예치", "보너스는 100% 저축"]
```

---

### 3.5 roadmap_tasks (비동기 작업 추적)

**설명**: AI 로드맵 생성 비동기 작업 상태 추적

| 컬럼명 | 데이터 타입 | NULL | 기본값 | 설명 |
|--------|------------|------|--------|------|
| id | VARCHAR(36) | NOT NULL | UUID | 기본키 (작업 ID) |
| user_id | VARCHAR(36) | NOT NULL | - | 사용자 ID (외부 참조) |
| status | VARCHAR(20) | NOT NULL | PENDING | 작업 상태 (PENDING, PROCESSING, COMPLETED, FAILED) |
| progress | INT | NOT NULL | 0 | 진행률 (0~100) |
| message | VARCHAR(200) | NULL | - | 진행 상황 메시지 |
| roadmap_id | VARCHAR(36) | NULL | - | 완료된 로드맵 ID (roadmaps 참조) |
| error_message | TEXT | NULL | - | 에러 메시지 (실패 시) |
| created_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | NOT NULL | CURRENT_TIMESTAMP | 수정일시 |
| completed_at | TIMESTAMP | NULL | - | 완료일시 |

**제약조건**:
- PRIMARY KEY: id
- INDEX: idx_roadmap_tasks_user_id (user_id)
- INDEX: idx_roadmap_tasks_status (status)
- CHECK: status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')
- CHECK: progress >= 0 AND progress <= 100

**비즈니스 규칙**:
- 작업 ID는 클라이언트에게 반환되어 상태 조회에 사용
- COMPLETED 또는 FAILED 상태일 때만 completed_at 설정
- 진행 중 작업은 사용자당 최대 1개 (중복 방지)

---

## 4. 인덱스 전략

### 4.1 주요 인덱스

| 테이블 | 인덱스명 | 컬럼 | 타입 | 목적 |
|--------|---------|------|------|------|
| lifecycle_events | idx_lifecycle_events_user_id | user_id | B-Tree | 사용자별 이벤트 조회 |
| lifecycle_events | idx_lifecycle_events_event_type | event_type | B-Tree | 이벤트 유형별 필터링 |
| roadmaps | idx_roadmaps_user_id | user_id | B-Tree | 사용자별 로드맵 조회 |
| roadmaps | idx_roadmaps_task_id | task_id | B-Tree | 작업 ID로 로드맵 조회 |
| roadmap_stages | idx_roadmap_stages_roadmap_id | roadmap_id | B-Tree | 로드맵별 단계 조회 |
| roadmap_tasks | idx_roadmap_tasks_user_id | user_id | B-Tree | 사용자별 작업 조회 |
| roadmap_tasks | idx_roadmap_tasks_status | status | B-Tree | 진행 중 작업 조회 |

### 4.2 복합 인덱스

| 테이블 | 인덱스명 | 컬럼 | 목적 |
|--------|---------|------|------|
| roadmaps | uk_roadmaps_user_version | (user_id, version) | 사용자별 버전 유니크 보장 |
| roadmap_stages | uk_roadmap_stages_roadmap_stage | (roadmap_id, stage_number) | 로드맵별 단계 번호 유니크 보장 |

---

## 5. 데이터 보존 정책

### 5.1 버전 관리
- **로드맵 버전**: 사용자당 최대 3개 유지
- **삭제 정책**: 4번째 버전 생성 시 가장 오래된 버전 자동 삭제
- **CASCADE 삭제**: roadmap 삭제 시 roadmap_stages, execution_guides 자동 삭제

### 5.2 작업 로그
- **roadmap_tasks**: 완료된 작업은 30일 후 자동 삭제 (배치 작업)
- **COMPLETED/FAILED**: 완료 또는 실패한 작업은 completed_at 기준 30일 보관

### 5.3 백업
- **일일 백업**: 매일 03:00 AM (KST) 전체 백업
- **보관 기간**: 최근 7일 백업 유지
- **주간 백업**: 매주 일요일 03:00 AM 주간 백업 (4주 보관)

---

## 6. 성능 최적화

### 6.1 쿼리 최적화
- 사용자별 최신 로드맵 조회 시 Redis 캐싱 활용 (TTL 30분)
- 로드맵 조회 시 roadmap_stages JOIN 최적화 (stage_number 정렬)
- 진행 중 작업 조회 시 status 인덱스 활용

### 6.2 캐싱 전략
- **캐시 키**: `roadmap:{userId}` 또는 `roadmap:{userId}:{version}`
- **캐시 TTL**: 30분
- **무효화 시점**: 로드맵 재설계 완료, 생애주기 이벤트 수정

### 6.3 파티셔닝
- 현재 데이터 규모에서는 파티셔닝 불필요
- 향후 사용자 100만명 초과 시 user_id 기준 파티셔닝 고려

---

## 7. 보안 및 제약사항

### 7.1 데이터 보안
- **민감 데이터**: 없음 (개인 식별 정보는 User 서비스에서 관리)
- **암호화**: 불필요 (재무 데이터는 숫자만 저장)

### 7.2 동시성 제어
- **중복 작업 방지**: 사용자별 진행 중 작업 1개 제한 (Application Level)
- **버전 충돌 방지**: UNIQUE 제약조건 (user_id, version)

### 7.3 데이터 무결성
- **외래 키 제약**: roadmap_stages, execution_guides → roadmaps (CASCADE)
- **체크 제약**: event_type, status, 날짜 형식, 숫자 범위
- **NOT NULL 제약**: 필수 필드 보장

---

## 8. 데이터 마이그레이션

### 8.1 초기 데이터
- 초기 데이터 없음 (사용자 생성 데이터만 존재)

### 8.2 버전 업그레이드
- **스키마 변경**: Flyway 또는 Liquibase 활용
- **마이그레이션 스크립트**: `/db/migration/V{version}__description.sql`
- **롤백 계획**: 각 마이그레이션마다 롤백 스크립트 준비

---

## 9. 모니터링

### 9.1 성능 모니터링
- **슬로우 쿼리**: 1초 이상 쿼리 로깅
- **인덱스 활용도**: 주간 인덱스 사용 통계 확인
- **테이블 크기**: 월별 테이블 크기 증가율 모니터링

### 9.2 데이터 품질
- **NULL 비율**: 컬럼별 NULL 비율 모니터링
- **중복 데이터**: 주기적 중복 데이터 검사
- **고아 레코드**: 외래 키 참조 무결성 검증

---

## 10. 참조

- **ERD**: `/design/backend/database/roadmap-erd.puml`
- **스키마 스크립트**: `/design/backend/database/roadmap-schema.psql`
- **클래스 설계서**: `/design/backend/class/roadmap-class-design.md`
- **API 명세서**: `/design/backend/api/roadmap-service-api.yaml`
