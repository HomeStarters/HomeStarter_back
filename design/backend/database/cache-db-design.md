# Redis 캐시 데이터베이스 설계서

## 1. 설계 개요

### 1.1 설계 정보
- **캐시 시스템**: Redis 7.x
- **격리 방식**: Redis Database 번호 (0~15)
- **설계 원칙**: 캐시DB분리원칙
- **작성일**: 2025-12-29

### 1.2 참조 문서
- 공통설계원칙: `claude/common-principles.md`
- 데이터설계가이드: `claude/data-design.md`
- 각 서비스별 데이터 설계서: `design/backend/database/{service-name}.md`

### 1.3 설계 원칙
- **논리적 분리**: Redis database 번호로 서비스별 캐시 영역 분리
- **공통 캐시 분리**: 여러 서비스가 공유하는 데이터는 별도 database에 저장
- **격리 원칙**: 각 서비스는 자신의 database만 접근 (타 서비스 직접 참조 금지)
- **Key Naming Convention**: `{domain}:{entity}:{id}` 형식

---

## 2. Redis Database 할당표

### 2.1 Database 할당 현황

| DB 번호 | 서비스/용도 | 설명 | 주요 키 패턴 |
|---------|-----------|------|-------------|
| **0** | 공통 영역 | 세션, 인증 토큰, 공유 설정 | `session:*`, `token:*` |
| **1** | User 서비스 | 사용자 프로필 캐시, 로그인 실패 | `profile:*`, `login:fail:*` |
| **2** | Asset 서비스 | 자산정보 캐시, 총액 캐시 | `asset:*`, `asset:summary:*` |
| **3** | Loan 서비스 | 대출상품 목록 캐시 | `loan:product:*`, `loan:list:*` |
| **4** | Housing 서비스 | 주택정보 캐시, 주택 목록 | `housing:*`, `housing:list:*` |
| **5** | Calculator 서비스 | 계산 결과 캐시 | `calc:result:*`, `calc:temp:*` |
| **6** | Roadmap 서비스 | 로드맵 결과 캐시, AI 응답 | `roadmap:*`, `roadmap:ai:*` |
| **7~14** | 예비 | 향후 서비스 확장 | - |
| **15** | 예비/테스트 | 개발/테스트 환경 전용 | - |

### 2.2 공통 영역 (DB 0) 상세

#### 용도
여러 서비스가 공유하는 인증, 세션, 공통 설정 데이터 저장

#### 주요 키 유형

| 키 패턴 | 설명 | TTL | 예시 |
|---------|------|-----|------|
| `session:{userId}` | 사용자 세션 | 7일 | session:john123 |
| `token:blacklist:{tokenHash}` | 블랙리스트 토큰 | 토큰 남은 시간 | token:blacklist:a3b2c1... |
| `config:*` | 시스템 설정 | 무제한 | config:max-login-attempts |

#### 접근 권한
- **User 서비스**: 읽기/쓰기
- **모든 서비스**: 읽기 (세션 검증)

---

## 3. 서비스별 캐시 전략

### 3.1 User 서비스 (DB 1)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `login:fail:{userId}` | INTEGER | 1800초 (30분) | 로그인 실패 횟수 추적 |
| `profile:{userId}` | JSON (UserProfileDto) | 3600초 (1시간) | 사용자 프로필 캐시 |

#### 무효화 전략
- **프로필 수정 시**: DEL profile:{userId}
- **로그인 성공 시**: DEL login:fail:{userId}

#### 예상 메모리 사용량
- 사용자 10만명 기준: ~50MB
- 프로필 당 평균 500bytes

---

### 3.2 Asset 서비스 (DB 2)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `asset:{userId}` | JSON (자산 목록) | 3600초 (1시간) | 사용자 자산 전체 캐시 |
| `asset:summary:{userId}` | JSON (총액 정보) | 3600초 (1시간) | 자산/대출/소득/지출 총액 |

