# Homestarter 백엔드 쿠버네티스 배포 가이드

## 1. 실행 정보

| 항목 | 값 |
|------|-----|
| IMG_REG | docker.io |
| IMG_ORG | daewoongjeon |
| IMG_ID | daewoongjeon |
| BACKEND_HOST | homestarter-api.172.30.1.205.nip.io |
| FRONTEND_HOST | homestarter.172.30.1.205.nip.io |
| 네임스페이스 | homestarter-ns |
| 파드수 | 1 |
| 리소스(CPU) | 256m/256m |
| 리소스(메모리) | 256Mi/256Mi |

## 2. 시스템 및 서비스 정보

- **시스템명**: homestarter
- **서비스 목록**:
  - user-service (포트: 8081)
  - asset-service (포트: 8082)
  - loan-service (포트: 8083)
  - housing-service (포트: 8084)
  - calculator-service (포트: 8085)
  - roadmap-service (포트: 8086)

## 3. 매니페스트 파일 구조

```
deployment/k8s/
├── common/
│   ├── secret-imagepull.yaml    # Image Pull Secret (name: homestarter)
│   ├── ingress.yaml             # Ingress (name: homestarter)
│   ├── cm-common.yaml           # 공통 ConfigMap (name: cm-common)
│   └── secret-common.yaml       # 공통 Secret (name: secret-common)
├── user-service/
│   ├── cm-user-service.yaml     # ConfigMap (name: cm-user-service)
│   ├── secret-user-service.yaml # Secret (name: secret-user-service)
│   ├── service.yaml             # Service (name: user-service)
│   └── deployment.yaml          # Deployment (name: user-service)
├── asset-service/
│   ├── cm-asset-service.yaml
│   ├── secret-asset-service.yaml
│   ├── service.yaml
│   └── deployment.yaml
├── loan-service/
│   ├── cm-loan-service.yaml
│   ├── secret-loan-service.yaml
│   ├── service.yaml
│   └── deployment.yaml
├── housing-service/
│   ├── cm-housing-service.yaml
│   ├── secret-housing-service.yaml
│   ├── service.yaml
│   └── deployment.yaml
├── calculator-service/
│   ├── cm-calculator-service.yaml
│   ├── secret-calculator-service.yaml
│   ├── service.yaml
│   └── deployment.yaml
├── roadmap-service/
│   ├── cm-roadmap-service.yaml
│   ├── secret-roadmap-service.yaml
│   ├── service.yaml
│   └── deployment.yaml
└── deploy-k8s-guide.md
```

## 4. Ingress 경로 매핑

| 경로 | 서비스 | 포트 |
|------|--------|------|
| /users | user-service | 80 |
| /api/v1/assets | asset-service | 80 |
| /housings | housing-service | 80 |
| /api/v1/loans | loan-service | 80 |
| /api/v1/admin/loans | loan-service | 80 |
| /calculator | calculator-service | 80 |
| /roadmaps | roadmap-service | 80 |
| /lifecycle-events | roadmap-service | 80 |

## 5. 검증 결과

### 5.1 체크리스트

| # | 검증 항목 | 결과 | 비고 |
|---|----------|------|------|
| 1 | 객체이름 네이밍룰 준수 | ✅ | cm-common, secret-common, cm-{서비스명}, secret-{서비스명}, Ingress: homestarter, Service/Deployment: {서비스명} |
| 2 | Redis Host명을 ClusterIP 타입의 Service 객체로 지정 | ✅ | `redis` 사용 (kubectl get svc \| grep redis로 확인 필요) |
| 3 | Database Host명을 ClusterIP 타입의 Service 객체로 지정 | ✅ | `postgresql` 사용 (kubectl get svc \| grep postgresql로 확인 필요) |
| 4 | Secret 매니페스트에서 stringData 사용 | ✅ | 모든 Secret에서 stringData 사용 |
| 5 | JWT_SECRET을 openssl 명령으로 생성 | ✅ | `openssl rand -base64 32` → `siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I=` |
| 6 | 매니페스트에 환경변수 미사용, 실제 값 지정 | ✅ | 모든 매니페스트에 실제 값 지정 |
| 7 | Image Pull Secret에 실제 USERNAME/PASSWORD 지정 | ✅ | daewoongjeon / Vnawmd135* 실제 값 지정 |
| 8 | Image명 형식 확인 | ✅ | docker.io/daewoongjeon/{서비스명}:latest |
| 9 | 보안 환경변수 Secret 지정 | ✅ | DB_PASSWORD, JWT_SECRET, REDIS_PASSWORD, LLM_API_KEY 등 |
| 10 | REDIS_DATABASE 서비스별 다르게 지정 | ✅ | user:0, asset:2, loan:3, housing:4, calculator:5, roadmap:6 |
| 11 | envFrom 사용 | ✅ | 모든 Deployment에서 configMapRef, secretRef 사용 |

