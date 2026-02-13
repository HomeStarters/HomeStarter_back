# LLM 활용 개발 진행 가이드

LLM 활용하여 분석/기획/설계된 자료를 기반으로 개발을 진행합니다.
진행에 Claude Opus 4.5 모델을 활용하였습니다.

## 개발

### 18. 백킹 서비스 설치

- 배포할 온프레미스 환경의 VM에 DB 및 MQ 등 백킹 서비스 설치 (kubernetes pod 형태)

### 19. MQ설치 계획서 생성 (LLM 활용)

- 마이크로 서비스별 활용 MQ 서비스를 선정하여 MQ 설치 계획서 생성

### 20. MQ/DB/Cache 설치결과서 생성 (LLM 활용)

- 백킹서비스 설치 후의 계정정보, ip정보 등의 서비스 정보를 작성하여 LLM 이용 결과서 생성

### 21. Graddle Wrapper 구성 (LLM 활용)

- 각 서비스별 gradle build를 위한 Graddle Wrapper 구성

### 22. 각 서비스별 공통 로직 개발 (LLM 활용)

- 공통 및 각 서비스별 아키텍처 패턴을 선정하여 공통 로직 개발

### 23. 서비스별 IDE 환경 세팅 (LLM 활용)

- IDE를 통한 서비스 실행을 위해 환경 세팅
- claude code 명령어 /develop-make-run-profile

### 24. 프론트엔드 설계/개발 요청 (LLM 활용)

- 작성된 설계 문서를 기반으로 각 백엔드 서비스별 Swagger API 명세 페이지를 명시하여 프론트엔드 설계서 작성 및 개발
- 설계서 작성 프롬프트
```
@design-front
'프론트엔드설계가이드'를 준용하여 프론트엔드설계서를 작성해 주세요.
[백엔드시스템]
- 마이크로서비스: user-service, asset-service, housing-service, loan-service, calculator-service, roadmap-service
- API문서
  - user service: http://localhost:8080/v3/api-docs
  - asset service: http://localhost:8082/v3/api-docs
  - housing service: http://localhost:8084/v3/api-docs
  - loan service: http://localhost:8083/v3/api-docs
  - calculator service: http://localhost:8085/v3/api-docs
  - roadmap service: http://localhost:8086/v3/api-docs
[요구사항]
- 개발언어는 Typescript + React로 함 
- 각 화면에 Back 아이콘 버튼과 화면 타이틀 표시
```

- 개발 프롬프트
```
@dev-front
"프론트엔드개발가이드"에 따라 개발해 주세요.   
[개발정보]
- 개발프레임워크: Typescript + React 18
- UI프레임워크: MUI v5
- 상태관리: Redux Toolkit
- 라우팅: React Router v6
- API통신: Axios
- 스타일링: MUI + styled-components
- 빌드도구: Vite
```
