# Kafka 설치 계획서 (운영환경)

**작성일**: 2025-12-29
**작성자**: 백엔드 개발자 이준호
**설치 대상**: 운영환경 (Kubernetes 클러스터)
**MQ 제품**: Apache Kafka (Bitnami Helm Chart)

---

## 목차
1. [개요](#1-개요)
2. [운영환경 요구사항](#2-운영환경-요구사항)
3. [토픽 설계](#3-토픽-설계)
4. [Kafka 아키텍처 설계](#4-kafka-아키텍처-설계)
5. [설치 사양](#5-설치-사양)
6. [설치 절차](#6-설치-절차)
7. [검증 계획](#7-검증-계획)
8. [모니터링 및 관리](#8-모니터링-및-관리)
9. [보안 설정](#9-보안-설정)
10. [백업 및 복구](#10-백업-및-복구)

---

## 1. 개요

### 1.1 설치 목적
- **운영 품질 보장**: 고가용성, 내구성, 보안성을 갖춘 Kafka 클러스터 구축
- **비즈니스 연속성**: 서비스 무중단 운영 및 장애 복구 체계 확립
- **확장 가능성**: 트래픽 증가에 대응 가능한 스케일 아웃 아키텍처

### 1.2 개발환경과의 차이점

| 항목 | 개발환경 | 운영환경 |
|-----|----------|----------|
| Controller | 1개 | 3개 (Quorum) |
| Broker | 2개 | 3개 (고가용성) |
| Replication Factor | 2 | 3 |
| Min In-Sync Replicas | 2 | 2 |
| 보안 (TLS/SASL) | 비활성화 | 활성화 |
| 외부 접근 | LoadBalancer | 내부만 접근 (ClusterIP) |
| 리소스 할당 | 최소 사양 | 프로덕션 사양 |
| 모니터링 | Kafka UI만 | Prometheus + Grafana + Kafka UI |
| 백업 | 없음 | 주기적 백업 |

### 1.3 참조 문서
- 논리아키텍처: `design/backend/logical/logical-architecture.md`
- 외부시퀀스 설계서: `design/backend/sequence/outer/데이터변경-이벤트전파.puml`
- 백킹서비스 설치방법: `claude/backing-service-method.md`
- 개발환경 계획서: `develop/mq/mq-plan-dev.md`

---

## 2. 운영환경 요구사항

### 2.1 성능 요구사항
- **최대 처리량**: 초당 500건 (피크 타임 기준)
- **평균 지연 시간**: < 50ms (P99: < 200ms)
- **메시지 보존 기간**: 7일 (규정 준수 및 장애 복구)
- **가용성 목표**: 99.9% (월 최대 43분 다운타임)

### 2.2 보안 요구사항
- **전송 암호화**: TLS 1.2 이상 적용
- **인증**: SASL/PLAIN 기반 사용자 인증
- **인가**: ACL을 통한 Topic별 접근 제어
- **네트워크 격리**: Private Subnet, Network Policy 적용

### 2.3 데이터 보호 요구사항
- **복제 보장**: 최소 3개 복제본 (Replication Factor 3)
- **동기화 보장**: 최소 2개 In-Sync Replica 확보
- **백업 주기**: 매일 자동 백업 (보존 기간 30일)
- **재해 복구**: RTO 1시간, RPO 15분

### 2.4 모니터링 요구사항
- **실시간 메트릭**: Prometheus를 통한 메트릭 수집
- **시각화**: Grafana 대시보드
- **알람**: 장애 발생 시 즉시 알림 (Slack, Email)
- **로그 수집**: Centralized Logging (ELK Stack)

---

## 3. 토픽 설계

### 3.1 토픽 목록

| 토픽 이름 | 파티션 수 | Replication Factor | 보존 기간 | 용도 |
|----------|----------|-------------------|----------|------|
| `asset.events` | 6 | 3 | 7일 | Asset 서비스 변경 이벤트 |
| `housing.events` | 6 | 3 | 7일 | Housing 서비스 변경 이벤트 |
| `dlq.events` | 3 | 3 | 30일 | Dead Letter Queue |

### 3.2 파티션 수 산정 근거

**계산 공식**:
```
파티션 수 = max(
  처리량 기준 파티션 수,
  병렬 처리 기준 파티션 수
)
```

**처리량 기준**:
```
예상 최대 처리량: 500 msg/s
단일 파티션 처리량: 100 msg/s
최소 파티션 수 = 500 / 100 = 5
안전 계수 20% 적용 = 5 * 1.2 = 6
```

**병렬 처리 기준**:
```
Calculator Service Pod 수: 3개
Consumer Concurrency: 2 (per Pod)
최대 Consumer 수: 3 * 2 = 6
파티션 수 = 6 (각 Consumer가 1개 파티션 담당)
```

**결정**: 6개 파티션

### 3.3 Consumer Group 설계

| Consumer Group ID | 구독 Topic | 서비스 | Pod 수 | Concurrency |
|------------------|-----------|--------|--------|-------------|
| `calculator.asset.events` | `asset.events` | Calculator Service | 3 | 2 |
| `calculator.housing.events` | `housing.events` | Calculator Service | 3 | 2 |
| `monitoring.dlq.events` | `dlq.events` | Monitoring Service | 1 | 3 |

---

## 4. Kafka 아키텍처 설계

### 4.1 클러스터 구성

```
┌──────────────────────────────────────────────────────────┐
│  Kafka Cluster (운영환경)                                 │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Controller 1 │  │ Controller 2 │  │ Controller 3 │   │
│  │ (KRaft)      │  │ (KRaft)      │  │ (KRaft)      │   │
│  │ Leader       │  │ Follower     │  │ Follower     │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Broker 1    │  │  Broker 2    │  │  Broker 3    │   │
│  │              │  │              │  │              │   │
│  │ Partition 0  │  │ Partition 1  │  │ Partition 2  │   │
│  │ Partition 1  │  │ Partition 2  │  │ Partition 0  │   │
│  │ Partition 2  │  │ Partition 0  │  │ Partition 1  │   │
│  │ (Leader)     │  │ (Leader)     │  │ (Leader)     │   │
│  │ (Replica)    │  │ (Replica)    │  │ (Replica)    │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                           │
└──────────────────────────────────────────────────────────┘

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

### 4.2 고가용성 전략

#### 4.2.1 Controller Quorum (3개)
- **Leader Election**: Raft 알고리즘 기반 리더 선출
- **Failover Time**: < 10초
- **최소 Quorum**: 2개 (과반수)

#### 4.2.2 Broker Replication (RF=3)
- **Leader Replica**: 1개 (읽기/쓰기)
- **Follower Replica**: 2개 (읽기만 가능, 동기화)
- **Min ISR**: 2개 (최소 동기화 복제본)
- **Unclean Leader Election**: Disabled (데이터 손실 방지)

#### 4.2.3 Pod Anti-Affinity
- **전략**: Soft Anti-Affinity
- **목적**: Broker Pod를 서로 다른 노드에 분산 배치
- **효과**: 노드 장애 시 서비스 연속성 보장

### 4.3 메시지 보존 정책

| 항목 | 설정값 | 근거 |
|-----|--------|------|
| 보존 기간 (일반 Topic) | 7일 | 장애 복구 및 재처리 여유 |
| 보존 기간 (DLQ Topic) | 30일 | 분석 및 재발 방지 |
| 최대 보존 크기 | 50GB per Topic | 비용 최적화 |
| Segment 크기 | 1GB | 효율적인 로그 관리 |
| 압축 방식 | LZ4 | 처리량과 압축률 균형 |
| Cleanup Policy | Delete | 시간/크기 기준 자동 삭제 |

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
| Replica Count | 3개 (Quorum) |
| CPU Request | 1 Core |
| CPU Limit | 2 Cores |
| Memory Request | 2Gi |
| Memory Limit | 4Gi |
| Heap Size | 2GB (-Xmx2g -Xms2g) |
| Persistent Volume | 20Gi (managed-premium StorageClass) |
| Anti-Affinity | Soft (다른 노드에 분산) |

#### Broker
| 항목 | 설정값 |
|-----|--------|
| Replica Count | 3개 |
| CPU Request | 2 Cores |
| CPU Limit | 4 Cores |
| Memory Request | 4Gi |
| Memory Limit | 8Gi |
| Heap Size | 4GB (-Xmx4g -Xms4g) |
| Persistent Volume | 100Gi per Broker (managed-premium) |
| Anti-Affinity | Soft (다른 노드에 분산) |

### 5.3 Kafka 성능 설정

#### Broker 설정
```properties
# 성능 최적화
num.network.threads=16
num.io.threads=16
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
num.replica.fetchers=4

# Topic 기본 설정
num.partitions=6
default.replication.factor=3
min.insync.replicas=2
unclean.leader.election.enable=false
log.retention.hours=168
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000

# Producer 설정
compression.type=lz4
max.message.bytes=1048588

# Consumer 설정
replica.lag.time.max.ms=10000
replica.socket.timeout.ms=30000

# 보안 설정
auto.create.topics.enable=false
delete.topic.enable=true
```

### 5.4 네트워크 설정

#### 내부 통신 (ClusterIP Only)
- **서비스 이름**: `kafka`
- **포트**: 9092 (TLS)
- **용도**: 서비스 간 내부 통신 (암호화)
- **외부 접근**: 차단 (운영 보안)

#### Network Policy
```yaml
# Kafka에 접근 가능한 Pod 제한
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kafka-network-policy
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: kafka
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: asset-service
        - podSelector:
            matchLabels:
              app: housing-service
        - podSelector:
            matchLabels:
              app: calculator-service
      ports:
        - protocol: TCP
          port: 9092
```

---

## 6. 설치 절차

### 6.1 사전 준비

#### 6.1.1 Namespace 생성
```bash
kubectl create namespace home-starter-prod
kubectl config set-context --current --namespace=home-starter-prod
```

#### 6.1.2 TLS 인증서 생성

**Self-Signed 인증서 생성** (또는 Let's Encrypt 사용):
```bash
# 작업 디렉토리 생성
mkdir -p ~/install/kafka-prod/certs && cd ~/install/kafka-prod/certs

# CA 키 생성
openssl genrsa -out ca-key.pem 4096

# CA 인증서 생성
openssl req -new -x509 -days 365 -key ca-key.pem -out ca-cert.pem \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=HomeStarter/OU=IT/CN=kafka-ca"

# Kafka 서버 키 생성
openssl genrsa -out kafka-server-key.pem 4096

# Kafka 서버 CSR 생성
openssl req -new -key kafka-server-key.pem -out kafka-server.csr \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=HomeStarter/OU=IT/CN=kafka.home-starter-prod.svc.cluster.local"

# Kafka 서버 인증서 발급
openssl x509 -req -days 365 -in kafka-server.csr \
  -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial \
  -out kafka-server-cert.pem

# JKS Keystore 생성 (Kafka에서 사용)
openssl pkcs12 -export -in kafka-server-cert.pem -inkey kafka-server-key.pem \
  -out kafka-server.p12 -name kafka -CAfile ca-cert.pem -caname ca \
  -password pass:Hi5Jessica!

keytool -importkeystore -deststorepass Hi5Jessica! -destkeystore kafka-server.jks \
  -srckeystore kafka-server.p12 -srcstoretype PKCS12 -srcstorepass Hi5Jessica! \
  -alias kafka

# Truststore 생성
keytool -keystore kafka-truststore.jks -alias ca-cert -import -file ca-cert.pem \
  -storepass Hi5Jessica! -noprompt
```

#### 6.1.3 Secret 생성
```bash
# TLS 인증서를 Kubernetes Secret으로 생성
kubectl create secret generic kafka-tls \
  --from-file=kafka.keystore.jks=kafka-server.jks \
  --from-file=kafka.truststore.jks=kafka-truststore.jks \
  --from-literal=keystore-password=Hi5Jessica! \
  --from-literal=truststore-password=Hi5Jessica! \
  -n home-starter-prod

# SASL 사용자 비밀번호 Secret 생성
kubectl create secret generic kafka-sasl-users \
  --from-literal=client-passwords='admin=Hi5Jessica!Admin,producer=Hi5Jessica!Prod,consumer=Hi5Jessica!Cons' \
  -n home-starter-prod
```

### 6.2 Kafka 설치

#### 6.2.1 작업 디렉토리 생성
```bash
mkdir -p ~/install/kafka-prod && cd ~/install/kafka-prod
```

#### 6.2.2 values.yaml 작성
```yaml
# values-prod.yaml - Kafka 운영환경 설정

global:
  storageClass: "managed-premium"

# KRaft 모드 활성화
kraft:
  enabled: true

# 보안 설정 (TLS + SASL)
auth:
  clientProtocol: sasl_ssl
  interBrokerProtocol: sasl_ssl
  sasl:
    enabled: true
    mechanism: plain
    jaas:
      clientUsers:
        - admin
        - producer
        - consumer
      clientPasswords:
        - "Hi5Jessica!Admin"
        - "Hi5Jessica!Prod"
        - "Hi5Jessica!Cons"
  tls:
    enabled: true
    existingSecret: "kafka-tls"
    keystorePassword: "Hi5Jessica!"
    truststorePassword: "Hi5Jessica!"

# Listener 설정
listeners:
  client:
    containerPort: 9092
    protocol: SASL_SSL
    name: CLIENT
    sslClientAuth: "none"
  controller:
    name: CONTROLLER
    containerPort: 9093
    protocol: SASL_SSL
  interbroker:
    containerPort: 9094
    protocol: SASL_SSL
    name: INTERNAL

# 외부 접근 비활성화 (운영 보안)
externalAccess:
  enabled: false

# Controller 설정 (고가용성)
controller:
  replicaCount: 3
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
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 100
          podAffinityTerm:
            labelSelector:
              matchLabels:
                app.kubernetes.io/name: kafka
                app.kubernetes.io/component: controller
            topologyKey: kubernetes.io/hostname

# Broker 설정 (고가용성)
broker:
  replicaCount: 3
  heapOpts: "-Xmx4g -Xms4g"
  persistence:
    enabled: true
    size: 100Gi
  automountServiceAccountToken: true
  resources:
    limits:
      memory: 8Gi
      cpu: 4
    requests:
      memory: 4Gi
      cpu: 2
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
        - weight: 100
          podAffinityTerm:
            labelSelector:
              matchLabels:
                app.kubernetes.io/name: kafka
                app.kubernetes.io/component: broker
            topologyKey: kubernetes.io/hostname

# Topic 설정
deleteTopicEnable: true
autoCreateTopicsEnable: false
numPartitions: 6
defaultReplicationFactor: 3

# Offset 설정
offsets:
  topic:
    replicationFactor: 3

# 이미지 설정
image:
  registry: docker.io
  repository: bitnamilegacy/kafka

# RBAC 설정
rbac:
  create: true

# Broker 추가 설정 (운영 최적화)
extraConfig: |
  min.insync.replicas=2
  unclean.leader.election.enable=false
  compression.type=lz4
  log.retention.hours=168
  log.segment.bytes=1073741824
  num.network.threads=16
  num.io.threads=16
  num.replica.fetchers=4
  replica.lag.time.max.ms=10000
  replica.socket.timeout.ms=30000

# JMX Exporter 설정 (Prometheus 메트릭)
metrics:
  kafka:
    enabled: true
  jmx:
    enabled: true
    kafkaJmxPort: 5555
```

#### 6.2.3 Kafka 설치 실행
```bash
helm upgrade -i kafka -f values-prod.yaml bitnami/kafka --version 29.3.14 \
  --namespace home-starter-prod
```

#### 6.2.4 설치 상태 확인
```bash
# Pod 상태 확인
watch kubectl get pods -n home-starter-prod

# Service 확인
kubectl get svc -n home-starter-prod

# PVC 확인
kubectl get pvc -n home-starter-prod
```

### 6.3 Network Policy 적용
```bash
# network-policy.yaml 작성 후 적용
kubectl apply -f network-policy.yaml -n home-starter-prod
```

### 6.4 Topic 생성

#### 6.4.1 관리자 권한으로 Kafka Pod 접속
```bash
kubectl exec -it kafka-broker-0 -n home-starter-prod -- /bin/bash
```

#### 6.4.2 JAAS 설정 파일 생성 (Pod 내부)
```bash
cat > /tmp/jaas.conf <<'EOF'
KafkaClient {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="admin"
  password="Hi5Jessica!Admin";
};
EOF

export KAFKA_OPTS="-Djava.security.auth.login.config=/tmp/jaas.conf"
```

#### 6.4.3 Topic 생성 스크립트
```bash
# asset.events 토픽 생성
kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --create \
  --topic asset.events \
  --partitions 6 \
  --replication-factor 3 \
  --config min.insync.replicas=2 \
  --config retention.ms=604800000 \
  --config compression.type=lz4 \
  --config segment.bytes=1073741824

# housing.events 토픽 생성
kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --create \
  --topic housing.events \
  --partitions 6 \
  --replication-factor 3 \
  --config min.insync.replicas=2 \
  --config retention.ms=604800000 \
  --config compression.type=lz4 \
  --config segment.bytes=1073741824

# dlq.events 토픽 생성
kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --create \
  --topic dlq.events \
  --partitions 3 \
  --replication-factor 3 \
  --config min.insync.replicas=2 \
  --config retention.ms=2592000000 \
  --config compression.type=lz4

# Topic 목록 확인
kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --list

# Topic 상세 정보 확인
kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --describe --topic asset.events
```

#### 6.4.4 ACL 설정 (Topic별 접근 제어)
```bash
# Producer (Asset/Housing Service)에게 쓰기 권한 부여
kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --add --allow-principal User:producer \
  --operation Write --topic asset.events

kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --add --allow-principal User:producer \
  --operation Write --topic housing.events

# Consumer (Calculator Service)에게 읽기 권한 부여
kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --add --allow-principal User:consumer \
  --operation Read --topic asset.events --group calculator.asset.events

kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --add --allow-principal User:consumer \
  --operation Read --topic housing.events --group calculator.housing.events

kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --add --allow-principal User:consumer \
  --operation Read --topic dlq.events --group monitoring.dlq.events

# ACL 목록 확인
kafka-acls.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --list
```

#### 6.4.5 Pod 종료
```bash
exit
```

---

## 7. 검증 계획

### 7.1 설치 검증

#### 7.1.1 Pod 상태 확인
```bash
kubectl get pods -n home-starter-prod | grep kafka

# 예상 결과:
# kafka-controller-0           1/1     Running   0          10m
# kafka-controller-1           1/1     Running   0          10m
# kafka-controller-2           1/1     Running   0          10m
# kafka-broker-0               1/1     Running   0          10m
# kafka-broker-1               1/1     Running   0          10m
# kafka-broker-2               1/1     Running   0          10m
```

#### 7.1.2 Controller Leader 확인
```bash
kubectl exec -it kafka-controller-0 -n home-starter-prod -- \
  kafka-metadata-shell.sh --snapshot /bitnami/kafka/data/__cluster_metadata-0/00000000000000000000.log \
  --print-controllers

# 예상 결과: 3개 Controller 중 1개가 Leader
```

#### 7.1.3 Broker 상태 확인
```bash
kubectl exec -it kafka-broker-0 -n home-starter-prod -- \
  kafka-broker-api-versions.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties

# 예상 결과: 3개 Broker 모두 정상 응답
```

### 7.2 보안 검증

#### 7.2.1 TLS 연결 확인
```bash
# TLS 연결 테스트
openssl s_client -connect kafka:9092 -servername kafka.home-starter-prod.svc.cluster.local

# 예상 결과: TLS 핸드셰이크 성공
```

#### 7.2.2 SASL 인증 테스트
```bash
# 잘못된 비밀번호로 접속 시도 (실패 예상)
kubectl exec -it kafka-broker-0 -n home-starter-prod -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --list --command-config /tmp/wrong-credentials.properties

# 예상 결과: Authentication failed
```

### 7.3 고가용성 검증

#### 7.3.1 Controller Failover 테스트
```bash
# Controller Leader Pod 삭제
LEADER_POD=$(kubectl exec kafka-controller-0 -n home-starter-prod -- \
  kafka-metadata-shell.sh --snapshot /bitnami/kafka/data/__cluster_metadata-0/00000000000000000000.log \
  --print-controllers | grep "is the active controller" | awk '{print $1}')

kubectl delete pod $LEADER_POD -n home-starter-prod

# 새로운 Leader 선출 확인 (< 10초)
sleep 15
kubectl exec -it kafka-controller-0 -n home-starter-prod -- \
  kafka-metadata-shell.sh --snapshot /bitnami/kafka/data/__cluster_metadata-0/00000000000000000000.log \
  --print-controllers
```

#### 7.3.2 Broker Failover 테스트
```bash
# Broker Pod 삭제
kubectl delete pod kafka-broker-0 -n home-starter-prod

# Partition Leader 재선출 확인
kubectl exec -it kafka-broker-1 -n home-starter-prod -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --describe --topic asset.events

# 예상 결과: Broker-0의 Partition Leader가 Broker-1 또는 Broker-2로 이동
```

### 7.4 성능 검증

#### 7.4.1 Producer 성능 테스트
```bash
kubectl exec -it kafka-broker-0 -n home-starter-prod -- \
  kafka-producer-perf-test.sh \
  --topic asset.events \
  --num-records 10000 \
  --record-size 1024 \
  --throughput 500 \
  --producer-props bootstrap.servers=localhost:9092 \
  --producer.config /opt/bitnami/kafka/config/client.properties

# 예상 결과: 초당 500건 이상 처리
```

#### 7.4.2 End-to-End 지연 시간 테스트
```bash
kubectl exec -it kafka-broker-0 -n home-starter-prod -- \
  kafka-run-class.sh kafka.tools.EndToEndLatency \
  localhost:9092 asset.events 10000 1 1024 \
  /opt/bitnami/kafka/config/client.properties

# 예상 결과: 평균 지연 시간 < 50ms, P99 < 200ms
```

---

## 8. 모니터링 및 관리

### 8.1 Prometheus + Grafana 설정

#### 8.1.1 Prometheus ServiceMonitor 생성
```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: kafka-metrics
  namespace: home-starter-prod
spec:
  selector:
    matchLabels:
      app.kubernetes.io/name: kafka
  endpoints:
    - port: metrics
      interval: 30s
```

#### 8.1.2 Grafana 대시보드 Import
- **Dashboard ID**: 7589 (Kafka Exporter Dashboard)
- **URL**: https://grafana.com/grafana/dashboards/7589

### 8.2 주요 모니터링 메트릭

#### 8.2.1 Broker 메트릭
| 메트릭 | 설명 | 임계값 |
|-------|------|--------|
| `kafka_server_broker_topic_metrics_messages_in_per_sec` | 초당 메시지 수 | > 500 (알림) |
| `kafka_server_replica_manager_under_replicated_partitions` | Under-Replicated 파티션 수 | > 0 (알림) |
| `kafka_server_broker_topic_metrics_bytes_in_per_sec` | 초당 수신 바이트 | 모니터링 |
| `kafka_server_broker_topic_metrics_bytes_out_per_sec` | 초당 송신 바이트 | 모니터링 |

#### 8.2.2 Consumer 메트릭
| 메트릭 | 설명 | 임계값 |
|-------|------|--------|
| `kafka_consumer_fetch_manager_records_lag_max` | 최대 Consumer Lag | > 1000 (알림) |
| `kafka_consumer_coordinator_commit_latency_avg` | Commit 평균 지연 시간 | > 100ms (알림) |

### 8.3 알람 설정 (Prometheus AlertManager)

#### 8.3.1 알람 규칙
```yaml
groups:
  - name: kafka-alerts
    interval: 30s
    rules:
      - alert: KafkaBrokerDown
        expr: up{job="kafka-metrics"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Kafka Broker {{ $labels.instance }} is down"

      - alert: UnderReplicatedPartitions
        expr: kafka_server_replica_manager_under_replicated_partitions > 0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Kafka has under-replicated partitions"

      - alert: ConsumerLagHigh
        expr: kafka_consumer_fetch_manager_records_lag_max > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Consumer lag is too high: {{ $value }}"
```

---

## 9. 보안 설정

### 9.1 TLS 인증서 갱신 절차

#### 9.1.1 인증서 만료 확인
```bash
# 인증서 만료일 확인
openssl x509 -in ca-cert.pem -noout -enddate
openssl x509 -in kafka-server-cert.pem -noout -enddate
```

#### 9.1.2 인증서 갱신 (만료 30일 전)
```bash
# 새 인증서 발급 (6.1.2 절차 반복)
# Secret 업데이트
kubectl delete secret kafka-tls -n home-starter-prod
kubectl create secret generic kafka-tls \
  --from-file=kafka.keystore.jks=kafka-server-new.jks \
  --from-file=kafka.truststore.jks=kafka-truststore-new.jks \
  --from-literal=keystore-password=Hi5Jessica! \
  --from-literal=truststore-password=Hi5Jessica! \
  -n home-starter-prod

# Kafka Rolling Restart
kubectl rollout restart statefulset kafka-broker -n home-starter-prod
kubectl rollout restart statefulset kafka-controller -n home-starter-prod
```

### 9.2 SASL 비밀번호 변경 절차

```bash
# 새 비밀번호로 Secret 업데이트
kubectl delete secret kafka-sasl-users -n home-starter-prod
kubectl create secret generic kafka-sasl-users \
  --from-literal=client-passwords='admin=NewPassword1,producer=NewPassword2,consumer=NewPassword3' \
  -n home-starter-prod

# Kafka Rolling Restart
kubectl rollout restart statefulset kafka-broker -n home-starter-prod
```

---

## 10. 백업 및 복구

### 10.1 백업 전략

#### 10.1.1 PV Snapshot 백업 (매일 자동)
```bash
# VolumeSnapshot CRD 사용
apiVersion: snapshot.storage.k8s.io/v1
kind: VolumeSnapshot
metadata:
  name: kafka-broker-0-snapshot-$(date +%Y%m%d)
  namespace: home-starter-prod
spec:
  volumeSnapshotClassName: managed-csi-snapshot
  source:
    persistentVolumeClaimName: data-kafka-broker-0
```

#### 10.1.2 Topic 메타데이터 백업
```bash
# Topic 설정 백업 (매주)
kubectl exec -it kafka-broker-0 -n home-starter-prod -- \
  kafka-topics.sh --bootstrap-server localhost:9092 \
  --command-config /opt/bitnami/kafka/config/client.properties \
  --describe > kafka-topics-backup-$(date +%Y%m%d).txt
```

### 10.2 복구 절차

#### 10.2.1 Broker 데이터 복구
```bash
# PVC 삭제 및 Snapshot에서 복원
kubectl delete pvc data-kafka-broker-0 -n home-starter-prod

# PVC 재생성 (dataSource에 Snapshot 지정)
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: data-kafka-broker-0
  namespace: home-starter-prod
spec:
  dataSource:
    name: kafka-broker-0-snapshot-20251229
    kind: VolumeSnapshot
    apiGroup: snapshot.storage.k8s.io
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 100Gi

# Broker Pod 재시작
kubectl delete pod kafka-broker-0 -n home-starter-prod
```

---

## 11. 연결 정보

### 11.1 서비스 연결 (Spring Boot)

**application-prod.yml**:
```yaml
spring:
  kafka:
    enabled: true
    bootstrap-servers: kafka:9092

    # 보안 설정
    security:
      protocol: SASL_SSL

    ssl:
      trust-store-location: file:/app/config/kafka-truststore.jks
      trust-store-password: Hi5Jessica!
      trust-store-type: JKS

    properties:
      sasl.mechanism: PLAIN
      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="producer" password="Hi5Jessica!Prod";

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
        sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="consumer" password="Hi5Jessica!Cons";

    # Listener 설정
    listener:
      ack-mode: manual
      concurrency: 2
```

### 11.2 Truststore 배포 (ConfigMap)
```bash
# Truststore를 ConfigMap으로 생성
kubectl create configmap kafka-truststore \
  --from-file=kafka-truststore.jks \
  -n home-starter-prod

# Deployment에 Volume Mount 추가
volumes:
  - name: kafka-truststore
    configMap:
      name: kafka-truststore
volumeMounts:
  - name: kafka-truststore
    mountPath: /app/config/kafka-truststore.jks
    subPath: kafka-truststore.jks
```

---

## 12. 설치 체크리스트

### 12.1 사전 준비
- [ ] Namespace 생성 완료
- [ ] TLS 인증서 생성 완료
- [ ] Secret 생성 완료 (TLS, SASL)
- [ ] values-prod.yaml 작성 완료

### 12.2 설치
- [ ] Kafka Helm Chart 설치 완료
- [ ] Network Policy 적용 완료
- [ ] Topic 생성 완료 (asset.events, housing.events, dlq.events)
- [ ] ACL 설정 완료

### 12.3 검증
- [ ] Pod 상태 확인 (Controller 3개, Broker 3개 Running)
- [ ] Controller Leader 확인
- [ ] Broker 상태 확인
- [ ] TLS 연결 확인
- [ ] SASL 인증 테스트
- [ ] Controller Failover 테스트
- [ ] Broker Failover 테스트
- [ ] 성능 테스트 (처리량, 지연 시간)

### 12.4 모니터링
- [ ] Prometheus ServiceMonitor 생성
- [ ] Grafana 대시보드 Import
- [ ] 알람 규칙 설정
- [ ] 알람 테스트 (Slack/Email)

### 12.5 백업
- [ ] PV Snapshot 백업 스케줄 설정
- [ ] Topic 메타데이터 백업 스케줄 설정
- [ ] 복구 절차 문서화

---

## 13. 참고 자료

### 13.1 내부 문서
- 논리아키텍처: `design/backend/logical/logical-architecture.md`
- 개발환경 계획서: `develop/mq/mq-plan-dev.md`

### 13.2 외부 문서
- Kafka 공식 문서: https://kafka.apache.org/documentation/
- Kafka Security: https://kafka.apache.org/documentation/#security
- Bitnami Kafka Helm Chart: https://github.com/bitnami/charts/tree/main/bitnami/kafka

---

**작성일**: 2025-12-29
**검토자**: 아키텍트 홍길동, DevOps 송주영, 보안 담당자
**승인자**: Product Owner 김민준
**문서 버전**: 1.0