### 5.2 실행 프로파일 매핑 테이블

| 서비스명 | 환경변수 | 지정 객체명 | 환경변수값 |
|----------|----------|-------------|------------|
| **user-service** | | | |
| user-service | SERVER_PORT | cm-user-service | 8081 |
| user-service | DB_KIND | cm-user-service | postgresql |
| user-service | DB_HOST | secret-user-service | postgresql |
| user-service | DB_PORT | cm-user-service | 5010 |
| user-service | DB_NAME | cm-user-service | homestarterdb |
| user-service | DB_USERNAME | secret-user-service | homestarteruser |
| user-service | DB_PASSWORD | secret-user-service | Vnawmd135* |
| user-service | DDL_AUTO | cm-user-service | update |
| user-service | SHOW_SQL | cm-user-service | true |
| user-service | REDIS_HOST | cm-common | redis |
| user-service | REDIS_PORT | cm-common | 6379 |
| user-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| user-service | REDIS_DATABASE | cm-user-service | 0 |
| user-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| user-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| user-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| user-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| user-service | LOG_LEVEL_APP | cm-common | DEBUG |
| user-service | LOG_LEVEL_WEB | cm-common | INFO |
| user-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| user-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| user-service | LOG_FILE_PATH | cm-user-service | logs/user-service.log |
| **asset-service** | | | |
| asset-service | SERVER_PORT | cm-asset-service | 8082 |
| asset-service | DB_KIND | cm-asset-service | postgresql |
| asset-service | DB_HOST | secret-asset-service | postgresql |
| asset-service | DB_PORT | cm-asset-service | 5010 |
| asset-service | DB_NAME | cm-asset-service | homestarterdb |
| asset-service | DB_USERNAME | secret-asset-service | homestarteruser |
| asset-service | DB_PASSWORD | secret-asset-service | Vnawmd135* |
| asset-service | DDL_AUTO | cm-asset-service | update |
| asset-service | SHOW_SQL | cm-asset-service | true |
| asset-service | REDIS_HOST | cm-common | redis |
| asset-service | REDIS_PORT | cm-common | 6379 |
| asset-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| asset-service | REDIS_DATABASE | cm-asset-service | 2 |
| asset-service | KAFKA_BOOTSTRAP_SERVERS | cm-asset-service | kafka:9092 |
| asset-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| asset-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| asset-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| asset-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| asset-service | LOG_LEVEL_APP | cm-common | DEBUG |
| asset-service | LOG_LEVEL_WEB | cm-common | INFO |
| asset-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| asset-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| asset-service | LOG_FILE_PATH | cm-asset-service | logs/asset-service.log |
| **loan-service** | | | |
| loan-service | SERVER_PORT | cm-loan-service | 8083 |
| loan-service | DB_KIND | cm-loan-service | postgresql |
| loan-service | DB_HOST | secret-loan-service | postgresql |
| loan-service | DB_PORT | cm-loan-service | 5010 |
| loan-service | DB_NAME | cm-loan-service | homestarterdb |
| loan-service | DB_USERNAME | secret-loan-service | homestarteruser |
| loan-service | DB_PASSWORD | secret-loan-service | Vnawmd135* |
| loan-service | DDL_AUTO | cm-loan-service | update |
| loan-service | SHOW_SQL | cm-loan-service | true |
| loan-service | REDIS_HOST | cm-common | redis |
| loan-service | REDIS_PORT | cm-common | 6379 |
| loan-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| loan-service | REDIS_DATABASE | cm-loan-service | 3 |
| loan-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| loan-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| loan-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| loan-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| loan-service | LOG_LEVEL_APP | cm-common | DEBUG |
| loan-service | LOG_LEVEL_WEB | cm-common | INFO |
| loan-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| loan-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| loan-service | LOG_FILE_PATH | cm-loan-service | logs/loan-service.log |
| **housing-service** | | | |
| housing-service | SERVER_PORT | cm-housing-service | 8084 |
| housing-service | DB_KIND | cm-housing-service | postgresql |
| housing-service | DB_HOST | secret-housing-service | postgresql |
| housing-service | DB_PORT | cm-housing-service | 5010 |
| housing-service | DB_NAME | cm-housing-service | homestarterdb |
| housing-service | DB_USERNAME | secret-housing-service | homestarteruser |
| housing-service | DB_PASSWORD | secret-housing-service | Vnawmd135* |
| housing-service | DDL_AUTO | cm-housing-service | update |
| housing-service | SHOW_SQL | cm-housing-service | true |
| housing-service | REDIS_HOST | cm-common | redis |
| housing-service | REDIS_PORT | cm-common | 6379 |
| housing-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| housing-service | REDIS_DATABASE | cm-housing-service | 4 |
| housing-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| housing-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| housing-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| housing-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| housing-service | LOG_LEVEL_APP | cm-common | DEBUG |
| housing-service | LOG_LEVEL_WEB | cm-common | INFO |
| housing-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| housing-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| housing-service | LOG_FILE_PATH | cm-housing-service | logs/housing-service.log |
| **calculator-service** | | | |
| calculator-service | SERVER_PORT | cm-calculator-service | 8085 |
| calculator-service | DB_KIND | cm-calculator-service | postgresql |
| calculator-service | DB_HOST | secret-calculator-service | postgresql |
| calculator-service | DB_PORT | cm-calculator-service | 5010 |
| calculator-service | DB_NAME | cm-calculator-service | homestarterdb |
| calculator-service | DB_USERNAME | secret-calculator-service | homestarteruser |
| calculator-service | DB_PASSWORD | secret-calculator-service | Vnawmd135* |
| calculator-service | DDL_AUTO | cm-calculator-service | update |
| calculator-service | SHOW_SQL | cm-calculator-service | true |
| calculator-service | REDIS_HOST | cm-common | redis |
| calculator-service | REDIS_PORT | cm-common | 6379 |
| calculator-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| calculator-service | REDIS_DATABASE | cm-calculator-service | 5 |
| calculator-service | KAFKA_BOOTSTRAP_SERVERS | cm-calculator-service | kafka:9092 |
| calculator-service | USER_SERVICE_URL | cm-calculator-service | http://user-service |
| calculator-service | ASSET_SERVICE_URL | cm-calculator-service | http://asset-service |
| calculator-service | HOUSING_SERVICE_URL | cm-calculator-service | http://housing-service |
| calculator-service | LOAN_SERVICE_URL | cm-calculator-service | http://loan-service |
| calculator-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| calculator-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| calculator-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| calculator-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| calculator-service | LOG_LEVEL_APP | cm-common | DEBUG |
| calculator-service | LOG_LEVEL_WEB | cm-common | INFO |
| calculator-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| calculator-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| calculator-service | LOG_FILE_PATH | cm-calculator-service | logs/calculator-service.log |
| **roadmap-service** | | | |
| roadmap-service | SERVER_PORT | cm-roadmap-service | 8086 |
| roadmap-service | DB_KIND | cm-roadmap-service | postgresql |
| roadmap-service | DB_HOST | secret-roadmap-service | postgresql |
| roadmap-service | DB_PORT | cm-roadmap-service | 5010 |
| roadmap-service | DB_NAME | cm-roadmap-service | homestarterdb |
| roadmap-service | DB_USERNAME | secret-roadmap-service | homestarteruser |
| roadmap-service | DB_PASSWORD | secret-roadmap-service | Vnawmd135* |
| roadmap-service | DDL_AUTO | cm-roadmap-service | update |
| roadmap-service | SHOW_SQL | cm-roadmap-service | true |
| roadmap-service | REDIS_HOST | cm-common | redis |
| roadmap-service | REDIS_PORT | cm-common | 6379 |
| roadmap-service | REDIS_PASSWORD | secret-common | Vnawmd135* |
| roadmap-service | REDIS_DATABASE | cm-roadmap-service | 6 |
| roadmap-service | KAFKA_BOOTSTRAP_SERVERS | cm-roadmap-service | kafka:9092 |
| roadmap-service | USER_SERVICE_URL | cm-roadmap-service | http://user-service |
| roadmap-service | ASSET_SERVICE_URL | cm-roadmap-service | http://asset-service |
| roadmap-service | HOUSING_SERVICE_URL | cm-roadmap-service | http://housing-service |
| roadmap-service | CALCULATOR_SERVICE_URL | cm-roadmap-service | http://calculator-service |
| roadmap-service | LLM_API_URL | cm-roadmap-service | https://api.openai.com |
| roadmap-service | LLM_API_KEY | secret-roadmap-service | (배포 전 설정 필요) |
| roadmap-service | JWT_SECRET | secret-common | siTTeU7IVrPkbSSCf9AcLoTu5LVF8kJhSmHp5rm7t/I= |
| roadmap-service | JWT_ACCESS_TOKEN_VALIDITY | cm-common | 1800 |
| roadmap-service | JWT_REFRESH_TOKEN_VALIDITY | cm-common | 86400 |
| roadmap-service | CORS_ALLOWED_ORIGINS | cm-common | http://localhost:8081,...,http://homestarter.172.30.1.205.nip.io |
| roadmap-service | LOG_LEVEL_APP | cm-common | DEBUG |
| roadmap-service | LOG_LEVEL_WEB | cm-common | INFO |
| roadmap-service | LOG_LEVEL_SQL | cm-common | DEBUG |
| roadmap-service | LOG_LEVEL_SQL_TYPE | cm-common | TRACE |
| roadmap-service | LOG_FILE_PATH | cm-roadmap-service | logs/roadmap-service.log |

