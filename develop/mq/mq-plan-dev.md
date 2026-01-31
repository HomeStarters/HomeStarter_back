# Kafka 설치 계획서 (개발환경)

**작성일**: 2025-12-29
**작성자**: 백엔드 개발자 이준호
**설치 대상**: 개발환경 (Kubernetes 클러스터)
**MQ 제품**: Apache Kafka (Bitnami Helm Chart)

---

## 목차
1. [개요](#1-개요)
2. [Kafka 사용 요구사항](#2-kafka-사용-요구사항)
3. [토픽 설계](#3-토픽-설계)
4. [Kafka 아키텍처 설계](#4-kafka-아키텍처-설계)
5. [설치 사양](#5-설치-사양)
6. [설치 절차](#6-설치-절차)
7. [검증 계획](#7-검증-계획)
8. [모니터링 및 관리](#8-모니터링-및-관리)

---

## 1. 개요

### 1.1 설치 목적
- **비동기 이벤트 전파**: Asset/Housing 서비스의 데이터 변경 이벤트를 Calculator 서비스에 전달
- **실시간 캐시 무효화**: 데이터 변경 시 관련 계산 결과 캐시를 즉시 무효화
- **느슨한 결합**: 서비스 간 직접 의존성을 제거하고 이벤트 기반 아키텍처 구현

### 1.2 설계 원칙
- **물리아키텍처 준수**: `design/backend/logical/logical-architecture.md` 6. 메시징 아키텍처와 일치
- **외부시퀀스 기반**: `design/backend/sequence/outer/데이터변경-이벤트전파.puml`에 정의된 이벤트만 구현
- **Publisher-Subscriber 패턴**: 1개의 Publisher가 여러 Subscriber에게 이벤트 브로드캐스트
- **고가용성**: 개발환경에서도 최소 2개 Broker로 운영하여 안정성 확보

### 1.3 참조 문서
- 논리아키텍처: `design/backend/logical/logical-architecture.md`
- 외부시퀀스 설계서: `design/backend/sequence/outer/데이터변경-이벤트전파.puml`
- 외부시퀀스 설계서: `design/backend/sequence/outer/자산정보-수정-및-이벤트발행.puml`
- 백킹서비스 설치방법: `claude/backing-service-method.md`

---

## 2. Kafka 사용 요구사항

### 2.1 비동기 통신 시나리오 분석

#### 시나리오 1: 자산정보 변경 → 캐시 무효화
**플로우**: `자산정보-수정-및-이벤트발행.puml`

```
사용자 → Asset Service: 자산정보 수정 (추가/수정/삭제)
Asset Service → Kafka: AssetUpdated 이벤트 발행
Kafka → Calculator Service: 이벤트 구독 및 수신
Calculator Service → Redis: 해당 사용자의 캐시 무효화
```

**이벤트 메시지 구조**:
```json
{
  "eventId": "evt-uuid",
  "eventType": "ASSET_UPDATED",
  "timestamp": "2025-01-15T10:35:00Z",
  "userId": "user-id",
  "changeType": "ITEM_ADDED|ITEM_MODIFIED|ITEM_DELETED",
  "ownerType": "SELF|SPOUSE",
  "itemType": "ASSET|LOAN|INCOME|EXPENSE",
  "itemId": "item-id",
  "previousAmount": 10000000,
  "newAmount": 15000000,
  "summary": {
    "totalNetAssets": 85000000,
    "totalMonthlyIncome": 5500000,
    "totalMonthlyExpense": 2500000,
    "monthlyAvailableFunds": 3000000
  }
}
```

#### 시나리오 2: 주택정보 변경 → 캐시 무효화
**플로우**: `데이터변경-이벤트전파.puml`

```
사용자 → Housing Service: 주택정보 수정
Housing Service → Kafka: HousingUpdated 이벤트 발행
Kafka → Calculator Service: 이벤트 구독 및 수신
Calculator Service → Redis: 해당 주택의 캐시 무효화
```

**이벤트 메시지 구조**:
```json
{
  "eventId": "evt-uuid",
  "eventType": "HOUSING_UPDATED",
  "timestamp": "2025-01-15T10:40:00Z",
  "userId": "user-id",
  "housingId": "housing-id",
  "updateType": "PRICE_CHANGED|LOCATION_CHANGED|TARGET_CHANGED",
  "timestamp": "2025-01-15T10:40:00Z"
}
```

### 2.2 성능 요구사항
- **이벤트 전파 시간**: < 1초 (초당 100건 처리 기준)
- **메시지 처리량**: 초당 100건 (개발환경 부하 기준)
- **메시지 보존 기간**: 24시간 (장애 복구 및 재처리용)
- **파티션 수**: Topic당 3개 (병렬 처리 및 고가용성)

### 2.3 데이터 일관성 요구사항
- **전달 보장 수준**: At-least-once (중복 전달 허용, Consumer 멱등성 구현)
- **순서 보장**: 동일 userId 내에서 순서 보장 (파티션 키: userId)
- **재시도 정책**: Consumer 실패 시 3회 재시도 (Exponential Backoff)
- **실패 처리**: 3회 재시도 실패 시 Dead Letter Topic 전송

---

## 3. 토픽 설계

### 3.1 토픽 목록

| 토픽 이름 | 파티션 수 | Replication Factor | 보존 기간 | 용도 |
|----------|----------|-------------------|----------|------|
| `asset.events` | 3 | 2 | 24시간 | Asset 서비스 변경 이벤트 |
| `housing.events` | 3 | 2 | 24시간 | Housing 서비스 변경 이벤트 |
| `dlq.events` | 1 | 2 | 7일 | Dead Letter Queue (실패한 메시지) |

### 3.2 토픽별 상세 설계

#### 3.2.1 asset.events
**Publisher**: Asset Service
**Subscriber**: Calculator Service

**이벤트 타입**:
- `ASSET_UPDATED`: 자산정보 변경 (추가/수정/삭제)

**파티션 전략**:
- **파티션 키**: `userId`
- **이유**: 동일 사용자의 이벤트 순서 보장

**메시지 크기**: 평균 500 bytes (JSON)

**예상 처리량**:
- 개발환경: 초당 10~50건
- 운영환경: 초당 100~500건

#### 3.2.2 housing.events
**Publisher**: Housing Service
**Subscriber**: Calculator Service

**이벤트 타입**:
- `HOUSING_UPDATED`: 주택정보 변경

**파티션 전략**:
- **파티션 키**: `userId`
- **이유**: 동일 사용자의 이벤트 순서 보장

**메시지 크기**: 평균 300 bytes (JSON)

**예상 처리량**:
- 개발환경: 초당 5~30건
- 운영환경: 초당 50~300건

#### 3.2.3 dlq.events
**Publisher**: Calculator Service (Consumer 실패 시)
**Subscriber**: 관리자 모니터링 시스템

**이벤트 타입**:
- 모든 처리 실패 이벤트

**파티션 전략**:
- **파티션 키**: 없음 (순서 불필요)
- **파티션 수**: 1개

**보존 기간**: 7일 (장기 분석 및 재처리)

### 3.3 Consumer Group 설계

| Consumer Group ID | 구독 Topic | 서비스 | 인스턴스 수 |
|------------------|-----------|--------|-----------|
| `calculator.asset.events` | `asset.events` | Calculator Service | 3개 (파티션 수와 동일) |
| `calculator.housing.events` | `housing.events` | Calculator Service | 3개 (파티션 수와 동일) |
| `monitoring.dlq.events` | `dlq.events` | Monitoring Service | 1개 |

---

## 4. Kafka 아키텍처 설계

### 4.1 클러스터 구성

```
┌─────────────────────────────────────────────────────┐
│  Kafka Cluster (개발환경)                            │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────┐  ┌──────────────┐                 │
│  │ Controller 1 │  │ Controller 2 │                 │
│  │ (KRaft)      │  │ (KRaft)      │                 │
│  └──────────────┘  └──────────────┘                 │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐                 │
│  │  Broker 1    │  │  Broker 2    │                 │
│  │              │  │              │                 │
│  │ Partition 0  │  │ Partition 1  │                 │
│  │ Partition 1  │  │ Partition 2  │                 │
│  │ (Replica)    │  │ (Replica)    │                 │
│  └──────────────┘  └──────────────┘                 │
│                                                      │
└─────────────────────────────────────────────────────┘

         ▲                           ▲
         │                           │
    ┌────┴────┐                 ┌───┴────┐
    │ Asset   │                 │Housing │
    │ Service │                 │Service │
    │(Publisher)                │(Publisher)
    └─────────┘                 └────────┘

         │                           │
         └───────────┬───────────────┘
                     │
                     ▼
              ┌─────────────┐
              │ Calculator  │
              │  Service    │
              │ (Subscriber)│
              └─────────────┘
```

### 4.2 KRaft 모드 선택 이유
- **ZooKeeper 제거**: Kafka 3.x부터 ZooKeeper 없이 운영 가능 (KRaft 모드)
- **단순화**: 관리 대상 컴포넌트 감소
- **성능 향상**: 메타데이터 관리 성능 개선
- **고가용성**: Controller 2개 구성으로 장애 대응

### 4.3 Replication 전략
- **Replication Factor**: 2 (최소 가용성 확보)
- **Min In-Sync Replicas**: 2 (데이터 손실 방지)
- **Unclean Leader Election**: Disabled (데이터 정합성 우선)

### 4.4 메시지 보존 정책
- **보존 기간**: 24시간 (일반 Topic), 7일 (DLQ Topic)
- **보존 크기**: 10GB per Topic
- **압축**: LZ4 압축 (처리량과 압축률 균형)
- **Cleanup Policy**: Delete (시간/크기 기준 자동 삭제)

---

## 5. 설치 사양

### 5.1 Helm Chart 정보
- **Chart**: `bitnami/kafka`
- **Version**: `29.3.14`
- **Kafka Version**: `3.7.x`

### 5.2 리소스 사양

#### Controller (KRaft)
| 항목 | 설정값 |
|-----|--------|
| Replica Count | 1개 (개발환경) |
| CPU Request | 500m |
| CPU Limit | 1 Core |
| Memory Request | 1Gi |
| Memory Limit | 2Gi |
| Heap Size | 1GB (-Xmx1g -Xms1g) |
| Persistent Volume | 10Gi (managed StorageClass) |

#### Broker
| 항목 | 설정값 |
|-----|--------|
| Replica Count | 2개 |
| CPU Request | 1 Core |
| CPU Limit | 2 Cores |
| Memory Request | 2Gi |
| Memory Limit | 4Gi |
| Heap Size | 2GB (-Xmx2g -Xms2g) |
| Persistent Volume | 20Gi per Broker (managed StorageClass) |

### 5.3 Kafka 성능 설정

#### Broker 설정
```properties
# 성능 최적화
num.network.threads=8
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600

# Topic 기본 설정
num.partitions=3
default.replication.factor=2
min.insync.replicas=2
log.retention.hours=24
log.segment.bytes=1073741824

# Producer 설정
compression.type=lz4
max.message.bytes=1048588

# Consumer 설정
replica.lag.time.max.ms=10000
replica.socket.timeout.ms=30000
```

### 5.4 네트워크 설정

#### 내부 통신 (ClusterIP)
- **서비스 이름**: `kafka`
- **포트**: 9092 (PLAINTEXT)
- **용도**: 서비스 간 내부 통신

#### 외부 접근 (LoadBalancer) - 개발/테스트 용도
- **서비스 이름**: `kafka-external`
- **포트**: 9095 (EXTERNAL)
- **용도**: 로컬 개발 환경에서 접근, 모니터링

### 5.5 보안 설정

#### 인증 (개발환경)
- **Protocol**: PLAINTEXT (개발환경에서 TLS/SASL 비활성화)
- **이유**: 개발 편의성 우선, 운영환경에서는 TLS 활성화

#### 네트워크 격리
- **Namespace**: 전용 Namespace 사용
- **Network Policy**: 서비스 간 통신만 허용

---

## 6. 설치 절차

### 6.1 사전 준비

#### 6.1.1 Namespace 생성
```bash
kubectl create namespace home-starter-dev
kubectl config set-context --current --namespace=home-starter-dev
```

#### 6.1.2 Helm Repository 추가
```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

#### 6.1.3 StorageClass 확인
```bash
kubectl get storageclass
```

**확인 사항**:
- 기본 StorageClass가 있는지 확인
- 없으면 `managed` 또는 `managed-premium` 사용

### 6.2 Kafka 설치

#### 6.2.1 작업 디렉토리 생성
```bash
mkdir -p ~/install/kafka && cd ~/install/kafka
```

#### 6.2.2 values.yaml 작성
```yaml
# values.yaml - Kafka 설치 설정

global:
  storageClass: "managed"

# KRaft 모드 활성화 (ZooKeeper 제거)
kraft:
  enabled: true

# 인증 설정 (개발환경: PLAINTEXT)
auth:
  clientProtocol: plaintext
  interBrokerProtocol: plaintext
  sasl:
    enabled: false
  tls:
    enabled: false

# Listener 설정
listeners:
  client:
    containerPort: 9092
    protocol: PLAINTEXT
    name: CLIENT
  controller:
    name: CONTROLLER
    containerPort: 9093
    protocol: PLAINTEXT
  interbroker:
    containerPort: 9094
    protocol: PLAINTEXT
    name: INTERNAL
  external:
    containerPort: 9095
    protocol: PLAINTEXT
    name: EXTERNAL

# 외부 접근 설정 (개발/테스트용)
externalAccess:
  enabled: true
  autoDiscovery:
    enabled: true
    image:
      repository: bitnamilegacy/kubectl
  controller:
    service:
      type: LoadBalancer
      ports:
        external: 9095
  broker:
    service:
      type: LoadBalancer
      ports:
        external: 9095

# Controller 설정 (KRaft)
controller:
  replicaCount: 1
  heapOpts: "-Xmx1g -Xms1g"
  persistence:
    enabled: true
    size: 10Gi
  automountServiceAccountToken: true
  resources:
    limits:
      memory: 2Gi
      cpu: 1
    requests:
      memory: 1Gi
      cpu: 0.5

# Broker 설정
broker:
  replicaCount: 2
  heapOpts: "-Xmx2g -Xms2g"
  persistence:
    enabled: true
    size: 20Gi
  automountServiceAccountToken: true
  resources:
    limits:
      memory: 4Gi
      cpu: 2
    requests:
      memory: 2Gi
      cpu: 1

# Topic 설정
deleteTopicEnable: true
autoCreateTopicsEnable: false
numPartitions: 3
defaultReplicationFactor: 2

# Offset 설정
offsets:
  topic:
    replicationFactor: 2

# 이미지 설정 (bitnamilegacy 사용)
image:
  registry: docker.io
  repository: bitnamilegacy/kafka

# RBAC 설정
rbac:
  create: true

# Broker 추가 설정
extraConfig: |
  min.insync.replicas=2
  compression.type=lz4
  log.retention.hours=24
  log.segment.bytes=1073741824
  num.network.threads=8
  num.io.threads=8
```

#### 6.2.3 Kafka 설치 실행
```bash
helm upgrade -i kafka -f values.yaml bitnami/kafka --version 29.3.14 \
  --namespace home-starter-dev
```

#### 6.2.4 설치 상태 확인
```bash
# Pod 상태 확인 (모든 Pod가 Running 상태가 될 때까지 대기)
watch kubectl get pods -n home-starter-dev

# Service 확인
kubectl get svc -n home-starter-dev

# PVC 확인
kubectl get pvc -n home-starter-dev
```

### 6.3 Kafka UI 설치 (관리 도구)

#### 6.3.1 kafka-ui.yaml 작성
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-ui
  namespace: home-starter-dev
  labels:
    app: kafka-ui
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka-ui
  template:
    metadata:
      labels:
        app: kafka-ui
    spec:
      containers:
        - name: kafka-ui
          image: provectuslabs/kafka-ui:latest
          env:
            - name: KAFKA_CLUSTERS_0_NAME
              value: "home-starter-kafka"
            - name: KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS
              value: "kafka:9092"
            - name: KAFKA_CLUSTERS_0_PROPERTIES_SECURITY_PROTOCOL
              value: "PLAINTEXT"
          ports:
            - containerPort: 8080
              name: http
          resources:
            limits:
              memory: "1Gi"
              cpu: "500m"
            requests:
              memory: "512Mi"
              cpu: "250m"
---
apiVersion: v1
kind: Service
metadata:
  name: kafka-ui
  namespace: home-starter-dev
  labels:
    app: kafka-ui
spec:
  type: LoadBalancer
  ports:
    - port: 8080
      targetPort: http
      protocol: TCP
      name: http
  selector:
    app: kafka-ui
```

#### 6.3.2 Kafka UI 배포
```bash
kubectl apply -f kafka-ui.yaml
```

#### 6.3.3 Kafka UI 접속 확인
```bash
# LoadBalancer IP 확인
kubectl get svc kafka-ui -n home-starter-dev

# 웹 브라우저에서 접속
# http://{KAFKA_UI_EXTERNAL_IP}:8080
```

### 6.4 Topic 생성

#### 6.4.1 Kafka Pod에 접속
```bash
kubectl exec -it kafka-broker-0 -n home-starter-dev -- /bin/bash
```

#### 6.4.2 Topic 생성 스크립트 실행
```bash
# asset.events 토픽 생성
kafka-topics.sh --bootstrap-server localhost:9092 \
  --create \
  --topic asset.events \
  --partitions 3 \
  --replication-factor 2 \
  --config min.insync.replicas=2 \
  --config retention.ms=86400000 \
  --config compression.type=lz4

# housing.events 토픽 생성
kafka-topics.sh --bootstrap-server localhost:9092 \
  --create \
  --topic housing.events \
  --partitions 3 \
  --replication-factor 2 \
  --config min.insync.replicas=2 \
  --config retention.ms=86400000 \
  --config compression.type=lz4

# dlq.events 토픽 생성 (Dead Letter Queue)
kafka-topics.sh --bootstrap-server localhost:9092 \
  --create \
  --topic dlq.events \
  --partitions 1 \
  --replication-factor 2 \
  --config min.insync.replicas=2 \
  --config retention.ms=604800000 \
  --config compression.type=lz4

# Topic 목록 확인
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Topic 상세 정보 확인
kafka-topics.sh --bootstrap-server localhost:9092 \
  --describe --topic asset.events
```

#### 6.4.3 Pod 종료
```bash
exit
```

---

## 7. 검증 계획

### 7.1 설치 검증

#### 7.1.1 Pod 상태 확인
```bash
# 모든 Pod가 Running 상태인지 확인
kubectl get pods -n home-starter-dev | grep kafka

# 예상 결과:
# kafka-controller-0           1/1     Running   0          5m
# kafka-broker-0               1/1     Running   0          5m
# kafka-broker-1               1/1     Running   0          5m
# kafka-ui-xxx                 1/1     Running   0          3m
```

#### 7.1.2 Service 확인
```bash
kubectl get svc -n home-starter-dev | grep kafka

# 예상 결과:
# kafka                        ClusterIP      10.x.x.x    <none>        9092/TCP         5m
# kafka-broker-0-external      LoadBalancer   10.x.x.x    20.x.x.x      9095:xxxxx/TCP   5m
# kafka-broker-1-external      LoadBalancer   10.x.x.x    20.x.x.x      9095:xxxxx/TCP   5m
# kafka-ui                     LoadBalancer   10.x.x.x    20.x.x.x      8080:xxxxx/TCP   3m
```

#### 7.1.3 Topic 확인
```bash
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-topics.sh --bootstrap-server localhost:9092 --list

# 예상 결과:
# asset.events
# housing.events
# dlq.events
```

### 7.2 기능 검증

#### 7.2.1 메시지 발행/구독 테스트

**Producer 테스트** (Asset Service 시뮬레이션):
```bash
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic asset.events \
  --property "parse.key=true" \
  --property "key.separator=:"

# 메시지 입력 (userId:JSON 형식)
user-001:{"eventId":"evt-test-001","eventType":"ASSET_UPDATED","timestamp":"2025-12-29T10:00:00Z","userId":"user-001","changeType":"ITEM_ADDED","ownerType":"SELF","itemType":"ASSET"}
```

**Consumer 테스트** (Calculator Service 시뮬레이션):
```bash
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic asset.events \
  --from-beginning \
  --property print.key=true \
  --property key.separator=":"

# 예상 결과: Producer에서 발행한 메시지 출력
```

#### 7.2.2 파티션 분산 확인
```bash
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --describe --topic asset.events

# 예상 결과: 3개 파티션이 2개 Broker에 분산되어 있는지 확인
# Topic: asset.events    Partition: 0    Leader: 0    Replicas: 0,1    Isr: 0,1
# Topic: asset.events    Partition: 1    Leader: 1    Replicas: 1,0    Isr: 1,0
# Topic: asset.events    Partition: 2    Leader: 0    Replicas: 0,1    Isr: 0,1
```

### 7.3 성능 검증

#### 7.3.1 처리량 테스트
```bash
# Producer 성능 테스트 (1MB 메시지 1000개)
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-producer-perf-test.sh \
  --topic asset.events \
  --num-records 1000 \
  --record-size 1024 \
  --throughput 100 \
  --producer-props bootstrap.servers=localhost:9092

# 예상 결과: 초당 100건 이상 처리
```

#### 7.3.2 지연 시간 테스트
```bash
# Consumer 성능 테스트
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-consumer-perf-test.sh \
  --topic asset.events \
  --bootstrap-server localhost:9092 \
  --messages 1000 \
  --timeout 60000

# 예상 결과: 평균 지연 시간 < 100ms
```

### 7.4 고가용성 검증

#### 7.4.1 Broker 장애 시뮬레이션
```bash
# Broker-0 Pod 삭제
kubectl delete pod kafka-broker-0 -n home-starter-dev

# 리더 재선출 확인 (약 10초 이내)
kubectl exec -it kafka-broker-1 -n home-starter-dev -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --describe --topic asset.events

# 예상 결과: Partition Leader가 Broker-1로 변경됨
```

#### 7.4.2 메시지 전달 보장 확인
```bash
# Broker-0 삭제 중에도 메시지 발행 가능한지 테스트
kubectl exec -it kafka-broker-1 -n home-starter-dev -- \
  kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic asset.events

# 메시지 입력 후 Consumer에서 수신 확인
```

---

## 8. 모니터링 및 관리

### 8.1 Kafka UI를 통한 모니터링

**접속 정보**:
```
URL: http://{KAFKA_UI_EXTERNAL_IP}:8080
```

**모니터링 항목**:
- **Broker 상태**: 활성 Broker 수, Under-Replicated Partitions
- **Topic 현황**: 메시지 수, 파티션 상태, 보존 기간
- **Consumer Group**: Lag, 처리 속도, Active Members
- **메시지 검색**: Topic별 메시지 내용 확인

### 8.2 로그 확인

#### 8.2.1 Broker 로그
```bash
# Broker 로그 확인
kubectl logs -f kafka-broker-0 -n home-starter-dev

# 최근 100줄 확인
kubectl logs --tail=100 kafka-broker-0 -n home-starter-dev
```

#### 8.2.2 Controller 로그
```bash
kubectl logs -f kafka-controller-0 -n home-starter-dev
```

### 8.3 메트릭 수집

#### 8.3.1 Kafka JMX 메트릭
```bash
# JMX Exporter를 통한 Prometheus 메트릭 노출
# values.yaml에 JMX Exporter 설정 추가 필요 (운영환경)
```

#### 8.3.2 주요 모니터링 메트릭
- **Broker 메트릭**:
  - `kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec`
  - `kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec`
  - `kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions`

- **Consumer 메트릭**:
  - `kafka.consumer:type=consumer-fetch-manager-metrics,client-id=*,attribute=records-lag-max`
  - `kafka.consumer:type=consumer-coordinator-metrics,client-id=*,attribute=commit-latency-avg`

### 8.4 장애 대응

#### 8.4.1 Broker 재시작
```bash
# Broker Pod 재시작
kubectl rollout restart statefulset kafka-broker -n home-starter-dev

# 재시작 상태 확인
kubectl rollout status statefulset kafka-broker -n home-starter-dev
```

#### 8.4.2 Topic 재설정
```bash
# Topic 파티션 수 증가 (축소는 불가능)
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --alter --topic asset.events --partitions 6

# Topic 보존 기간 변경
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-configs.sh --bootstrap-server localhost:9092 \
  --alter --entity-type topics --entity-name asset.events \
  --add-config retention.ms=172800000
```

#### 8.4.3 Consumer Group 리셋
```bash
# Consumer Group Offset 리셋 (재처리)
kubectl exec -it kafka-broker-0 -n home-starter-dev -- \
  kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group calculator.asset.events \
  --reset-offsets --to-earliest --topic asset.events --execute
```

---

## 9. 연결 정보

### 9.1 내부 서비스 연결 (Spring Boot)

**application.yml 설정 예시**:
```yaml
spring:
  kafka:
    enabled: true
    bootstrap-servers: kafka:9092

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

### 9.2 외부 접근 (로컬 개발)

**Broker 외부 IP 확인**:
```bash
kubectl get svc -n home-starter-dev | grep broker-.*-external

# 예상 결과:
# kafka-broker-0-external   LoadBalancer   10.x.x.x   20.249.182.13   9095:xxxxx/TCP
# kafka-broker-1-external   LoadBalancer   10.x.x.x   4.217.131.59    9095:xxxxx/TCP
```

**로컬 application.yml**:
```yaml
spring:
  kafka:
    bootstrap-servers: 20.249.182.13:9095,4.217.131.59:9095
```

---

## 10. 설치 체크리스트

### 10.1 사전 준비
- [ ] Namespace 생성 완료
- [ ] Helm Repository 추가 완료
- [ ] StorageClass 확인 완료
- [ ] values.yaml 작성 완료

### 10.2 설치
- [ ] Kafka Helm Chart 설치 완료
- [ ] Kafka UI 설치 완료
- [ ] Topic 생성 완료 (asset.events, housing.events, dlq.events)

### 10.3 검증
- [ ] Pod 상태 확인 (모든 Pod Running)
- [ ] Service 확인 (ClusterIP, LoadBalancer)
- [ ] Topic 확인 (3개 Topic 존재)
- [ ] 메시지 발행/구독 테스트 완료
- [ ] 파티션 분산 확인 완료
- [ ] 성능 테스트 완료 (처리량, 지연 시간)
- [ ] 고가용성 테스트 완료 (Broker 장애 시뮬레이션)

### 10.4 모니터링
- [ ] Kafka UI 접속 확인
- [ ] Broker 로그 확인
- [ ] Topic 메트릭 확인

---

## 11. 참고 자료

### 11.1 내부 문서
- 논리아키텍처: `design/backend/logical/logical-architecture.md`
- 외부시퀀스: `design/backend/sequence/outer/데이터변경-이벤트전파.puml`
- 백킹서비스 설치방법: `claude/backing-service-method.md`

### 11.2 외부 문서
- Kafka 공식 문서: https://kafka.apache.org/documentation/
- Bitnami Kafka Helm Chart: https://github.com/bitnami/charts/tree/main/bitnami/kafka
- Kafka UI: https://github.com/provectus/kafka-ui

---

**작성일**: 2025-12-29
**검토자**: 아키텍트 홍길동, DevOps 송주영
**승인자**: Product Owner 김민준
**문서 버전**: 1.0