#### 무효화 전략
- **자산 추가/수정/삭제 시**: DEL asset:{userId}, DEL asset:summary:{userId}
- **자동 재캐싱**: 다음 조회 시

#### 예상 메모리 사용량
- 사용자 10만명 기준: ~100MB
- 자산 정보 당 평균 1KB

---

### 3.3 Loan 서비스 (DB 3)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `loan:product:{id}` | JSON (대출상품 상세) | 86400초 (24시간) | 대출상품 개별 캐시 |
| `loan:list:all` | JSON (대출상품 목록) | 3600초 (1시간) | 전체 대출상품 목록 |
| `loan:list:filter:{hash}` | JSON (필터링 결과) | 1800초 (30분) | 필터링된 목록 캐시 |

#### 무효화 전략
- **대출상품 수정 시**: DEL loan:product:{id}, DEL loan:list:*
- **관리자 등록/수정 시**: 전체 목록 캐시 무효화

#### 예상 메모리 사용량
- 대출상품 100개 기준: ~5MB
- 상품 당 평균 50KB

---

### 3.4 Housing 서비스 (DB 4)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `housing:{userId}:{housingId}` | JSON (주택 상세) | 3600초 (1시간) | 주택 개별 캐시 |
| `housing:list:{userId}` | JSON (주택 목록) | 1800초 (30분) | 사용자별 주택 목록 |
| `housing:final:{userId}` | JSON (최종목표 주택) | 7200초 (2시간) | 최종목표 주택 캐시 |

#### 무효화 전략
- **주택 추가/수정 시**: DEL housing:{userId}:{housingId}, DEL housing:list:{userId}
- **최종목표 변경 시**: DEL housing:final:{userId}

#### 예상 메모리 사용량
- 사용자 10만명, 평균 3개 주택 기준: ~150MB
- 주택 정보 당 평균 500bytes

---

### 3.5 Calculator 서비스 (DB 5)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `calc:result:{userId}:{housingId}:{loanId}` | JSON (계산 결과) | 3600초 (1시간) | 계산 결과 캐시 |
| `calc:temp:{sessionId}` | JSON (임시 계산 데이터) | 600초 (10분) | 계산 중 임시 데이터 |

#### 무효화 전략
- **자산/주택 변경 시**: DEL calc:result:{userId}:*
- **임시 데이터**: 자동 만료 (10분)

#### 예상 메모리 사용량
- 활성 계산 1만건 기준: ~20MB
- 계산 결과 당 평균 2KB

---

### 3.6 Roadmap 서비스 (DB 6)

#### 캐시 대상

| 키 패턴 | 값 구조 | TTL | 용도 |
|---------|---------|-----|------|
| `roadmap:{userId}` | JSON (로드맵 전체) | 7200초 (2시간) | 로드맵 전체 캐시 |
| `roadmap:ai:{userId}` | JSON (AI 응답 원본) | 86400초 (24시간) | AI 응답 원본 캐시 |
| `roadmap:version:{userId}:{version}` | JSON (로드맵 버전) | 86400초 (24시간) | 이전 버전 캐시 |

#### 무효화 전략
- **로드맵 재생성 시**: DEL roadmap:{userId}, 새 버전 캐시 생성
- **생애주기 이벤트 변경 시**: DEL roadmap:{userId}

#### 예상 메모리 사용량
- 로드맵 1만건, 버전 3개 기준: ~100MB
- 로드맵 당 평균 10KB (AI 응답 포함)

---

## 4. Key Naming Convention

### 4.1 공통 규칙
```
{domain}:{entity}:{id}[:{sub-entity}]
```

### 4.2 예시

| 서비스 | 도메인 | 엔티티 | 예시 키 |
|--------|--------|--------|---------|
| User | user | profile | profile:john123 |
| User | login | fail | login:fail:john123 |
| Asset | asset | summary | asset:summary:john123 |
| Loan | loan | product | loan:product:1 |
| Housing | housing | - | housing:john123:house1 |
| Calculator | calc | result | calc:result:john123:house1:loan1 |
| Roadmap | roadmap | ai | roadmap:ai:john123 |

