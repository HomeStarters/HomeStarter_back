# Housing Service - 데이터베이스 설계서

## 개요

- **서비스명**: Housing Service
- **데이터베이스**: PostgreSQL
- **스키마명**: housing
- **설계 날짜**: 2024-12-29

## 데이터베이스 선정 이유

1. **관계형 데이터 모델**: 주택-교통호재-출퇴근시간의 명확한 1:N 관계
2. **트랜잭션 지원**: ACID 특성이 중요한 주택 정보 관리
3. **공간 데이터 지원**: PostGIS 확장으로 위치 기반 검색 지원 가능
4. **JSON 타입 지원**: 복잡한 정보(ComplexInfo, LivingEnvironment)를 JSONB로 효율적 저장
5. **성능과 안정성**: 대규모 데이터 처리에 검증된 RDBMS

## 테이블 설계

### 1. housings (주택 정보)

주택의 기본 정보를 저장하는 메인 테이블입니다.

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| housing_id | BIGSERIAL | NO | - | 주택 ID (PK) |
| user_id | VARCHAR(100) | NO | - | 사용자 ID |
| housing_name | VARCHAR(200) | NO | - | 주택명 |
| housing_type | VARCHAR(50) | NO | - | 주택 유형 (APARTMENT, OFFICETEL, VILLA, HOUSE) |
| price | DECIMAL(15,0) | NO | - | 가격 (원) |
| move_in_date | VARCHAR(7) | YES | - | 입주희망년월 (YYYY-MM) |
| completion_date | DATE | YES | - | 준공일 |
| full_address | TEXT | NO | - | 전체 주소 |
| road_address | TEXT | YES | - | 도로명 주소 |
| jibun_address | TEXT | YES | - | 지번 주소 |
| latitude | DECIMAL(10,7) | YES | - | 위도 |
| longitude | DECIMAL(11,7) | YES | - | 경도 |
| complex_info | JSONB | YES | - | 단지 정보 (JSON) |
| living_environment | JSONB | YES | - | 생활환경 정보 (JSON) |
| is_goal | BOOLEAN | NO | false | 최종목표 주택 여부 |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | 수정 시각 |

**인덱스**:
- PRIMARY KEY: housing_id
- INDEX: user_id (주택 목록 조회)
- INDEX: user_id, is_goal (최종목표 주택 조회)
- INDEX: latitude, longitude (공간 검색용)

**제약조건**:
- housing_type CHECK (housing_type IN ('APARTMENT', 'OFFICETEL', 'VILLA', 'HOUSE'))
- price CHECK (price > 0)
- 사용자당 최종목표 주택은 1개만 (UNIQUE INDEX: user_id WHERE is_goal = true)

**JSON 컬럼 구조**:

```json
// complex_info
{
  "complexName": "string",
  "totalHouseholds": "integer",
  "totalDong": "integer",
  "totalFloors": "integer",
  "parkingCount": "integer",
  "moveInDate": "YYYY-MM",
  "constructionCompany": "string",
  "houseArea": "decimal",
  "exclusiveArea": "decimal",
  "floor": "integer",
  "direction": "string"
}

// living_environment
{
  "sunlightLevel": "VERY_GOOD|GOOD|AVERAGE|POOR",
  "noiseLevel": "VERY_QUIET|QUIET|NORMAL|NOISY",
  "nearbySchools": ["string"],
  "nearbyMarts": ["string"],
  "nearbyHospitals": ["string"]
}
```

---

### 2. transportations (교통호재)

주택별 교통호재 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| transportation_id | BIGSERIAL | NO | - | 교통호재 ID (PK) |
| housing_id | BIGINT | NO | - | 주택 ID (FK) |
| transport_type | VARCHAR(50) | NO | - | 교통수단 유형 (SUBWAY, BUS, TRAIN) |
| line_name | VARCHAR(100) | YES | - | 노선명 (예: 2호선, 9호선) |
| station_name | VARCHAR(200) | NO | - | 역/정류장명 |
| distance | DECIMAL(6,2) | YES | - | 거리 (m) |
| walking_time | INTEGER | YES | - | 도보 소요시간 (분) |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | 생성 시각 |

