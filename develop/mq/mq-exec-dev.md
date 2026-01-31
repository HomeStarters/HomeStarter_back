# Kafka 설치 결과서 (개발환경)

**작성일**: 2025-12-29
**작성자**: DevOps 송주영
**설치 환경**: minikube (Kubernetes v1.34.0)
**설치 제품**: Apache Kafka 4.0.0 (Strimzi Operator)
**Namespace**: homestarter-ns

---

## 1. 설치 개요

### 1.1 설치 정보
- **Kafka 버전**: 4.0.0
- **배포 방식**: Strimzi Kafka Operator (KRaft 모드)
- **ZooKeeper**: 사용 안 함 (KRaft 모드로 대체)
- **Broker 수**: 1개 (개발환경 최적화)
- **Controller 수**: 1개 (Broker와 통합)

### 1.2 설치 경로
- **설치 디렉토리**: `/Users/daewoong/home_starter/develop/mq/install`
- **설정 파일**:
  - `kafka-kraft-cluster.yaml`: Kafka 클러스터 및 NodePool 정의
  - `kafka-topics.yaml`: Topic 정의

### 1.3 설치 변경 사항
**원 계획 대비 변경사항**:
- **Helm Chart**: Bitnami → Strimzi Operator로 변경
  - **사유**: Bitnami 이미지 무료 접근 제한 (2025년 8월부터)
- **ZooKeeper**: 사용 → 미사용으로 변경
  - **사유**: Strimzi 0.46.0부터 KRaft 모드만 지원
- **Kafka 버전**: 3.7.x → 4.0.0으로 변경
  - **사유**: Strimzi에서 지원하는 최소 버전
- **Broker 수**: 2개 → 1개로 변경
  - **사유**: minikube 리소스 제약 및 개발환경 특성
- **Replication Factor**: 2 → 1로 변경
  - **사유**: 단일 Broker 구성

---

## 2. 설치 결과

### 2.1 설치된 리소스

#### 2.1.1 Operator
```
NAME                                       READY   STATUS    RESTARTS   AGE
strimzi-cluster-operator-8457d7566-6twzr   1/1     Running   2          5m
```

#### 2.1.2 Kafka 클러스터
```
NAME                                 READY   STATUS    RESTARTS   AGE
kafka-kafka-pool-0                   1/1     Running   0          2m
kafka-entity-operator-57fd8d74b5-m97nk  2/2     Running   0          1m
```

#### 2.1.3 서비스
```
NAME                    TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)
kafka-kafka-bootstrap   ClusterIP   10.98.130.22   <none>        9091/TCP,9092/TCP,9093/TCP
kafka-kafka-brokers     ClusterIP   None           <none>        9090/TCP,9091/TCP,8443/TCP,9092/TCP,9093/TCP
```

#### 2.1.4 Persistent Volumes
```
NAME                        STATUS   VOLUME                                     CAPACITY
data-0-kafka-kafka-pool-0   Bound    pvc-dfd9d9c2-6e45-4179-9c20-c7796a54119e   10Gi
```

#### 2.1.5 Topics
```
NAME             CLUSTER   PARTITIONS   REPLICATION FACTOR   READY
asset.events     kafka     3            1                    True
housing.events   kafka     3            1                    True
dlq.events       kafka     1            1                    True
```

### 2.2 리소스 사양

#### Kafka Broker (kafka-kafka-pool-0)
- **Replicas**: 1
- **역할**: Controller + Broker (통합)
- **CPU**: Request 500m, Limit 1 Core
- **Memory**: Request 1Gi, Limit 2Gi
- **Storage**: 10Gi (Persistent Volume)

#### Entity Operator
- **Topic Operator**: CPU 100m/200m, Memory 128Mi/256Mi
- **User Operator**: CPU 100m/200m, Memory 128Mi/256Mi

### 2.3 Topic 설정

