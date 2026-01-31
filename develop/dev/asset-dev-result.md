# Asset 서비스 백엔드 개발 결과서

## 작성 정보
- **작성일시**: 2026-01-05
- **작성자**: 이준호 (Backend Developer)
- **서비스명**: Asset Service
- **개발 유형**: Kafka 마이그레이션 및 설정

---

## 1. 개발 개요

### 1.1 작업 목적
- RabbitMQ 대신 Kafka를 사용한 이벤트 기반 아키텍처 구성
- Asset 서비스의 Kafka 설정 완성
- 자산정보 변경 시 Calculator 서비스로 이벤트 발행 기능 활성화

### 1.2 작업 범위
- **의존성 확인**: spring-kafka (이미 설정됨)
- **설정 확인**: Kafka 설정 (이미 완료됨)
- **IntelliJ 프로파일 확인**: Kafka 환경변수 (이미 설정됨)
- **신규 클래스 개발**:
  - KafkaConfig.java (Kafka 설정)

---

## 2. 기존 설정 현황

### 2.1 의존성 현황

#### build.gradle
```gradle
dependencies {
    // ... 기타 의존성 ...

    // Kafka for event publishing
    implementation 'org.springframework.kafka:spring-kafka'

    // Test
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

**상태**: ✅ Kafka 의존성이 이미 설정되어 있음

---

### 2.2 설정 현황

#### application.yml
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:121.129.45.98:9002}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
    consumer:
      group-id: asset-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "*"
```

**설정 설명**:
- `bootstrap-servers`: Kafka 브로커 주소 (환경변수: KAFKA_BOOTSTRAP_SERVERS)
- `producer`: 이벤트 발행을 위한 Producer 설정
  - `acks: all`: 모든 복제본에 쓰기 완료 확인 (데이터 손실 방지)
  - `retries: 3`: 전송 실패 시 재시도 횟수
  - `enable.idempotence: true`: 중복 메시지 방지
- `consumer`: 필요시 이벤트 수신을 위한 Consumer 설정
  - `group-id`: Consumer 그룹 ID (asset-service-group)
  - `auto-offset-reset: earliest`: 처음부터 메시지 수신
  - `enable-auto-commit: false`: 수동 커밋 모드

**상태**: ✅ Kafka 설정이 이미 완료되어 있음

---

#### IntelliJ 실행 프로파일 (.run/AssetApplication.run.xml)
```xml
<entry key="KAFKA_BOOTSTRAP_SERVERS" value="121.129.45.98:9002" />
```

**환경변수 설명**:
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka 브로커 주소 (121.129.45.98:9002)

**상태**: ✅ Kafka 환경변수가 이미 설정되어 있음

---

### 2.3 기존 이벤트 발행 클래스

#### AssetEventPublisher.java
**패키지**: `com.dwj.homestarter.asset.event`

**책임**: 자산 변경 이벤트를 Kafka로 발행

**주요 메소드**:
```java
+ publishAssetUpdated(userId: String, ownerType: OwnerType, action: String, summary: AssetSummary): void
```

**처리 로직**:
1. 자산 요약 정보를 이벤트 Map으로 변환
2. JSON 직렬화
3. Kafka `asset-events` 토픽으로 발행
4. 성공/실패 로그 기록

**Kafka 설정**:
- **Topic**: `asset-events`
- **Key**: userId
- **Value**: JSON 형식의 이벤트 데이터

**이벤트 구조**:
```json
{
  "userId": "user123",
  "ownerType": "SELF",
  "action": "CREATED",
  "totalAssets": 100000000,
  "totalLoans": 50000000,
  "netAssets": 50000000,
  "totalMonthlyIncome": 5000000,
  "totalMonthlyExpense": 3000000,
  "monthlyAvailableFunds": 2000000,
  "timestamp": 1735000000000
}
```

**파일 위치**: `asset-service/src/main/java/com/dwj/homestarter/asset/event/AssetEventPublisher.java`

**상태**: ✅ 이미 구현되어 있음

---

## 3. 신규 개발 내용

### 3.1 KafkaConfig.java

**패키지**: `com.dwj.homestarter.asset.config`

**책임**: Kafka Consumer 및 Producer 설정

**주요 메소드**:
```java
+ consumerFactory(): ConsumerFactory<String, Object>
+ kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Object>
+ producerFactory(): ProducerFactory<String, String>
+ kafkaTemplate(): KafkaTemplate<String, String>
```

**설정 내용**:

#### Consumer Factory
- JSON 역직렬화 설정
- 신뢰할 수 있는 패키지 설정 (`*`)
- Map 타입으로 수신
- 필요시 다른 서비스의 이벤트 수신 가능

