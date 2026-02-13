# HomeStarter - 내집마련 도우미 플랫폼

## 프로젝트 주제

사회초년생과 신혼부부를 위한 **내집마련 도우미 플랫폼**입니다.
입주희망 주택에 대한 사용자의 재무 상태를 분석하고, 대출 적격 여부를 판단하며, AI 기반의 생애주기별 장기주거 로드맵을 제공합니다.

## 프로젝트 목적

- 사회초년생과 신혼부부가 **니즈에 맞는 첫 집을 쉽게 선택**할 수 있도록 지원
- 입주 전/후의 자산, 소득, 지출을 종합적으로 관리하고 분석
- LTV/DTI/DSR 등 **재무 계산을 자동화**하여 대출 충족 여부를 판단
- AI(LLM)를 활용한 **생애주기별 장기주거 로드맵** 설계
- 복수의 입주희망 주택을 비교 분석하여 최적의 선택을 지원

## 기술 스택

| 구분 | 기술 | 버전 | 비고 |
|------|------|------|-----|
| Language | Java | 23 | 사용중 |
| Framework | Spring Boot | 3.3.0 | 사용중 |
| Database | PostgreSQL | - | 사용중 |
| Cache | Redis | - | 사용중 |
| Message Queue | Kafka | - | 미사용 |
| Authentication | JWT (jjwt) | 0.12.5 | 사용중 |
| Build | Gradle | - ||

## 구현 목표

| 요구사항 | 플랫폼 버전 | 일정 |
|------------------|------|------|
| 사회초년생과 신혼부부가 **니즈에 맞는 첫 집을 쉽게 선택**할 수 있도록 지원 | v1.0 | 26년 1분기 |
| 입주 전/후의 자산, 소득, 지출을 종합적으로 관리하고 분석 | v1.0 | 26년 1분기 |
| LTV/DTI/DSR 등 **재무 계산을 자동화**하여 대출 충족 여부를 판단 | v1.0 | 26년 1분기 |
| 복수의 입주희망 주택을 비교 분석하여 최적의 선택을 지원 | v2.0 | 미정 |
| AI(LLM)를 활용한 **생애주기별 장기주거 로드맵** 설계 | v3.0 | 미정 |

## 구현 단계

분석/기획부터 초기모델 개발 단계까지 아래 링크의 github 자료를 참고하여 진행하였습니다.
- LLM을 활용한 분석/기획/설계/개발/배포 실습 교육 (kt ds digital garage 교육 자료) : https://github.com/cna-bootcamp/clauding-guide.git

### 1. 분석/기획/설계

- LLM을 활용하여 분석/기획/설계 작업 진행
- LLM 결과물을 검토하고 수정하여 구현 의도에 맞게 직접 방향성 조정
- 분석/기획/설계 진행 과정 : 

### 2. 초기모델 개발

- LLM을 활용하여 초기모델 개발 진행
- 개발 과정 : 

### 3. 보완 개발

- 개발되지 않은 화면 식별 후 화면 개발 진행 (LLM 활용)
- 프론트-백엔드 간 연동을 위한 보완 개발 진행 (LLM 활용)
- 미완성되거나 요구사항에 맞지 않는 로직에 대한 보완 개발 진행 (수동 및 LLM 활용, github issue tab 참고)

## 인프라 구성 목표

- 온프레미스(집컴) windows 환경 기반으로 다수의 VM(Hyper-V) 구동
- 멀티 VM 기반 Kubernetes cluster 구성
- 현 12GB RAM -> 24GB RAM 증설 예정 (CI/CD 구축 및 kubernetes cluster 스펙 고려)

### 1. 네트워크 구성

댁내 AP --(1)--> Hyper-V 가상 스위치 --(2)--> kubernetes cluster

(1) : 홈AP 고정(유동)IP -> VM 내부 IP/PORT로의 포트포워딩 설정
(2) : 특정 kubernetes service로의 routing 설정 (iptables)

(cluster내 postgresql 접근 ex. 121.129.xx.xx:5000 (AP) -> 172.30.1.5:5432 (VM) -> 172.30.1.200:5432 (kubernetes service))

### 2. kubernetes cluster 구성

- Hyper-V VM01 (172.30.1.5) - kubernetes master node
- Hyper-V VM02 (172.30.1.97) - kubernetes worker node01
- Hyper-V VM03 (172.30.1.98) - kubernetes worker node02