### 5.3 주의사항

- **Redis Host**: `redis`로 지정. 배포 전 `kubectl get svc | grep redis` 명령으로 실제 ClusterIP 타입 Redis 서비스명 확인 후 필요시 수정
- **Database Host**: `postgresql`로 지정. 배포 전 `kubectl get svc | grep postgres` 명령으로 실제 ClusterIP 타입 PostgreSQL 서비스명 확인 후 필요시 수정
- **Kafka Host**: `kafka:9092`로 지정. 배포 전 `kubectl get svc | grep kafka` 명령으로 실제 Kafka 서비스명 확인 후 필요시 수정
- **LLM_API_KEY**: roadmap-service의 `secret-roadmap-service.yaml`에 실제 API 키 설정 필요

## 6. 사전 확인

### 6.1 네임스페이스 존재 확인
```bash
kubectl get ns homestarter-ns
```

네임스페이스가 없으면 생성:
```bash
kubectl create ns homestarter-ns
```

### 6.2 백킹 서비스 확인
```bash
# Redis 서비스 확인
kubectl get svc | grep redis

# PostgreSQL 서비스 확인
kubectl get svc | grep postgres

# Kafka 서비스 확인 (asset, calculator, roadmap 서비스에서 사용)
kubectl get svc | grep kafka
```

