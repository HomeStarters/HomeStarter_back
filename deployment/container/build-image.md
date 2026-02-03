# 백엔드 컨테이너 이미지 작성 결과서

## 1. 대상 서비스
| No | 서비스명 | JAR 파일 |
|----|---------|----------|
| 1 | user-service | user-service.jar |
| 2 | asset-service | asset-service.jar |
| 3 | loan-service | loan-service.jar |
| 4 | housing-service | housing-service.jar |
| 5 | calculator-service | calculator-service.jar |
| 6 | roadmap-service | roadmap-service.jar |

## 2. 실행 JAR 빌드

### bootJar 설정
각 서비스의 `build.gradle`에 아래와 같이 설정되어 있음:
```gradle
bootJar {
    archiveFileName = '{서비스명}.jar'
}
```

### 빌드 명령
```bash
./gradlew clean bootJar -x test
```

### 빌드 결과
- BUILD SUCCESSFUL (42 actionable tasks: 42 executed)

## 3. Dockerfile

### 파일 위치
`deployment/container/Dockerfile-backend`

### Dockerfile 내용
```dockerfile
# Build stage
FROM openjdk:23-oraclelinux8 AS builder
ARG BUILD_LIB_DIR
ARG ARTIFACTORY_FILE
COPY ${BUILD_LIB_DIR}/${ARTIFACTORY_FILE} app.jar

# Run stage
FROM openjdk:23-slim
ENV USERNAME=k8s
ENV ARTIFACTORY_HOME=/home/${USERNAME}
ENV JAVA_OPTS=""

# Add a non-root user
RUN adduser --system --group ${USERNAME} && \
    mkdir -p ${ARTIFACTORY_HOME} && \
    chown ${USERNAME}:${USERNAME} ${ARTIFACTORY_HOME}

WORKDIR ${ARTIFACTORY_HOME}
COPY --from=builder app.jar app.jar
RUN chown ${USERNAME}:${USERNAME} app.jar

USER ${USERNAME}

ENTRYPOINT [ "sh", "-c" ]
CMD ["java ${JAVA_OPTS} -jar app.jar"]
```

### Dockerfile 주요 특징
- **멀티스테이지 빌드**: builder 스테이지에서 JAR 복사 후 slim 이미지에 배포
- **비루트 실행**: `k8s` 시스템 사용자로 실행 (보안 강화)
- **JAVA_OPTS 지원**: 환경 변수로 JVM 옵션 전달 가능
- **플랫폼**: `linux/amd64`

## 4. 컨테이너 이미지 빌드

### 빌드 명령
```bash
DOCKER_FILE=deployment/container/Dockerfile-backend

# user-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="user-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="user-service.jar" \
  -f ${DOCKER_FILE} -t user-service:latest .

# asset-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="asset-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="asset-service.jar" \
  -f ${DOCKER_FILE} -t asset-service:latest .

# loan-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="loan-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="loan-service.jar" \
  -f ${DOCKER_FILE} -t loan-service:latest .

# housing-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="housing-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="housing-service.jar" \
  -f ${DOCKER_FILE} -t housing-service:latest .

# calculator-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="calculator-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="calculator-service.jar" \
  -f ${DOCKER_FILE} -t calculator-service:latest .

# roadmap-service
docker build --platform linux/amd64 \
  --build-arg BUILD_LIB_DIR="roadmap-service/build/libs" \
  --build-arg ARTIFACTORY_FILE="roadmap-service.jar" \
  -f ${DOCKER_FILE} -t roadmap-service:latest .
```

## 5. 이미지 빌드 결과

| No | 이미지명 | 태그 | IMAGE ID | 크기 |
|----|---------|------|----------|------|
| 1 | user-service | latest | 63e20d060013 | 584MB |
| 2 | asset-service | latest | 56d572f579af | 621MB |
| 3 | loan-service | latest | 881157256bac | 584MB |
| 4 | housing-service | latest | bd53c809d8cd | 584MB |
| 5 | calculator-service | latest | e96101119142 | 641MB |
| 6 | roadmap-service | latest | daf566fd14cb | 642MB |

### 이미지 확인 명령
```bash
docker images | grep -E "user-service|asset-service|loan-service|housing-service|calculator-service|roadmap-service"
```

## 6. 결과 요약
- 6개 서비스 모두 컨테이너 이미지 빌드 성공
- 베이스 이미지: `openjdk:23-slim` (런타임)
- 플랫폼: `linux/amd64`
- 비루트 실행 환경 구성 완료 (`k8s` 사용자)