### 4.3 명명 규칙
- **소문자 사용**: 모든 키는 소문자
- **콜론 구분**: 계층 구조는 `:` 으로 구분
- **와일드카드 지원**: 패턴 매칭 가능하도록 설계
- **명확성**: 키만 보고 용도 파악 가능하도록

---

## 5. 성능 및 용량 계획

### 5.1 총 예상 메모리 사용량 (1년 기준)

| Database | 서비스 | 예상 메모리 |
|----------|--------|-----------|
| DB 0 | 공통 (세션, 토큰) | 500MB |
| DB 1 | User | 50MB |
| DB 2 | Asset | 100MB |
| DB 3 | Loan | 5MB |
| DB 4 | Housing | 150MB |
| DB 5 | Calculator | 20MB |
| DB 6 | Roadmap | 100MB |
| **합계** | | **925MB (~1GB)** |

### 5.2 확장 계획 (5년 기준)

| Database | 서비스 | 예상 메모리 |
|----------|--------|-----------|
| DB 0 | 공통 | 5GB |
| DB 1 | User | 500MB |
| DB 2 | Asset | 1GB |
| DB 3 | Loan | 50MB |
| DB 4 | Housing | 1.5GB |
| DB 5 | Calculator | 200MB |
| DB 6 | Roadmap | 1GB |
| **합계** | | **~10GB** |

### 5.3 Redis 서버 사양 권장
- **초기 (1년)**: Redis 인스턴스 2GB RAM
- **확장 (5년)**: Redis 인스턴스 16GB RAM
- **HA 구성**: Master-Replica 구성 권장
- **백업**: RDB 스냅샷 일 1회

---

## 6. 애플리케이션 설정

### 6.1 Spring Boot 설정 예시

#### User 서비스 (`application.yml`)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      database: 1  # User 서비스 전용 DB
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2
```

#### Asset 서비스 (`application.yml`)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      database: 2  # Asset 서비스 전용 DB
      password: ${REDIS_PASSWORD:}
```

#### 공통 영역 접근 (모든 서비스)
```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory commonRedisConnectionFactory() {
        // DB 0 (공통 영역) 접근용
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        config.setDatabase(0);  // 공통 영역
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisConnectionFactory serviceRedisConnectionFactory() {
        // 각 서비스 전용 DB 접근용
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        config.setDatabase(1);  // 서비스별 DB 번호
        return new LettuceConnectionFactory(config);
    }
}
```

---

## 7. 모니터링 및 관리

### 7.1 모니터링 항목

| 항목 | 명령어 | 임계값 |
|------|--------|--------|
| Database별 키 개수 | `DBSIZE` | - |
| Database별 메모리 사용량 | `INFO memory` | > 80% maxmemory |
| Hit/Miss 비율 | `INFO stats` | Hit < 70% |
| Eviction 발생 | `INFO stats` | > 100/sec |

### 7.2 Redis CLI 모니터링 예시

#### Database별 키 개수 확인
```bash
# DB 0 (공통)
redis-cli -n 0 DBSIZE

# DB 1 (User)
redis-cli -n 1 DBSIZE

# DB 2 (Asset)
redis-cli -n 2 DBSIZE
```

#### 키 패턴별 조회
```bash
# User 서비스 프로필 캐시
redis-cli -n 1 KEYS "profile:*"

# Loan 서비스 상품 목록
redis-cli -n 3 KEYS "loan:list:*"
```

#### TTL 확인
```bash
# 특정 키의 남은 시간
redis-cli -n 1 TTL "profile:john123"
```

### 7.3 정기 유지보수

| 작업 | 주기 | 명령어/도구 |
|------|------|-----------|
| 메모리 분석 | 주 1회 | `redis-cli --bigkeys` |
| 키 분포 확인 | 주 1회 | `SCAN` + `TYPE` |
| 만료 키 정리 | 자동 | Redis 자동 만료 |
| 백업 | 일 1회 | `BGSAVE` |

