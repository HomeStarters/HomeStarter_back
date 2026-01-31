# Calculator 서비스 백엔드 개발 결과서

## 작성 정보
- **작성일시**: 2026-01-05
- **작성자**: 이준호 (Backend Developer)
- **서비스명**: Calculator Service
- **개발 유형**: RabbitMQ → Kafka 마이그레이션 및 누락 클래스 개발

---

## 1. 개발 개요

### 1.1 작업 목적
- RabbitMQ 메시지 브로커를 Kafka로 마이그레이션
- 클래스 설계서에 명시된 누락된 클래스 개발
- 외부 서비스 이벤트 처리를 위한 Kafka Consumer 구현

### 1.2 작업 범위
- **의존성 변경**: spring-boot-starter-amqp → spring-kafka
- **설정 변경**: RabbitMQ 설정 → Kafka 설정
- **신규 클래스 개발**:
  - KafkaConfig.java (설정)
  - AssetUpdatedEvent.java (이벤트 DTO)
  - HousingUpdatedEvent.java (이벤트 DTO)
  - AssetEventListener.java (Kafka Consumer)
  - HousingEventListener.java (Kafka Consumer)

---

## 2. 변경 사항 상세

### 2.1 의존성 변경

#### build.gradle
```gradle
// 변경 전
implementation 'org.springframework.boot:spring-boot-starter-amqp'

// 변경 후
implementation 'org.springframework.kafka:spring-kafka'
```

**변경 사유**:
- Kafka를 사용한 이벤트 기반 아키텍처로 전환
- Kafka의 높은 처리량과 확장성 활용

---

### 2.2 설정 변경

#### application.yml

**변경 전 (RabbitMQ 설정)**:
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2
          max-interval: 10000