**인덱스**:
- PRIMARY KEY: transportation_id
- INDEX: housing_id (주택별 교통호재 조회)

**제약조건**:
- FOREIGN KEY: housing_id REFERENCES housings(housing_id) ON DELETE CASCADE
- transport_type CHECK (transport_type IN ('SUBWAY', 'BUS', 'TRAIN'))
- distance CHECK (distance >= 0)
- walking_time CHECK (walking_time >= 0)

---

### 3. commute_times (출퇴근 시간)

교통호재별 출퇴근 시간 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | NULL | 기본값 | 설명 |
|--------|------|------|--------|------|
| commute_time_id | BIGSERIAL | NO | - | 출퇴근시간 ID (PK) |
| transportation_id | BIGINT | NO | - | 교통호재 ID (FK) |
| self_before_9am | INTEGER | YES | - | 본인 출근(9시 이전 도착) 소요시간 (분) |
| self_after_6pm | INTEGER | YES | - | 본인 퇴근(18시 이후 출발) 소요시간 (분) |
| spouse_before_9am | INTEGER | YES | - | 배우자 출근(9시 이전 도착) 소요시간 (분) |
| spouse_after_6pm | INTEGER | YES | - | 배우자 퇴근(18시 이후 출발) 소요시간 (분) |
| created_at | TIMESTAMP | NO | CURRENT_TIMESTAMP | 생성 시각 |

**인덱스**:
- PRIMARY KEY: commute_time_id
- UNIQUE INDEX: transportation_id (교통호재당 1개의 출퇴근시간 정보)

**제약조건**:
- FOREIGN KEY: transportation_id REFERENCES transportations(transportation_id) ON DELETE CASCADE
- self_before_9am CHECK (self_before_9am >= 0)
- self_after_6pm CHECK (self_after_6pm >= 0)
- spouse_before_9am CHECK (spouse_before_9am >= 0)
- spouse_after_6pm CHECK (spouse_after_6pm >= 0)

---

## 테이블 관계도

```
housings (1) ─────< (N) transportations (1) ───── (1) commute_times
   │
   └─ user_id (사용자별 주택 관리)
   └─ is_goal (사용자별 최종목표 주택 1개)
```

**관계 설명**:
1. **Housing → Transportation**: 1:N (한 주택은 여러 교통호재를 가질 수 있음)
2. **Transportation → CommuteTime**: 1:1 (한 교통호재는 하나의 출퇴근시간 정보를 가짐)
3. **CASCADE DELETE**: 주택 삭제 시 관련 교통호재와 출퇴근시간 자동 삭제

---

## JPA 매핑 전략

### 1. Housing Entity
```java
@Entity
@Table(name = "housings", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_goal", columnList = "user_id, is_goal")
})
public class Housing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "housing_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "housing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transportation> transportations = new ArrayList<>();

    @Convert(converter = ComplexInfoConverter.class)
    @Column(name = "complex_info", columnDefinition = "jsonb")
    private ComplexInfo complexInfo;

    @Convert(converter = LivingEnvironmentConverter.class)
    @Column(name = "living_environment", columnDefinition = "jsonb")
    private LivingEnvironment livingEnvironment;
}
```

### 2. Address Embeddable
```java
@Embeddable
public class Address {
    @Column(name = "full_address", nullable = false, columnDefinition = "TEXT")
    private String fullAddress;

    @Column(name = "road_address", columnDefinition = "TEXT")
    private String roadAddress;

    @Column(name = "jibun_address", columnDefinition = "TEXT")
    private String jibunAddress;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 7)
    private BigDecimal longitude;
}
```