---

## 8. Cluster 모드 전환 시 고려사항

### 8.1 현재 설계의 제약
Redis Cluster 모드에서는 database 번호가 0만 사용 가능하므로, 현재 database 분리 방식 사용 불가

### 8.2 Cluster 전환 전략
Cluster 전환이 필요한 경우, Key Prefix 방식으로 전환:

| 현재 (Database 분리) | Cluster (Key Prefix) |
|---------------------|---------------------|
| DB 1: profile:john123 | user:profile:john123 |
| DB 2: asset:john123 | asset:data:john123 |
| DB 3: loan:product:1 | loan:product:1 |

### 8.3 마이그레이션 계획
1. **Key Prefix 추가**: 모든 키에 서비스 prefix 추가
2. **이중 쓰기**: 기존 DB + Cluster 동시 쓰기
3. **검증**: Cluster 데이터 검증
4. **전환**: Cluster로 전환
5. **기존 DB 정리**: 기존 database 데이터 삭제

---

## 9. 보안 및 접근 제어

### 9.1 접근 제어 원칙
- **서비스 격리**: 각 서비스는 자신의 database만 접근
- **공통 영역 보호**: 공통 영역(DB 0)은 읽기 전용 (User 서비스 제외)
- **인증 필수**: Redis AUTH 사용 권장

### 9.2 ACL 설정 (Redis 6.0+)

#### 서비스별 사용자 생성
```bash
# User 서비스 계정
ACL SETUSER user_service on >password ~* +@all -@dangerous

# Asset 서비스 계정
ACL SETUSER asset_service on >password ~asset:* +@read +@write +@set

# 공통 영역 읽기 전용
ACL SETUSER readonly_common on >password ~session:* ~token:* +@read
```

### 9.3 비밀번호 관리
- **환경 변수**: `REDIS_PASSWORD`로 관리
- **암호화 전송**: TLS/SSL 사용 권장 (프로덕션)
- **정기 교체**: 3개월마다 비밀번호 교체

---

## 10. 백업 및 복구

### 10.1 백업 전략

| 백업 유형 | 방식 | 주기 | 보관 기간 |
|----------|------|------|----------|
| RDB 스냅샷 | BGSAVE | 일 1회 (새벽 3시) | 7일 |
| AOF 로그 | appendonly.aof | 실시간 | 3일 |

### 10.2 백업 설정 (`redis.conf`)
```conf
# RDB 스냅샷
save 900 1      # 15분 내 1개 이상 키 변경
save 300 10     # 5분 내 10개 이상 키 변경
save 60 10000   # 1분 내 10000개 이상 키 변경

# AOF 설정
appendonly yes
appendfsync everysec  # 1초마다 fsync
```

### 10.3 복구 절차
1. **RDB 파일 복사**: dump.rdb → Redis 데이터 디렉토리
2. **Redis 재시작**: systemctl restart redis
3. **데이터 검증**: KEYS 명령으로 확인
4. **서비스 재시작**: 애플리케이션 재시작

---

## 11. 변경 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0 | 2025-12-29 | 길동 (아키텍트) | 초기 캐시 DB 설계 완료 |

---

## 12. 결과물

### 12.1 산출물
- **캐시 DB 설계서**: `design/backend/database/cache-db-design.md` (본 문서)
- **Redis 설정 템플릿**: 각 서비스별 `application.yml` 설정 예시 포함

### 12.2 다음 단계
1. **Redis 서버 구축**: 개발/운영 환경별로 Redis 서버 설치
2. **ACL 설정**: 서비스별 Redis 사용자 및 권한 설정
3. **애플리케이션 연동**: Spring Boot Redis 설정 적용
4. **모니터링 구축**: Redis 모니터링 도구 (Redis Insight, Grafana) 설치