```

**변경 후 (Kafka 설정)**:
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: calculator-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**설정 설명**:
- `bootstrap-servers`: Kafka 브로커 주소 (환경변수: KAFKA_BOOTSTRAP_SERVERS)
- `group-id`: Consumer 그룹 ID (calculator-service)
- `auto-offset-reset`: earliest로 설정하여 처음부터 메시지 수신
- `key/value deserializer`: JSON 직렬화/역직렬화 사용
- `trusted.packages`: 모든 패키지 신뢰 설정

---

#### IntelliJ 실행 프로파일 (.run/CalculatorApplication.run.xml)

**변경 전 (RabbitMQ 환경변수)**:
```xml
<entry key="RABBITMQ_HOST" value="localhost" />
<entry key="RABBITMQ_PORT" value="5672" />
<entry key="RABBITMQ_USERNAME" value="guest" />
<entry key="RABBITMQ_PASSWORD" value="guest" />
```

**변경 후 (Kafka 환경변수)**:
```xml
<entry key="KAFKA_BOOTSTRAP_SERVERS" value="121.129.45.98:9002" />
```

**환경변수 설명**:
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka 브로커 주소 (121.129.45.98:9002)

---

### 2.3 신규 클래스 개발

#### 2.3.1 KafkaConfig.java

**패키지**: `com.dwj.homestarter.calculator.config`

**책임**: Kafka Consumer 및 Producer 설정

**주요 메소드**:
```java
+ consumerFactory(): ConsumerFactory<String, Object>
+ kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Object>
+ producerFactory(): ProducerFactory<String, Object>
+ kafkaTemplate(): KafkaTemplate<String, Object>
```

**설정 내용**:
- Consumer: JSON 역직렬화, 신뢰할 수 있는 패키지 설정
- Producer: JSON 직렬화
- Listener Container: 이벤트 리스너를 위한 컨테이너 팩토리

**파일 위치**: `calculator-service/src/main/java/com/dwj/homestarter/calculator/config/KafkaConfig.java`

---

#### 2.3.2 AssetUpdatedEvent.java

**패키지**: `com.dwj.homestarter.calculator.service.event`

**책임**: Asset 변경 이벤트 데이터 전송

**필드**:
```java
+ eventId: String              // 이벤트 ID
+ userId: String               // 사용자 ID
+ updatedField: String         // 변경된 필드
+ occurredAt: LocalDateTime    // 이벤트 발생 시각
+ eventType: String            // 이벤트 타입
```

**사용 목적**:
- Asset 서비스에서 자산 정보가 변경될 때 발행되는 이벤트
- Calculator 서비스에서 해당 사용자의 캐시를 무효화하기 위해 사용

**파일 위치**: `calculator-service/src/main/java/com/dwj/homestarter/calculator/service/event/AssetUpdatedEvent.java`

---

#### 2.3.3 HousingUpdatedEvent.java

**패키지**: `com.dwj.homestarter.calculator.service.event`

**책임**: Housing 변경 이벤트 데이터 전송

**필드**:
```java
+ eventId: String              // 이벤트 ID
+ housingId: String            // 주택 ID
+ updatedField: String         // 변경된 필드
+ occurredAt: LocalDateTime    // 이벤트 발생 시각
+ eventType: String            // 이벤트 타입
```

**사용 목적**:
- Housing 서비스에서 주택 정보가 변경될 때 발행되는 이벤트
- Calculator 서비스에서 해당 주택 관련 캐시를 무효화하기 위해 사용

**파일 위치**: `calculator-service/src/main/java/com/dwj/homestarter/calculator/service/event/HousingUpdatedEvent.java`

---

#### 2.3.4 AssetEventListener.java

**패키지**: `com.dwj.homestarter.calculator.service.event`

**책임**: Asset 변경 이벤트 수신 및 캐시 무효화

**주요 메소드**:
```java
+ handleAssetUpdated(payload: Map<String, Object>, topic: String, acknowledgment: Acknowledgment): void
- invalidateCacheByUserId(userId: String): void
```

**처리 로직**:
1. Kafka에서 `asset.updated` 토픽 구독
2. 이벤트 수신 시 userId 추출
3. 해당 사용자의 모든 계산 결과 캐시 무효화
   - 계산 결과 캐시: `calc:{userId}:*`
   - 목록 캐시: `calc:list:{userId}:*`
4. 정상 처리 시 Kafka ACK 전송
5. 오류 발생 시 재시도 (ACK 하지 않음)

**Kafka 설정**:
- **Topic**: `asset.updated`
- **Group ID**: `calculator-service`
- **Container Factory**: `kafkaListenerContainerFactory`

**에러 처리**:
- 오류 발생 시 acknowledge하지 않아 재시도 유도
- 로그를 통한 에러 추적

**파일 위치**: `calculator-service/src/main/java/com/dwj/homestarter/calculator/service/event/AssetEventListener.java`

---

#### 2.3.5 HousingEventListener.java

**패키지**: `com.dwj.homestarter.calculator.service.event`

**책임**: Housing 변경 이벤트 수신 및 캐시 무효화

**주요 메소드**:
```java
+ handleHousingUpdated(payload: Map<String, Object>, topic: String, acknowledgment: Acknowledgment): void
- invalidateCacheByHousingId(housingId: String): void
```

**처리 로직**:
1. Kafka에서 `housing.updated` 토픽 구독
2. 이벤트 수신 시 housingId 추출
3. 해당 주택과 관련된 사용자 ID 목록 조회 (DB 쿼리)
4. 각 사용자의 캐시 무효화
   - 계산 결과 캐시: `calc:{userId}:{housingId}:*`
   - 목록 캐시: `calc:list:{userId}:*`
5. 정상 처리 시 Kafka ACK 전송
6. 오류 발생 시 재시도 (ACK 하지 않음)

**Kafka 설정**:
- **Topic**: `housing.updated`
- **Group ID**: `calculator-service`
- **Container Factory**: `kafkaListenerContainerFactory`

**DB 연동**:
- `CalculatorRepository.findUserIdsByHousingId(housingId)` 사용
- 해당 주택과 관련된 계산 결과를 작성한 모든 사용자 조회

**에러 처리**:
- 오류 발생 시 acknowledge하지 않아 재시도 유도
- 로그를 통한 에러 추적 및 영향받는 사용자 수 기록

**파일 위치**: `calculator-service/src/main/java/com/dwj/homestarter/calculator/service/event/HousingEventListener.java`

---

## 3. 캐시 무효화 전략

### 3.1 Asset 변경 시 캐시 무효화

**트리거**: Asset 서비스에서 자산 정보 변경

**무효화 대상**:
- `calc:{userId}:*` - 해당 사용자의 모든 계산 결과
- `calc:list:{userId}:*` - 해당 사용자의 모든 목록 캐시

**처리 흐름**:
```
Asset 서비스 → asset.updated 토픽 발행
              ↓
AssetEventListener 수신 → userId 추출
              ↓
CacheService.deletePattern() 호출
              ↓
Redis 캐시 삭제 완료
              ↓
Kafka ACK 전송
```

**예상 효과**:
- 자산 정보 변경 시 계산 결과가 실시간으로 무효화됨
- 다음 조회 시 최신 자산 정보로 재계산됨

---

### 3.2 Housing 변경 시 캐시 무효화

**트리거**: Housing 서비스에서 주택 정보 변경

**무효화 대상**:
- `calc:{userId}:{housingId}:*` - 해당 주택 관련 모든 계산 결과
- `calc:list:{userId}:*` - 영향받는 사용자의 모든 목록 캐시

**처리 흐름**:
```
Housing 서비스 → housing.updated 토픽 발행
              ↓
HousingEventListener 수신 → housingId 추출
              ↓
DB 조회: 영향받는 사용자 ID 목록
              ↓
각 사용자별 CacheService.deletePattern() 호출
              ↓
Redis 캐시 삭제 완료
              ↓
Kafka ACK 전송
```

**예상 효과**:
- 주택 정보 변경 시 관련된 모든 계산 결과가 무효화됨
- 여러 사용자가 동일 주택을 조회한 경우 모두 무효화됨

---

## 4. 컴파일 결과

### 4.1 컴파일 명령어
```bash
./gradlew calculator-service:compileJava
```

### 4.2 컴파일 결과
```
> Task :common:generateEffectiveLombokConfig UP-TO-DATE
> Task :common:compileJava UP-TO-DATE
> Task :calculator-service:generateEffectiveLombokConfig
> Task :calculator-service:compileJava