| Topic 이름 | 파티션 수 | Replication Factor | 보존 기간 | 압축 타입 |
|-----------|----------|-------------------|----------|----------|
| asset.events | 3 | 1 | 24시간 | LZ4 |
| housing.events | 3 | 1 | 24시간 | LZ4 |
| dlq.events | 1 | 1 | 7일 | LZ4 |

---

## 3. 접속 정보

### 3.1 내부 서비스 연결 (Spring Boot)

**Bootstrap Server**:
```
kafka-kafka-bootstrap.homestarter-ns.svc.cluster.local:9092
```

**Spring Boot application.yml 설정 예시**:
```yaml
spring:
  kafka:
    enabled: true
    bootstrap-servers: kafka-kafka-bootstrap.homestarter-ns.svc.cluster.local:9092

    # Producer 설정
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      compression-type: lz4
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 1

    # Consumer 설정
    consumer:
      group-id: calculator.asset.events
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "*"
        isolation.level: read_committed

    # Listener 설정
    listener:
      ack-mode: manual
      concurrency: 3
```

### 3.2 외부 접근 (로컬 개발)

**Port-Forward 설정**:
```bash
# Kafka Bootstrap 서버 포트 포워딩
kubectl port-forward -n homestarter-ns svc/kafka-kafka-bootstrap 9092:9092

# 로컬에서 접속
# bootstrap-servers: localhost:9092
```

---

## 4. 검증 결과

### 4.1 Pod 상태 확인
✅ **모든 Pod가 Running 상태**
- Strimzi Operator: Running
- Kafka Broker (kafka-kafka-pool-0): Running
- Entity Operator: Running

### 4.2 Service 확인
✅ **Kafka 서비스 정상 동작**
- kafka-kafka-bootstrap (ClusterIP): 생성됨
- kafka-kafka-brokers (Headless): 생성됨

### 4.3 Topic 확인
✅ **모든 Topic이 Ready 상태**
- asset.events: 3 partitions, Ready
- housing.events: 3 partitions, Ready
- dlq.events: 1 partition, Ready

### 4.4 Persistent Volume 확인
✅ **PVC가 정상적으로 Bound**
- data-0-kafka-kafka-pool-0: 10Gi, Bound

---

## 5. 운영 가이드

### 5.1 상태 확인 명령어

```bash
# Pod 상태 확인
kubectl get pods -n homestarter-ns

# Kafka 클러스터 상태 확인
kubectl get kafka -n homestarter-ns

# Topic 목록 확인
kubectl get kafkatopic -n homestarter-ns

# Topic 상세 정보 확인
kubectl describe kafkatopic asset.events -n homestarter-ns

# 로그 확인
kubectl logs -n homestarter-ns kafka-kafka-pool-0
```

### 5.2 메시지 발행/구독 테스트

**Producer 테스트 (Kafka Client Pod 생성)**:
```bash
# Kafka Client Pod 생성
kubectl run kafka-producer-test --restart='Never' \
  --image quay.io/strimzi/kafka:latest-kafka-4.0.0 \
  --namespace homestarter-ns \
  --command -- sleep infinity

# Pod에 접속
kubectl exec -it kafka-producer-test -n homestarter-ns -- /bin/bash

# Producer 실행
bin/kafka-console-producer.sh \
  --bootstrap-server kafka-kafka-bootstrap:9092 \
  --topic asset.events \
  --property "parse.key=true" \
  --property "key.separator=:"

# 메시지 입력 예시
user-001:{"eventId":"evt-test-001","eventType":"ASSET_UPDATED","timestamp":"2025-12-29T10:00:00Z"}
```

**Consumer 테스트**:
```bash
# 별도 터미널에서 Consumer Pod 생성
kubectl run kafka-consumer-test --restart='Never' \
  --image quay.io/strimzi/kafka:latest-kafka-4.0.0 \
  --namespace homestarter-ns \
  --command -- sleep infinity

# Consumer 실행
kubectl exec -it kafka-consumer-test -n homestarter-ns -- \
  bin/kafka-console-consumer.sh \
  --bootstrap-server kafka-kafka-bootstrap:9092 \
  --topic asset.events \
  --from-beginning \
  --property print.key=true \
  --property key.separator=":"
```