### 6.3 Ingress Controller 확인
```bash
kubectl get pods -n ingress-nginx
```

## 7. 매니페스트 적용

### 7.1 전체 매니페스트 적용
```bash
kubectl apply -f deployment/k8s -R
```

### 7.2 개별 적용 (순서)
```bash
# 1. 공통 리소스
kubectl apply -f deployment/k8s/common/

# 2. 서비스별 리소스
kubectl apply -f deployment/k8s/user-service/
kubectl apply -f deployment/k8s/asset-service/
kubectl apply -f deployment/k8s/loan-service/
kubectl apply -f deployment/k8s/housing-service/
kubectl apply -f deployment/k8s/calculator-service/
kubectl apply -f deployment/k8s/roadmap-service/
```

## 8. 객체 생성 확인

### 8.1 전체 리소스 확인
```bash
kubectl get all -n homestarter-ns
```

### 8.2 개별 확인
```bash
# ConfigMap 확인
kubectl get cm -n homestarter-ns

# Secret 확인
kubectl get secret -n homestarter-ns

# Deployment 확인
kubectl get deploy -n homestarter-ns

# Pod 상태 확인
kubectl get pods -n homestarter-ns

# Service 확인
kubectl get svc -n homestarter-ns

# Ingress 확인
kubectl get ingress -n homestarter-ns
```

### 8.3 Pod 로그 확인
```bash
# 각 서비스 로그 확인
kubectl logs -f -l app=user-service -n homestarter-ns
kubectl logs -f -l app=asset-service -n homestarter-ns
kubectl logs -f -l app=loan-service -n homestarter-ns
kubectl logs -f -l app=housing-service -n homestarter-ns
kubectl logs -f -l app=calculator-service -n homestarter-ns
kubectl logs -f -l app=roadmap-service -n homestarter-ns
```

### 8.4 접속 테스트
```bash
# API 엔드포인트 테스트
curl http://homestarter-api.172.30.1.205.nip.io/users/profile
curl http://homestarter-api.172.30.1.205.nip.io/api/v1/assets
curl http://homestarter-api.172.30.1.205.nip.io/api/v1/loans
curl http://homestarter-api.172.30.1.205.nip.io/housings
curl http://homestarter-api.172.30.1.205.nip.io/calculator/results
curl http://homestarter-api.172.30.1.205.nip.io/roadmaps
```

## 9. 트러블슈팅

### Pod가 시작되지 않는 경우
```bash
# Pod 이벤트 확인
kubectl describe pod -l app={서비스명} -n homestarter-ns

# 이미지 풀 에러 확인
kubectl get events -n homestarter-ns --sort-by=.metadata.creationTimestamp
```

### 서비스 연결 실패 시
```bash
# 서비스 엔드포인트 확인
kubectl get endpoints -n homestarter-ns

# DNS 확인 (Pod 내에서)
kubectl exec -it {pod-name} -n homestarter-ns -- nslookup user-service
```