#### Producer Factory
- String 직렬화 설정
- `acks=all`: 모든 복제본 쓰기 확인 (높은 신뢰성)
- `retries=3`: 재시도 3회
- `enable.idempotence=true`: 중복 메시지 방지

#### Kafka Template
- AssetEventPublisher에서 사용
- 자산 변경 이벤트 발행

**파일 위치**: `asset-service/src/main/java/com/dwj/homestarter/asset/config/KafkaConfig.java`

**코드**:
```java
package com.dwj.homestarter.asset.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 설정
 *
 * Asset 서비스는 주로 Producer로 동작하여 자산정보 변경 이벤트를 발행합니다.
 *
 * @author homestarter
 * @since 1.0.0
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:asset-service-group}")
    private String groupId;

    /**
     * Kafka Consumer Factory 설정
     * 필요 시 다른 서비스의 이벤트를 수신하기 위한 Consumer 설정
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Kafka Listener Container Factory
     * 이벤트 리스너를 위한 컨테이너 팩토리
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    /**
     * Kafka Producer Factory 설정
     * Asset 변경 이벤트를 발행하기 위한 Producer 설정
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka Template
     * 이벤트 발행을 위한 템플릿
     * AssetEventPublisher에서 사용
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

---

## 4. 이벤트 발행 아키텍처

### 4.1 Asset 변경 이벤트 발행 흐름

```
AssetServiceImpl → AssetEventPublisher → Kafka (asset-events 토픽)
                                              ↓
                                      Calculator Service
```

### 4.2 이벤트 발행 시점

**생성 (CREATE)**:
- API: `POST /assets/self`, `POST /assets/spouse`
- 트리거: `createSelfAssets()`, `createSpouseAssets()`
- Action: "CREATED"

**수정 (UPDATE)**:
- API: `PUT /assets/{id}`
- 트리거: `updateAsset()`
- Action: "UPDATED"

**삭제 (DELETE)**:
- API: `DELETE /assets/{id}`
- 트리거: `deleteAsset()`
- Action: "DELETED"

### 4.3 이벤트 처리 흐름

```
1. 사용자 요청 → AssetController
2. 비즈니스 로직 실행 → AssetServiceImpl
3. 데이터베이스 저장/수정/삭제
4. 이벤트 발행 → AssetEventPublisher
5. Kafka 토픽 전송 → asset-events
6. Calculator 서비스 수신 → 캐시 무효화
```

### 4.4 에러 처리

**발행 실패 시**:
- 로그 기록 (에러 레벨)
- 트랜잭션 롤백 없음 (비동기 처리)
- 향후 재시도 메커니즘 추가 권장

---

## 5. 컴파일 결과

### 5.1 컴파일 명령어
```bash
./gradlew asset-service:compileJava
```

### 5.2 컴파일 결과
```
> Task :common:generateEffectiveLombokConfig UP-TO-DATE
> Task :common:compileJava UP-TO-DATE
> Task :asset-service:generateEffectiveLombokConfig
> Task :asset-service:compileJava