### 3. Transportation Entity
```java
@Entity
@Table(name = "transportations", indexes = {
    @Index(name = "idx_housing_id", columnList = "housing_id")
})
public class Transportation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transportation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "housing_id", nullable = false)
    private Housing housing;

    @OneToOne(mappedBy = "transportation", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommuteTime commuteTime;
}
```

### 4. CommuteTime Entity
```java
@Entity
@Table(name = "commute_times")
public class CommuteTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commute_time_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportation_id", nullable = false, unique = true)
    private Transportation transportation;

    @Column(name = "self_before_9am")
    private Integer selfBefore9am;

    @Column(name = "self_after_6pm")
    private Integer selfAfter6pm;

    @Column(name = "spouse_before_9am")
    private Integer spouseBefore9am;

    @Column(name = "spouse_after_6pm")
    private Integer spouseAfter6pm;
}
```

---

## 인덱스 전략

### 1. 주요 조회 패턴별 인덱스

| 조회 패턴 | 인덱스 | 설명 |
|----------|--------|------|
| 사용자별 주택 목록 | (user_id) | 주택 목록 조회 성능 |
| 최종목표 주택 조회 | (user_id, is_goal) | 최종목표 주택 빠른 조회 |
| 공간 검색 | (latitude, longitude) | 위치 기반 검색 |
| 교통호재 조회 | (housing_id) | 주택별 교통호재 목록 |

### 2. Unique 제약

```sql
-- 사용자당 최종목표 주택 1개 제한
CREATE UNIQUE INDEX idx_user_goal_housing
ON housings(user_id)
WHERE is_goal = true;

-- 교통호재당 출퇴근시간 1개 제한
CREATE UNIQUE INDEX idx_transportation_commute
ON commute_times(transportation_id);
```

---

## 데이터 타입 선택 근거

### 1. 숫자 타입
- **price**: DECIMAL(15,0) - 최대 999조원까지 표현 가능, 정확한 금액 저장
- **latitude**: DECIMAL(10,7) - 위도 정밀도 7자리 (약 1cm)
- **longitude**: DECIMAL(11,7) - 경도 정밀도 7자리 (약 1cm)
- **distance**: DECIMAL(6,2) - 최대 9999.99m, 소수점 2자리
- **walking_time**: INTEGER - 분 단위 정수

### 2. 문자 타입
- **housing_name**: VARCHAR(200) - 일반적인 주택명 길이
- **user_id**: VARCHAR(100) - JWT subject 길이
- **address**: TEXT - 주소 길이 제한 없음
- **housing_type**: VARCHAR(50) - Enum 값 저장

### 3. 날짜/시간 타입
- **move_in_date**: VARCHAR(7) - "YYYY-MM" 형식 (월 단위)
- **completion_date**: DATE - 정확한 준공일
- **created_at/updated_at**: TIMESTAMP - 마이크로초 단위 시각

### 4. JSON 타입
- **complex_info/living_environment**: JSONB - 인덱싱 가능, 쿼리 성능 우수

---

## 성능 최적화 전략

### 1. 조회 최적화
- **Lazy Loading**: Transportation, CommuteTime은 지연 로딩
- **Fetch Join**: 필요 시 명시적 Fetch Join 사용
- **캐시**: Redis를 통한 자주 조회되는 데이터 캐싱

### 2. 쓰기 최적화
- **Batch Insert**: 여러 교통호재 일괄 저장
- **Cascade**: 주택 삭제 시 관련 데이터 자동 삭제

### 3. 인덱스 최적화
- **Partial Index**: is_goal = true 조건부 인덱스
- **Composite Index**: 복합 조회 패턴 지원

---

## 확장 가능성

### 1. 공간 검색 지원 (PostGIS)
```sql
-- PostGIS 확장 설치
CREATE EXTENSION IF NOT EXISTS postgis;

-- 공간 컬럼 추가
ALTER TABLE housings ADD COLUMN geom GEOMETRY(Point, 4326);

-- 공간 인덱스 생성
CREATE INDEX idx_housing_geom ON housings USING GIST(geom);

-- 공간 쿼리 예시: 반경 1km 이내 주택 검색
SELECT * FROM housings
WHERE ST_DWithin(geom, ST_MakePoint(longitude, latitude)::geography, 1000);
```