### 5.3 Topic 관리

**Topic 추가**:
```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: new-topic
  namespace: homestarter-ns
  labels:
    strimzi.io/cluster: kafka
spec:
  partitions: 3
  replicas: 1
  config:
    retention.ms: 86400000
    compression.type: lz4
```

**Topic 수정**:
```bash
# KafkaTopic 리소스 편집
kubectl edit kafkatopic asset.events -n homestarter-ns

# 파티션 수 증가 (축소는 불가능)
```

**Topic 삭제**:
```bash
kubectl delete kafkatopic asset.events -n homestarter-ns
```

### 5.4 장애 대응

**Pod 재시작**:
```bash
# Broker Pod 재시작
kubectl delete pod kafka-kafka-pool-0 -n homestarter-ns

# Operator 재시작
kubectl rollout restart deployment strimzi-cluster-operator -n homestarter-ns
```

**로그 확인**:
```bash
# Kafka Broker 로그
kubectl logs -n homestarter-ns kafka-kafka-pool-0 -f

# Operator 로그
kubectl logs -n homestarter-ns deployment/strimzi-cluster-operator -f

# Entity Operator 로그
kubectl logs -n homestarter-ns deployment/kafka-entity-operator -c topic-operator
```

---

## 6. 주의사항

### 6.1 리소스 제약
- minikube 환경에서는 단일 Broker로 운영
- Replication Factor가 1이므로 데이터 손실 위험 존재
- 운영환경에서는 최소 3개 Broker, Replication Factor 2 이상 권장

### 6.2 데이터 영속성
- Persistent Volume을 사용하므로 Pod 재시작 시에도 데이터 보존
- PVC 삭제 시 데이터 영구 손실 (deleteClaim: false 설정됨)

### 6.3 성능 고려사항
- 개발환경 최적화된 리소스 설정
- 운영환경에서는 리소스 증설 필요
- 처리량 테스트 후 파티션 수 조정 권장

---

## 7. 문제 해결

### 7.1 Bitnami 이미지 사용 불가
**문제**: Bitnami 이미지 무료 접근 제한
**해결**: Strimzi Operator로 변경 (오픈소스, 무료)

### 7.2 ZooKeeper 미지원
**문제**: Strimzi 0.46.0부터 ZooKeeper 미지원
**해결**: KRaft 모드로 전환 (ZooKeeper 불필요)

### 7.3 Kafka 버전 호환성
**문제**: Kafka 3.7.0 미지원
**해결**: Kafka 4.0.0 사용 (Strimzi 지원 버전)

---

## 8. 다음 단계

### 8.1 애플리케이션 연동
1. Asset Service: Producer 구현
2. Housing Service: Producer 구현
3. Calculator Service: Consumer 구현

### 8.2 모니터링 설정
1. Kafka UI 설치 (선택사항)
2. Prometheus/Grafana 연동 (운영환경)

### 8.3 운영환경 마이그레이션
1. Broker 수 증설 (3개 이상)
2. Replication Factor 증설 (2 이상)
3. 리소스 증설 (CPU, Memory, Storage)
4. TLS 활성화
5. 인증/인가 설정

---

## 9. 참고 자료

### 9.1 내부 문서
- 설치 계획서: `develop/mq/mq-plan-dev.md`
- 논리아키텍처: `design/backend/logical/logical-architecture.md`
- 외부시퀀스: `design/backend/sequence/outer/데이터변경-이벤트전파.puml`

### 9.2 외부 문서
- Strimzi 공식 문서: https://strimzi.io/documentation/
- Kafka 공식 문서: https://kafka.apache.org/documentation/
- Strimzi GitHub: https://github.com/strimzi/strimzi-kafka-operator

---

**작성일**: 2025-12-29
**검토자**: 백엔드 개발자 이준호
**승인자**: Product Owner 김민준
**문서 버전**: 1.0