BUILD SUCCESSFUL in 7s
4 actionable tasks: 2 executed, 2 up-to-date
```

**결과**: ✅ 컴파일 성공 (에러 없음)

---

## 5. 개발 완료 항목

### 5.1 의존성 변경
- ✅ build.gradle: RabbitMQ → Kafka 의존성 변경
- ✅ 컴파일 성공 확인

### 5.2 설정 변경
- ✅ application.yml: Kafka 설정 추가
- ✅ IntelliJ 실행 프로파일: Kafka 환경변수 추가

### 5.3 클래스 개발
- ✅ KafkaConfig.java: Kafka Consumer/Producer 설정
- ✅ AssetUpdatedEvent.java: Asset 이벤트 DTO
- ✅ HousingUpdatedEvent.java: Housing 이벤트 DTO
- ✅ AssetEventListener.java: Asset 이벤트 수신 및 캐시 무효화
- ✅ HousingEventListener.java: Housing 이벤트 수신 및 캐시 무효화

### 5.4 컴파일 및 검증
- ✅ 컴파일 성공
- ✅ 모든 클래스 정상 생성
- ✅ 의존성 주입 확인

---

## 6. 테스트 권고사항

### 6.1 단위 테스트
- AssetEventListener 단위 테스트
  - 정상 이벤트 수신 시 캐시 무효화 확인
  - userId 누락 시 처리 확인
  - 예외 발생 시 재시도 확인

- HousingEventListener 단위 테스트
  - 정상 이벤트 수신 시 캐시 무효화 확인
  - housingId 누락 시 처리 확인
  - DB 조회 및 다중 사용자 캐시 무효화 확인

### 6.2 통합 테스트
- Kafka 통합 테스트
  - Asset 이벤트 발행 → 수신 → 캐시 무효화 전체 플로우
  - Housing 이벤트 발행 → 수신 → 캐시 무효화 전체 플로우
  - Kafka Consumer 재시도 메커니즘 확인

- 캐시 무효화 테스트
  - Redis 패턴 매칭 캐시 삭제 확인
  - 다중 사용자 캐시 무효화 확인
  - 캐시 무효화 후 재조회 시 재계산 확인

### 6.3 성능 테스트
- 이벤트 처리 성능
  - 초당 처리 가능한 이벤트 수
  - 평균 처리 시간 측정

- 캐시 무효화 성능
  - 패턴 매칭 캐시 삭제 시간
  - 다중 사용자 캐시 무효화 시간

---

## 7. 배포 전 체크리스트

### 7.1 환경변수 설정
- [ ] `KAFKA_BOOTSTRAP_SERVERS`: Kafka 브로커 주소 확인
- [ ] Kafka 토픽 생성 확인:
  - `asset.updated`
  - `housing.updated`

### 7.2 Kafka 인프라
- [ ] Kafka 브로커 정상 작동 확인
- [ ] 토픽 파티션 및 복제 설정 확인
- [ ] Consumer 그룹 설정 확인

### 7.3 모니터링
- [ ] Kafka Consumer Lag 모니터링 설정
- [ ] 이벤트 처리 성공/실패 메트릭 설정
- [ ] 캐시 무효화 성공률 모니터링 설정

---

## 8. 알려진 이슈 및 제한사항

### 8.1 현재 제한사항
- Event DTO에 타입 정보가 없어 Map<String, Object>로 수신
  - 향후 구체적인 Event DTO 타입으로 변경 권장

### 8.2 개선 필요 사항
- 이벤트 처리 실패 시 DLQ (Dead Letter Queue) 설정
- 재시도 정책 세부 설정 (최대 재시도 횟수, 백오프 전략)
- 이벤트 처리 메트릭 수집 및 대시보드 구축

---

## 9. 참고 문서

- **클래스 설계서**: `design/backend/class/calculator.md`
- **API 설계서**: `design/backend/api/calculator-service-api.yaml`
- **백엔드 개발 가이드**: `claude/dev-backend.md`
- **Kafka 공식 문서**: https://kafka.apache.org/documentation/
- **Spring Kafka 문서**: https://docs.spring.io/spring-kafka/reference/

---

## 10. 작업 완료 요약

**작업 기간**: 2026-01-05
**개발자**: 이준호 (Backend Developer)

**주요 성과**:
- ✅ RabbitMQ → Kafka 마이그레이션 완료
- ✅ 누락된 5개 클래스 개발 완료
- ✅ 컴파일 성공 (에러 없음)
- ✅ 이벤트 기반 캐시 무효화 아키텍처 구현

**다음 단계**:
1. 단위 테스트 및 통합 테스트 작성
2. Kafka 인프라 및 토픽 설정
3. 실행 및 동작 확인
4. 성능 테스트 및 모니터링 설정

---

**작성일**: 2026-01-05
**작성자**: 이준호 (Backend Developer)