### 2. 파티셔닝 (대용량 데이터)
```sql
-- 사용자 ID 기반 파티셔닝 (샤딩 준비)
CREATE TABLE housings_partition (LIKE housings INCLUDING ALL)
PARTITION BY HASH(user_id);

CREATE TABLE housings_p0 PARTITION OF housings_partition
FOR VALUES WITH (MODULUS 4, REMAINDER 0);

CREATE TABLE housings_p1 PARTITION OF housings_partition
FOR VALUES WITH (MODULUS 4, REMAINDER 1);
-- ...
```

### 3. 이력 관리 (추후 확장)
```sql
-- 주택 변경 이력 테이블
CREATE TABLE housing_history (
    history_id BIGSERIAL PRIMARY KEY,
    housing_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    changed_fields JSONB,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100)
);
```

---

## 백업 및 복구 전략

### 1. 백업 주기
- **전체 백업**: 매일 새벽 2시
- **증분 백업**: 매 시간
- **트랜잭션 로그**: 실시간

### 2. 보관 정책
- **일일 백업**: 30일 보관
- **주간 백업**: 12주 보관
- **월간 백업**: 1년 보관

### 3. 복구 테스트
- 월 1회 복구 테스트 실행
- RTO (복구 목표 시간): 1시간
- RPO (복구 시점 목표): 1시간

---

## 보안 고려사항

### 1. 접근 제어
```sql
-- 애플리케이션 전용 사용자 생성
CREATE USER housing_app WITH PASSWORD 'strong_password';

-- 필요한 권한만 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA housing TO housing_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA housing TO housing_app;

-- DDL 권한 제거
REVOKE CREATE ON SCHEMA housing FROM housing_app;
```

### 2. 데이터 암호화
- **전송 암호화**: TLS/SSL 연결 강제
- **저장 암호화**: PostgreSQL Transparent Data Encryption (TDE)
- **민감 정보**: 주소는 암호화하지 않음 (검색 필요)

### 3. 감사 로그
```sql
-- 감사 로그 활성화
ALTER DATABASE housing SET log_statement = 'mod';
ALTER DATABASE housing SET log_duration = on;
```

---

## 마이그레이션 전략

### 1. 스키마 버전 관리
- **도구**: Flyway 또는 Liquibase
- **버전 형식**: V{version}__{description}.sql
- **예시**: V1__create_housing_tables.sql

### 2. 무중단 배포
1. **컬럼 추가**: nullable로 추가 → 데이터 마이그레이션 → not null 제약 추가
2. **컬럼 삭제**: deprecated 마킹 → 사용 중단 → 삭제
3. **테이블 분리**: 새 테이블 생성 → 데이터 동기화 → 전환 → 구 테이블 삭제

---

## 모니터링 지표

### 1. 성능 지표
- **조회 성능**: 평균 응답 시간 < 100ms
- **쓰기 성능**: 평균 트랜잭션 시간 < 200ms
- **인덱스 효율**: Index Scan / Seq Scan 비율 > 90%

### 2. 용량 지표
- **테이블 크기**: 주간 증가율 모니터링
- **인덱스 크기**: 테이블 대비 인덱스 비율
- **JSONB 컬럼**: 평균 크기 추적

### 3. 경고 임계값
- **커넥션 풀**: 사용률 > 80%
- **디스크 사용**: 여유 공간 < 20%
- **슬로우 쿼리**: 실행 시간 > 1초

---

## 참조 문서

- **클래스 설계서**: design/backend/class/housing-class-design.md
- **API 설계서**: design/backend/api/housing-api-spec.yaml
- **ERD**: design/backend/database/housing-erd.puml
- **스키마 스크립트**: design/backend/database/housing-schema.psql