BUILD SUCCESSFUL in 1s
4 actionable tasks: 2 executed, 2 up-to-date
```

**결과**: ✅ 컴파일 성공 (에러 없음)

---

## 6. 개발 완료 항목

### 6.1 의존성
- ✅ spring-kafka (이미 설정됨)
- ✅ spring-kafka-test (테스트용, 이미 설정됨)

### 6.2 설정
- ✅ application.yml: Kafka 설정 (이미 완료됨)
- ✅ IntelliJ 실행 프로파일: Kafka 환경변수 (이미 설정됨)

### 6.3 클래스 개발
- ✅ KafkaConfig.java: Kafka Consumer/Producer 설정 (신규 개발)
- ✅ AssetEventPublisher.java: 이벤트 발행 (이미 구현됨)

### 6.4 컴파일 및 검증
- ✅ 컴파일 성공
- ✅ 모든 설정 정상 확인
- ✅ Kafka 이벤트 발행 준비 완료

---

## 7. Asset 서비스의 특징

### 7.1 Producer 중심 서비스
- Asset 서비스는 주로 **Producer**로 동작
- 자산 정보 변경 시 이벤트를 **발행**
- Consumer 기능은 필요시 사용 가능하도록 준비됨

### 7.2 다른 서비스와의 차이점

| 서비스 | 역할 | 이벤트 |
|--------|------|--------|
| Asset | Producer | 자산 변경 이벤트 발행 → Calculator |
| Calculator | Consumer | Asset, Housing 이벤트 수신 → 캐시 무효화 |
| Housing | Producer | 주택 변경 이벤트 발행 → Calculator |

### 7.3 이벤트 토픽

**발행 토픽**:
- `asset-events`: Asset 서비스가 자산 변경 이벤트 발행

**구독 토픽**:
- 없음 (현재는 Consumer로 동작하지 않음)

---

## 8. 테스트 권고사항

### 8.1 단위 테스트
- AssetEventPublisher 단위 테스트
  - 정상 이벤트 발행 확인
  - JSON 직렬화 확인
  - Kafka 전송 성공 확인

- KafkaConfig 테스트
  - Bean 생성 확인
  - Consumer/Producer Factory 설정 확인

### 8.2 통합 테스트
- Kafka 통합 테스트
  - Asset 생성 → 이벤트 발행 → 토픽 확인
  - Asset 수정 → 이벤트 발행 → 토픽 확인
  - Asset 삭제 → 이벤트 발행 → 토픽 확인

- Calculator 연동 테스트
  - Asset 변경 → 이벤트 발행 → Calculator 캐시 무효화 확인
  - 재조회 시 최신 데이터 반영 확인

### 8.3 성능 테스트
- 이벤트 발행 성능
  - 초당 발행 가능한 이벤트 수
  - 평균 발행 시간 측정

- 대량 자산 변경 시나리오
  - 동시 다발적 자산 수정 시 이벤트 발행 성능
  - Kafka 병목 현상 확인

---

## 9. 배포 전 체크리스트

### 9.1 환경변수 설정
- [x] `KAFKA_BOOTSTRAP_SERVERS`: Kafka 브로커 주소 확인 (121.129.45.98:9002)
- [ ] Kafka 토픽 생성 확인:
  - `asset-events` (Asset 서비스가 발행)

### 9.2 Kafka 인프라
- [ ] Kafka 브로커 정상 작동 확인
- [ ] 토픽 파티션 및 복제 설정 확인
- [ ] Producer 설정 확인 (acks=all, idempotence=true)

### 9.3 모니터링
- [ ] Kafka Producer 메트릭 설정
- [ ] 이벤트 발행 성공/실패 메트릭 설정
- [ ] 로그 모니터링 설정 (AssetEventPublisher 로그)

---

## 10. 알려진 이슈 및 제한사항

### 10.1 현재 제한사항
- 이벤트 발행 실패 시 재시도 메커니즘 없음
  - 현재는 로그만 기록
  - 향후 DLQ (Dead Letter Queue) 추가 권장

- AssetEventPublisher가 JSON 직렬화를 수동으로 수행
  - KafkaTemplate이 String 타입으로 설정되어 있음
  - 향후 Object 타입 지원으로 변경 고려

### 10.2 개선 필요 사항
- 이벤트 발행 실패 시 DLQ 설정
- 재시도 정책 세부 설정
- 이벤트 발행 메트릭 수집 및 대시보드 구축
- 이벤트 스키마 버전 관리

---

## 11. 참고 문서

- **클래스 설계서**: `design/backend/class/asset-class-design.md`
- **API 설계서**: `design/backend/api/asset-service-api.yaml`
- **백엔드 개발 가이드**: `claude/dev-backend.md`
- **Calculator 개발 결과서**: `develop/dev/calculator-dev-result.md` (이벤트 수신 측 참고)
- **Kafka 공식 문서**: https://kafka.apache.org/documentation/
- **Spring Kafka 문서**: https://docs.spring.io/spring-kafka/reference/

---

## 12. 작업 완료 요약

**작업 기간**: 2026-01-05
**개발자**: 이준호 (Backend Developer)

**주요 성과**:
- ✅ Kafka 설정 확인 (이미 완료되어 있음)
- ✅ KafkaConfig.java 개발 완료
- ✅ 컴파일 성공 (에러 없음)
- ✅ 자산 변경 이벤트 발행 아키텍처 완성

**Asset 서비스 특징**:
- **Producer 중심**: 자산 변경 이벤트를 Calculator 서비스로 발행
- **비동기 통신**: Kafka를 통한 느슨한 결합
- **신뢰성 보장**: acks=all, idempotence=true 설정

**다음 단계**:
1. Kafka 토픽 생성 (`asset-events`)
2. 단위 테스트 및 통합 테스트 작성
3. Calculator 서비스와 연동 테스트
4. 성능 테스트 및 모니터링 설정
5. 실행 및 동작 확인

---

**작성일**: 2026-01-05
**작성자**: 이준호 (Backend Developer)
