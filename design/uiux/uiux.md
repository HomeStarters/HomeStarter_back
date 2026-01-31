# UI/UX 설계서 - 내집마련 도우미 플랫폼

## 문서 정보
- **작성일**: 2025-12-14
- **작성자**: 강지수 (Product Designer)
- **버전**: 1.0
- **관련 문서**: design/userstory.md

---

## 목차
1. [개요](#1-개요)
2. [디자인 시스템](#2-디자인-시스템)
3. [화면 구조](#3-화면-구조)
4. [화면별 상세 설계](#4-화면별-상세-설계)
5. [공통 컴포넌트](#5-공통-컴포넌트)
6. [화면 간 전환 및 네비게이션](#6-화면-간-전환-및-네비게이션)
7. [반응형 설계 전략](#7-반응형-설계-전략)
8. [접근성 보장 방안](#8-접근성-보장-방안)
9. [성능 최적화 방안](#9-성능-최적화-방안)
10. [기술 스택 및 라이브러리](#10-기술-스택-및-라이브러리)
11. [변경 이력](#11-변경-이력)

---

## 1. 개요

### 1.1 프로젝트 소개
사회초년생과 신혼부부를 위한 맞춤형 주택 선택 지원 서비스의 UI/UX 설계

### 1.2 설계 목표
- **사용자 중심**: 복잡한 재무 정보를 직관적으로 입력하고 이해할 수 있는 UI
- **Mobile First**: 모바일 환경에서 최적의 경험 제공
- **접근성**: WCAG 2.1 AA 수준 준수
- **성능**: 빠른 로딩과 부드러운 인터랙션

### 1.3 설계 원칙
1. **유저스토리 기반**: 모든 화면과 기능은 유저스토리와 1:1 매칭
2. **Mobile First**: 모바일(320-767px)을 기본으로 설계 후 점진적 향상
3. **우선순위 중심**: 핵심 정보와 기능을 먼저 제공
4. **점진적 공개**: 복잡한 정보는 단계적으로 표시
5. **성능 최우선**: 모든 디자인 결정에서 성능을 고려

### 1.4 레퍼런스 분석
- **분석 대상**: monimo.com (삼성카드 금융 통합 앱)
- **주요 인사이트**:
  * 카드 기반 정보 구조가 금융 데이터 표현에 효과적
  * 자산 분석 섹션의 시각적 계층 구조 우수
  * 3가지 큐레이션 홈 제공으로 사용자 맞춤화
  * 명확한 CTA (앱 다운로드) 배치
- **적용 사항**:
  * 대시보드 재무 현황을 카드 형태로 구성
  * 자산/대출/주택 정보를 카드 리스트로 표현
  * 시각적 차트(도넛, 게이지 바) 적극 활용

---

## 2. 디자인 시스템

### 2.1 색상 시스템

#### Primary Colors
- **Primary**: `#0066FF`
  - 용도: 주요 CTA 버튼, 링크, 강조 요소
  - 의미: 신뢰감, 전문성
- **Primary Hover**: `#0052CC`
- **Primary Disabled**: `#99B3FF`

#### Secondary Colors
- **Secondary**: `#00C9A7`
  - 용도: 성공 메시지, 긍정적 지표
  - 의미: 성장, 안정
- **Secondary Hover**: `#00A688`

#### Semantic Colors
- **Error**: `#FF3B30`
  - 용도: 오류 메시지, 경고, 삭제 액션
- **Warning**: `#FF9500`
  - 용도: 주의 메시지, 중요 알림
- **Success**: `#00C9A7`
  - 용도: 성공 메시지, 완료 상태
- **Info**: `#007AFF`
  - 용도: 정보 메시지, 힌트

#### Neutral Colors
- **Background**: `#F5F5F7`
  - 용도: 앱 전체 배경
- **Surface**: `#FFFFFF`
  - 용도: 카드, 모달, 입력 필드 배경
- **Border**: `#D1D1D6`
  - 용도: 구분선, 테두리
- **Text Primary**: `#1D1D1F`
  - 용도: 주요 텍스트
- **Text Secondary**: `#86868B`
  - 용도: 보조 텍스트, 설명

### 2.2 타이포그래피

#### 폰트 패밀리
- **Primary**: Pretendard Variable (한글), SF Pro (영문/숫자)
- **Fallback**: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif

#### 타이포그래피 스케일
| 레벨 | 크기 | 굵기 | 행간 | 용도 |
|------|------|------|------|------|
| H1 | 28px | Bold (700) | 36px | 화면 타이틀 |
| H2 | 22px | Bold (700) | 28px | 섹션 헤더 |
| H3 | 18px | Semibold (600) | 24px | 카드 타이틀 |
| Body | 16px | Regular (400) | 24px | 본문 텍스트 |
| Caption | 14px | Regular (400) | 20px | 보조 텍스트 |
| Small | 12px | Regular (400) | 16px | 힌트, 레이블 |

#### 금액 표시
- **강조 금액**: 24px / Bold / Primary Color
- **일반 금액**: 18px / Semibold / Text Primary
- **천 단위 콤마**: 자동 삽입
- **단위**: "원" 표시

### 2.3 간격(Spacing) 시스템
- **4px 기본 단위** (4의 배수 사용)
- **간격 스케일**:
  * xs: 4px (최소 간격)
  * sm: 8px (밀집 요소)
  * md: 16px (기본 간격)
  * lg: 24px (섹션 구분)
  * xl: 32px (화면 여백)
  * xxl: 48px (주요 섹션 구분)

### 2.4 그림자(Elevation)
- **Level 1** (카드): `0 1px 3px rgba(0,0,0,0.1)`
- **Level 2** (모달): `0 4px 12px rgba(0,0,0,0.15)`
- **Level 3** (드롭다운): `0 8px 24px rgba(0,0,0,0.2)`

### 2.5 테두리(Border Radius)
- **Small**: 4px (체크박스, 뱃지)
- **Medium**: 8px (버튼, 입력 필드)
- **Large**: 12px (카드)
- **XLarge**: 16px (모달)
- **Round**: 50% (아바타, FAB)

---

## 3. 화면 구조

### 3.1 프로토타입 화면 목록

#### 인증 및 기본정보 (User 서비스)
1. **01-로그인**
   - 유저스토리: UFR-USER-020
   - 중요도: M (중간)

2. **02-회원가입**
   - 유저스토리: UFR-USER-010
   - 중요도: M (중간)

3. **03-기본정보입력**
   - 유저스토리: UFR-USER-040
   - 중요도: M (중간)

4. **04-프로필편집**
   - 유저스토리: UFR-USER-050
   - 중요도: S (낮음)

#### 자산정보 (Asset 서비스)
5. **05-본인자산정보입력**
   - 유저스토리: UFR-ASST-010
   - 중요도: M (중간)

6. **06-배우자자산정보입력**
   - 유저스토리: UFR-ASST-020
   - 중요도: M (중간)

7. **07-자산정보관리**
   - 유저스토리: UFR-ASST-030
   - 중요도: S (낮음)

#### 대출상품 (Loan 서비스)
8. **08-대출상품목록**
   - 유저스토리: UFR-LOAN-010
   - 중요도: S (낮음)

9. **09-대출상품상세**
   - 유저스토리: UFR-LOAN-020
   - 중요도: C (매우 낮음)

10. **10-대출상품관리** (관리자)
    - 유저스토리: AFR-LOAN-030
    - 중요도: S (낮음)

#### 주택정보 (Housing 서비스)
11. **11-입주희망주택입력-기본정보**
    - 유저스토리: UFR-HOUS-010 (Step 1)
    - 중요도: M (중간)

12. **12-입주희망주택입력-상세정보**
    - 유저스토리: UFR-HOUS-010 (Step 2)
    - 중요도: M (중간)

13. **13-입주희망주택목록**
    - 유저스토리: UFR-HOUS-020
    - 중요도: S (낮음)

14. **14-입주희망주택상세**
    - 유저스토리: UFR-HOUS-030, UFR-HOUS-040
    - 중요도: S (낮음)

#### 계산 (Calculator 서비스)
15. **15-입주후지출계산**
    - 유저스토리: UFR-CALC-010
    - 중요도: M (중간)

16. **16-계산결과목록**
    - 유저스토리: UFR-CALC-020
    - 중요도: C (매우 낮음)

#### 로드맵 (Roadmap 서비스)
17. **17-생애주기이벤트관리**
    - 유저스토리: UFR-ROAD-010
    - 중요도: M (중간)

18. **18-장기주거로드맵조회**
    - 유저스토리: UFR-ROAD-030
    - 중요도: S (낮음)

#### 공통
19. **00-대시보드**
    - 유저스토리: 전체 서비스 진입점
    - 중요도: H (높음)

### 3.2 화면 간 사용자 플로우

#### 초기 온보딩 플로우 (필수)
```
02-회원가입
  → 01-로그인
  → 03-기본정보입력
  → 05-본인자산정보입력
  → 06-배우자자산정보입력
  → 00-대시보드
```

#### 주택 등록 및 계산 플로우 (핵심)
```
00-대시보드
  → 11-입주희망주택입력-기본정보
  → 12-입주희망주택입력-상세정보
  → 13-입주희망주택목록
  → 14-입주희망주택상세
  → 08-대출상품목록
  → 09-대출상품상세
  → 15-입주후지출계산
  → 16-계산결과목록
```

#### 로드맵 생성 플로우 (차별화)
```
00-대시보드
  → 17-생애주기이벤트관리
  → 13-입주희망주택목록
  → 최종목표 주택 선택
  → 18-장기주거로드맵조회
```

#### 정보 관리 플로우
```
00-대시보드
  → 07-자산정보관리
  → 수정 완료

00-대시보드
  → 04-프로필편집
  → 수정 완료
```

### 3.3 네비게이션 구조

#### 하단 탭 네비게이션 (모바일)
- **홈**: 00-대시보드
- **주택**: 13-입주희망주택목록
- **계산**: 15-입주후지출계산
- **로드맵**: 18-장기주거로드맵조회
- **더보기**: 프로필, 자산 관리, 설정

#### 사이드 메뉴 (태블릿/데스크톱)
- 대시보드
- 내 정보 관리
  * 프로필 편집
  * 자산정보 관리
- 주택 관리
  * 입주희망주택 목록
  * 주택 등록
- 대출상품
- 계산 및 분석
  * 입주 후 지출 계산
  * 계산 결과 목록
- 장기주거 로드맵
  * 생애주기 이벤트
  * 로드맵 조회
- 설정

---

## 4. 화면별 상세 설계

### 4.1 00-대시보드

#### 개요
- **목적**: 통합 정보 제공 및 주요 기능 빠른 접근
- **관련 유저스토리**: 모든 서비스의 진입점
- **비즈니스 중요도**: H (높음) - 사용자가 가장 자주 방문하는 화면

#### 주요 기능
1. 재무 현황 요약 표시
2. 빠른 작업 바로가기 제공
3. 최근 활동 표시
4. 주요 기능 접근

#### UI 구성요소

**헤더**
- 사용자 아바타 (원형, 32px)
  * 이름 첫 글자 표시
  * 배경색: Primary
  * 텍스트: White
- 사용자 이름 (Body)
- 알림 아이콘 (24px, 뱃지 표시 가능)

**재무 현황 섹션**
- 섹션 타이틀: "내 재무 현황" (H2)
- 총 자산 카드:
  * 레이블: "총 자산" (Caption, Text Secondary)
  * 금액: 강조 금액 스타일
  * 전월 대비 증감: ↑ 5.2% (Success/Error 색상)
- 총 대출 카드:
  * 레이블: "총 대출"
  * 금액: 강조 금액 스타일
  * 전월 대비 증감
- 자산 비중 카드:
  * 레이블: "자산 비중"
  * 도넛 차트 (recharts)
  * 범례: 부동산, 예금, 주식, 기타

**빠른 작업 섹션**
- 섹션 타이틀: "빠른 작업" (H2)
- 액션 카드 (3개):
  1. "주택 등록하기"
     - 아이콘: 🏠
     - 설명: "관심 주택을 등록하세요"
     - 클릭: 11-입주희망주택입력
  2. "지출 계산하기"
     - 아이콘: 💰
     - 설명: "입주 후 재무 상태를 확인하세요"
     - 클릭: 15-입주후지출계산
  3. "로드맵 보기"
     - 아이콘: 🗺️
     - 설명: "장기주거 계획을 확인하세요"
     - 클릭: 18-장기주거로드맵조회

**최근 활동 섹션**
- 섹션 타이틀: "최근 활동" (H2)
- 탭: "등록한 주택" | "계산 결과"
- 최근 등록한 주택 (최대 3개):
  * 주택 카드: 이름, 위치, 가격, 입주희망년월
  * Empty State: "아직 등록한 주택이 없습니다" + "주택 등록하기" 버튼
- 최근 계산 결과 (최대 3개):
  * 결과 카드: 주택이름, 계산일시, 충족여부 뱃지
  * Empty State: "아직 계산 결과가 없습니다"

#### 인터랙션
- **Pull to Refresh** (모바일): 데이터 새로고침
- **카드 클릭**: 해당 상세 화면으로 이동
- **스크롤**:
  * 헤더 축소 (collapsible header)
  * 무한 스크롤 없음 (최근 3개만 표시)
- **스켈레톤 로딩**: 초기 로딩 시 카드 윤곽 표시

#### 반응형 동작
- **Mobile (320-767px)**:
  * 싱글 컬럼 레이아웃
  * 재무 현황 카드: 2개씩 가로 배치
  * 빠른 작업 카드: 세로 스택

- **Tablet (768-1023px)**:
  * 재무 현황: 3개 가로 배치
  * 빠른 작업: 3개 가로 배치
  * 최근 활동: 2컬럼 그리드

- **Desktop (1024px+)**:
  * 3컬럼 레이아웃
  * 좌측: 재무 현황 + 빠른 작업
  * 우측: 최근 활동 (확장)

---

### 4.2 01-로그인

#### 개요
- **목적**: 간편한 사용자 인증
- **관련 유저스토리**: UFR-USER-020
- **비즈니스 중요도**: M (중간)

#### 주요 기능
1. 아이디/비밀번호 인증
2. 로그인 상태 유지
3. 회원가입 안내
4. 비밀번호 찾기

#### UI 구성요소

**헤더**
- 로고 (중앙 정렬, 64px)
- 타이틀: "로그인" (H1)

**입력 폼**
- 아이디 입력 필드:
  * Label: "아이디"
  * Placeholder: "아이디를 입력하세요"
  * Type: text
  * Auto-complete: username
- 비밀번호 입력 필드:
  * Label: "비밀번호"
  * Placeholder: "비밀번호를 입력하세요"
  * Type: password
  * 표시/숨김 토글 아이콘
  * Auto-complete: current-password

**옵션**
- 로그인 유지 체크박스:
  * Label: "로그인 상태 유지"
  * 기본값: unchecked

**CTA 버튼**
- "로그인" 버튼:
  * Primary 색상
  * Full width
  * Height: 48px
  * Disabled: 입력 전

**링크**
- "회원가입" 링크 (Primary 색상)
- "비밀번호 찾기" 링크 (Text Secondary)

#### 인터랙션
- **입력 필드 포커스**:
  * 테두리 Primary 색상 강조
  * Label 위로 이동 (Floating Label)
- **로그인 성공**:
  * 로딩 스피너 표시
  * JWT 토큰 저장
  * 00-대시보드로 즉시 이동
- **로그인 실패**:
  * 필드 하단에 에러 메시지 표시
  * "아이디 또는 비밀번호를 확인해주세요"
  * 입력 필드 테두리 Error 색상
- **5회 연속 실패**:
  * 다이얼로그 표시
  * "보안을 위해 30분간 로그인이 제한됩니다"
  * 타이머 표시

#### 반응형 동작
- **Mobile**:
  * 풀 너비 레이아웃
  * 화면 중앙 정렬

- **Tablet/Desktop**:
  * 최대 너비 400px
  * 화면 중앙 정렬
  * 배경 카드 효과

---

### 4.3 02-회원가입

#### 개요
- **목적**: 신규 사용자 등록
- **관련 유저스토리**: UFR-USER-010
- **비즈니스 중요도**: M (중간)

#### 주요 기능
1. 필수 정보 입력
2. 실시간 입력 검증
3. 중복 확인 (아이디, 이메일)
4. 비밀번호 강도 표시
5. 약관 동의

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: "회원가입" (H1)
- 진행 표시기: "1/1" (현재/전체)

**입력 섹션**
1. 이름 입력:
   * Label: "이름 *"
   * Placeholder: "2자 이상 입력"
   * 검증: 2자 이상 한글/영문
   * 에러: "이름은 2자 이상이어야 합니다"

2. 이메일 입력:
   * Label: "이메일 *"
   * Placeholder: "example@email.com"
   * Type: email
   * 우측 버튼: "중복확인"
   * 검증: 이메일 형식
   * 에러: "올바른 이메일 형식이 아닙니다"

3. 연락처 입력:
   * Label: "연락처 *"
   * Placeholder: "010-0000-0000"
   * Type: tel
   * 자동 하이픈 삽입
   * 검증: 휴대폰 번호 형식

4. 아이디 입력:
   * Label: "아이디 *"
   * Placeholder: "5자 이상 영문/숫자"
   * 우측 버튼: "중복확인"
   * 검증: 5자 이상 영문/숫자
   * 에러: "아이디는 5자 이상 영문/숫자 조합이어야 합니다"

5. 비밀번호 입력:
   * Label: "비밀번호 *"
   * Placeholder: "8자 이상 영문/숫자/특수문자"
   * Type: password
   * 표시/숨김 토글
   * 비밀번호 강도 표시기:
     - 약함 (빨강): 8자 미만 또는 단순
     - 보통 (노랑): 8자 이상, 2종류 조합
     - 강함 (초록): 8자 이상, 3종류 조합
   * 검증: 8자 이상, 영문/숫자/특수문자 포함

6. 비밀번호 확인:
   * Label: "비밀번호 확인 *"
   * Placeholder: "비밀번호를 다시 입력하세요"
   * Type: password
   * 검증: 비밀번호와 동일
   * 에러: "비밀번호가 일치하지 않습니다"

**약관 동의**
- 전체 동의 체크박스:
  * Label: "전체 동의"
  * 하위 항목 모두 선택
- 필수 약관 (2개):
  * "이용약관 동의 (필수)" - 링크로 상세 보기
  * "개인정보 처리방침 동의 (필수)" - 링크로 상세 보기
- 선택 약관 (1개):
  * "마케팅 정보 수신 동의 (선택)"

**CTA 버튼**
- "회원가입" 버튼:
  * Primary 색상
  * Full width
  * Height: 48px
  * Disabled: 필수 항목 미입력 또는 필수 약관 미동의

#### 인터랙션
- **실시간 검증**:
  * 각 필드 포커스 아웃 시 검증
  * 통과: 필드 우측에 ✓ 아이콘 (Success 색상)
  * 실패: 필드 하단에 에러 메시지 (Error 색상)

- **중복확인**:
  * 버튼 클릭 시 API 호출
  * 로딩: 버튼에 스피너 표시
  * 성공: "사용 가능한 아이디입니다" (Success 색상)
  * 실패: "이미 사용 중인 아이디입니다" (Error 색상)

- **비밀번호 강도**:
  * 입력 시 실시간 업데이트
  * 프로그레스 바 + 텍스트 레이블

- **회원가입 성공**:
  * "회원가입이 완료되었습니다" 토스트 메시지
  * 01-로그인 화면으로 이동

- **회원가입 실패**:
  * 실패 사유 다이얼로그 표시
  * 해당 필드로 포커스 이동

#### 반응형 동작
- **Mobile**: 풀 너비, 스크롤 가능
- **Tablet/Desktop**: 최대 너비 600px, 중앙 정렬

---

### 4.4 05-본인자산정보입력

#### 개요
- **목적**: 재무 계산을 위한 자산정보 입력
- **관련 유저스토리**: UFR-ASST-010
- **비즈니스 중요도**: M (중간)

#### 주요 기능
1. 복수 자산 항목 등록 (카드 형태)
2. 복수 대출 항목 등록
3. 복수 월소득 항목 등록
4. 복수 월지출 항목 등록
5. 항목별 총액 자동 계산
6. 자동 저장 (3초 debounce)
7. 드래그앤드롭으로 순서 변경

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: "본인 자산정보" (H1)
- 진행 표시기: "3/5"

**섹션 탭** (스크롤 가능)
- 자산 | 대출 | 월소득 | 월지출
- 활성 탭: Primary 색상 하단 바
- 스와이프로 탭 전환 가능

**각 섹션 공통 구조**

*자산 섹션 예시:*

1. 안내 텍스트:
   * "보유하신 자산을 모두 등록해주세요" (Caption, Text Secondary)

2. 추가 버튼 (FAB - Floating Action Button):
   * 위치: 우하단 고정
   * 아이콘: + (24px)
   * 색상: Primary
   * 크기: 56x56px
   * Elevation: Level 2

3. 카드 리스트:
   * 각 카드:
     - 드래그 핸들 아이콘 (좌측)
     - 자산 이름 (H3)
     - 자산 금액 (강조 금액 스타일)
     - 수정 버튼 (아이콘)
     - 삭제 버튼 (아이콘)
   * Empty State:
     - 일러스트 (중앙)
     - "아직 등록된 자산이 없습니다" (Body)
     - "자산 추가" 버튼

4. 총액 표시 (Sticky Footer):
   * 배경: Surface (White)
   * 테두리: Border 색상 상단
   * 레이블: "총 자산" (Caption)
   * 금액: 강조 금액 스타일
   * 자동 계산 및 실시간 업데이트

**하단 시트 (항목 추가/수정 시)**
- 타이틀: "자산 추가" 또는 "자산 수정"
- 닫기 버튼 (X)
- 입력 필드:
  * 자산 이름:
    - Label: "자산 이름"
    - Placeholder: "예: 주택청약저축"
    - Max length: 30자
  * 자산 금액:
    - Label: "자산 금액"
    - Placeholder: "0"
    - Type: number (천 단위 콤마 자동)
    - 숫자 키패드 (모바일)
- CTA:
  * "저장" 버튼 (Primary, Full width)
  * "취소" 버튼 (Secondary, Full width)

**네비게이션 버튼**
- "다음" 버튼:
  * Primary 색상
  * Full width
  * Sticky bottom (스크롤 시에도 고정)
  * Disabled: 최소 1개 자산 또는 소득 미입력

#### 인터랙션
- **FAB 클릭**:
  * 하단 시트 슬라이드업 애니메이션
  * 배경 dim 처리

- **카드 드래그**:
  * 드래그 시작: 카드 Elevation 증가
  * 드래그 중: 다른 카드들 자리 이동
  * 드래그 완료: 순서 저장 (자동 저장)

- **수정 버튼**:
  * 하단 시트 표시 (기존 데이터 채워짐)

- **삭제 버튼**:
  * 확인 다이얼로그: "삭제하시겠습니까?"
  * 확인 선택: 카드 fade-out 애니메이션 후 제거
  * 총액 자동 재계산

- **자동 저장**:
  * 3초 debounce 후 저장
  * 저장 중: 헤더에 작은 스피너
  * 저장 완료: "저장됨" 텍스트 잠깐 표시

- **다음 버튼**:
  * 06-배우자자산정보입력으로 이동

#### 반응형 동작
- **Mobile**:
  * 싱글 컬럼 카드 리스트
  * 하단 시트 전체 높이

- **Tablet**:
  * 카드 2컬럼 그리드
  * 하단 시트 높이 60%

- **Desktop**:
  * 카드 3컬럼 그리드
  * 하단 시트 대신 모달 (max-width: 500px)

---

### 4.5 11-입주희망주택입력-기본정보

#### 개요
- **목적**: 관심 주택의 기본정보 입력 (Step 1/2)
- **관련 유저스토리**: UFR-HOUS-010 (기본정보 부분)
- **비즈니스 중요도**: M (중간)

#### 주요 기능
1. 주택 기본정보 입력
2. 주소 검색 (Kakao API)
3. 입력 검증
4. 임시 저장

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: "주택 등록" (H1)
- 진행 표시기: "1/2"

**폼 섹션**

1. 주택유형 선택:
   * Label: "주택유형 *"
   * 세그먼트 컨트롤:
     - 매매 | 전세 | 월세
     - 선택된 항목: Primary 배경, White 텍스트
     - 미선택: Surface 배경, Text Secondary

2. 입주희망년월:
   * Label: "입주희망년월 *"
   * 날짜 피커 트리거 버튼
   * 형식: "YYYY년 MM월"
   * 클릭 시 날짜 피커 모달

3. 주택이름:
   * Label: "주택이름 *"
   * Placeholder: "예: 반포자이 84㎡"
   * Max length: 50자
   * Counter: "0/50"

4. 위치:
   * Label: "위치 *"
   * 주소 검색 버튼
   * 선택된 주소 표시 (읽기 전용)
   * 변경 버튼

5. 준공년월:
   * Label: "준공년월 *"
   * 날짜 피커 (년월만)
   * 형식: "YYYY년 MM월"

6. 가격:
   * Label: "가격 *"
   * Placeholder: "0"
   * Type: number
   * 천 단위 콤마 자동
   * 단위: "원" (고정 표시)
   * 숫자 키패드 (모바일)

7. 타입:
   * Label: "타입 *"
   * Placeholder: "예: 84㎡, 32평"
   * Max length: 20자

**Sticky CTA**
- "다음" 버튼:
  * Primary 색상
  * Full width
  * Disabled: 필수 항목 미입력
  * 툴팁: "모든 필수 항목을 입력해주세요"

**주소 검색 모달**
- 타이틀: "주소 검색"
- 검색 입력 필드:
  * Placeholder: "도로명, 지번, 건물명 검색"
  * 검색 버튼
- 검색 결과 리스트:
  * 각 항목: 도로명 주소, 지번 주소
  * 클릭 시 선택 및 모달 닫기
- 닫기 버튼

#### 인터랙션
- **주택유형 선택**:
  * 탭 시 즉시 선택
  * 애니메이션: 배경 색상 전환

- **날짜 피커**:
  * 트리거 버튼 클릭: 모달 표시
  * 년월 선택 UI (스크롤 휠 또는 드롭다운)
  * 확인: 선택 적용 및 모달 닫기

- **주소 검색**:
  * 검색 버튼 클릭: Kakao 주소 검색 API 모달
  * 주소 선택: 입력 필드에 자동 입력
  * 에러 처리: "주소 검색에 실패했습니다. 다시 시도해주세요"

- **필수 항목 검증**:
  * 실시간 검증 (포커스 아웃)
  * 미입력 시: 필드 하단에 "필수 항목입니다" (Error 색상)
  * 모두 입력 시: "다음" 버튼 활성화

- **다음 버튼**:
  * 클릭: 12-입주희망주택입력-상세정보로 이동
  * 데이터 임시 저장 (LocalStorage)

#### 반응형 동작
- **Mobile**: 풀 너비 레이아웃
- **Tablet/Desktop**: 최대 너비 600px, 중앙 정렬

---

### 4.6 13-입주희망주택목록

#### 개요
- **목적**: 등록한 주택 조회 및 비교
- **관련 유저스토리**: UFR-HOUS-020
- **비즈니스 중요도**: S (낮음)

#### 주요 기능
1. 주택 목록 표시
2. 필터 및 정렬
3. 최종목표 주택 선택
4. 주택 비교 (최대 3개)
5. 주택 삭제 (스와이프)

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: "내 주택 목록" (H1)
- 필터 아이콘 버튼
- 정렬 아이콘 버튼

**필터/정렬 바**
- 주택유형 칩:
  * 전체 | 매매 | 전세 | 월세
  * 선택된 칩: Primary 배경, White 텍스트
  * 미선택: Border, Text Secondary
- 정렬 드롭다운:
  * 최근 등록순
  * 입주희망년월순
  * 가격 높은 순
  * 가격 낮은 순

**주택 카드 그리드**

각 카드 구성:
- 주택이름 (H3)
- 주택유형 뱃지:
  * 매매: Primary 배경
  * 전세: Secondary 배경
  * 월세: Warning 배경
  * 크기: Small
  * 위치: 우상단
- 위치 (Caption):
  * 아이콘: 📍
  * 간략 주소 (예: "서울 강남구 역삼동")
- 가격 (강조 금액 스타일)
- 입주희망년월 (Caption, Text Secondary)
- 타입 (Caption)
- 최종목표 별 아이콘:
  * 위치: 좌상단
  * 미선택: ☆ (Border)
  * 선택: ★ (Warning 색상)
- 비교 체크박스:
  * 위치: 좌하단
  * 최대 3개 선택 제한

**Empty State**
- 일러스트 (중앙)
- "아직 등록한 주택이 없습니다" (H3)
- "첫 주택을 등록하고 계획을 시작하세요" (Body, Text Secondary)
- "주택 등록하기" 버튼 (Primary)

**FAB (Floating Action Button)**
- 위치: 우하단 고정
- 아이콘: + (24px)
- 색상: Primary
- 크기: 56x56px
- 클릭: 11-입주희망주택입력

**하단 액션 바** (2개 이상 선택 시 표시)
- 배경: Surface (White)
- 테두리: Border 색상 상단
- "비교하기" 버튼 (Primary, Full width)
- 선택 개수 표시: "2개 선택됨"

#### 인터랙션
- **카드 클릭**:
  * 14-입주희망주택상세로 이동
  * 슬라이드 전환 애니메이션

- **별 아이콘 클릭**:
  * 확인 다이얼로그: "이 주택을 최종목표로 설정하시겠습니까?"
  * 기존 최종목표 있는 경우: "기존 최종목표가 해제됩니다"
  * 확인: 별 아이콘 채워짐 (★)
  * 기존 최종목표 카드 별 아이콘 비워짐 (☆)

- **체크박스 선택**:
  * 최대 3개 제한
  * 3개 초과 시: "최대 3개까지 선택 가능합니다" 토스트
  * 2개 이상 선택: 하단 액션 바 슬라이드업

- **스와이프 삭제** (모바일):
  * 좌측 스와이프: 삭제 버튼 표시 (빨강 배경)
  * 삭제 버튼 클릭: 확인 다이얼로그
  * 확인: 카드 슬라이드 아웃 애니메이션 후 제거

- **필터/정렬**:
  * 칩 클릭: 즉시 필터 적용
  * 정렬 변경: 즉시 재정렬

- **비교하기**:
  * 비교 화면으로 이동 (별도 화면)
  * 선택된 주택 정보 테이블 형태로 비교

#### 반응형 동작
- **Mobile (320-767px)**:
  * 1컬럼 카드 리스트
  * 카드 간격: md (16px)

- **Tablet (768-1023px)**:
  * 2컬럼 그리드
  * 카드 간격: lg (24px)

- **Desktop (1024px+)**:
  * 3컬럼 그리드
  * 카드 간격: lg (24px)
  * 호버 시: Elevation 증가 + 테두리 Primary

---

### 4.7 15-입주후지출계산

#### 개요
- **목적**: 재무 계산 및 대출 충족 여부 확인
- **관련 유저스토리**: UFR-CALC-010
- **비즈니스 중요도**: M (중간)

#### 주요 기능
1. 대출상품 선택
2. 입주 후 지출 계산
3. LTV/DTI/DSR 계산 및 표시
4. 대출 충족 여부 판단
5. 결과 저장 및 공유

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: [주택이름] (H1)
- 서브타이틀: "입주 후 지출 계산" (Caption)

**입력 섹션**

1. 대출상품 선택:
   * Label: "대출상품 선택 *"
   * 드롭다운 또는 카드 선택 UI
   * Placeholder: "대출상품을 선택하세요"

2. 선택된 대출상품 요약 카드:
   * 대출이름 (H3)
   * 주요 정보:
     - 대출한도
     - 금리
     - LTV/DTI/DSR 한도
   * 접기/펼치기 가능

**계산 버튼**
- "계산하기" 버튼:
  * Primary 색상
  * Full width
  * Height: 48px
  * Disabled: 대출상품 미선택

**결과 섹션** (계산 완료 후 표시)

1. 재무 현황 카드:
   * 타이틀: "재무 현황" (H2)
   * 항목:
     - 입주희망년월: [YYYY년 MM월]
     - 예상자산: [금액]
     - 대출필요 금액: [금액] (강조)

2. 대출 분석 카드:
   * 타이틀: "대출 분석" (H2)
   * 실행 대출: [대출이름]
   * LTV 게이지 바:
     - 레이블: "LTV"
     - 현재값: [계산된 LTV]%
     - 한도: [대출상품 LTV 한도]%
     - 게이지: 충족 시 Success, 미충족 시 Error
   * DTI 게이지 바: (동일 구조)
   * DSR 게이지 바: (동일 구조)
   * 충족여부 배지:
     - 충족: ✅ "이 대출상품을 이용할 수 있습니다" (Success 배경)
     - 미충족: ❌ "이 대출상품 기준을 충족하지 못합니다" (Error 배경)
   * 미충족 시 상세 사유:
     - "LTV가 5% 초과합니다"
     - "DTI가 10% 초과합니다"

3. 입주 후 재무상태 카드:
   * 타이틀: "입주 후 예상" (H2)
   * 항목:
     - 예상 자산: [금액]
     - 예상 월지출액: [금액]
     - 여유자금: [금액] (강조 금액 스타일)
       * 양수: Success 색상
       * 음수: Error 색상

**하단 액션**
- "다른 대출로 재계산" 버튼 (Secondary)
- "결과 저장" 버튼 (Primary)
- "공유하기" 버튼 (아이콘)

#### 인터랙션
- **대출상품 선택**:
  * 드롭다운 클릭: 대출상품 목록 표시
  * 선택: 요약 카드 표시
  * 계산 버튼 활성화

- **계산 중**:
  * 로딩 스피너 + 진행 메시지
  * "최적의 대출 조건을 찾고 있습니다..."
  * 약 2-3초 소요

- **결과 표시**:
  * 결과 섹션 fade-in 애니메이션
  * 순차적 표시 (재무 현황 → 대출 분석 → 입주 후)
  * 게이지 바 애니메이션 (0에서 값까지)

- **다른 대출로 재계산**:
  * 대출상품 선택 섹션으로 스크롤
  * 이전 결과 유지 (비교 가능)

- **결과 저장**:
  * 로딩 스피너
  * 성공: "계산 결과가 저장되었습니다" 토스트
  * 실패: 에러 메시지

- **공유하기**:
  * 공유 시트 표시
  * 옵션: 이미지 다운로드, 카카오톡, 링크 복사

#### 반응형 동작
- **Mobile**:
  * 싱글 컬럼 레이아웃
  * 카드 간격: lg (24px)

- **Tablet/Desktop**:
  * 재무 현황 + 대출 분석: 좌측 (60%)
  * 입주 후 재무상태: 우측 (40%)
  * 2컬럼 레이아웃

---

### 4.8 18-장기주거로드맵조회

#### 개요
- **목적**: AI 생성 장기주거 로드맵 확인
- **관련 유저스토리**: UFR-ROAD-030
- **비즈니스 중요도**: S (낮음)

#### 주요 기능
1. 타임라인 형태 로드맵 표시
2. 단계별 상세정보 조회
3. 생애주기 이벤트 표시
4. 실행가이드 제공
5. 로드맵 수정 요청
6. PDF 다운로드/공유

#### UI 구성요소

**헤더**
- 뒤로 가기 버튼
- 타이틀: "장기주거 로드맵" (H1)
- 메뉴 아이콘 (수정, 공유, 다운로드)

**타임라인 뷰** (수평 스크롤)

- 타임라인 축:
  * 좌측: 현재 (고정)
  * 우측: 최종목표까지
  * 년도 표시 (5년 단위)

- 현재 위치 마커:
  * "현재" 레이블
  * 아이콘: 📍 (Primary 색상)
  * 수직선

- 단계별 카드:
  * 카드 크기: 240px x 180px
  * 카드 간격: md (16px)
  * 각 카드:
    - 단계명 (H3): "1단계 - 신혼집"
    - 예상 입주시기: "2026년 3월"
    - 예상 가격: [금액] (강조)
    - 주택 타입: "전세 60㎡"
    - 아이콘/이미지 (상단)
  * 선택된 카드: Primary 테두리, Elevation 증가

- 생애주기 이벤트 마커:
  * 위치: 타임라인 상단
  * 각 이벤트:
    - 아이콘 (이벤트 유형별)
    - 이벤트명: "결혼", "출산", "자녀교육"
    - 예정일
  * 연결선: 해당 단계와 연결

**선택된 단계 상세 패널** (하단 시트)

- 타이틀: [단계명] (H2)
- 닫기 버튼

- 추천 주택 특징:
  * 위치: [지역]
  * 타입: [평형]
  * 예상 가격: [금액]
  * 주요 고려사항: [텍스트]

- 재무 목표:
  * 필요 자금: [금액]
  * 저축 목표: [금액]
  * 예상 대출: [금액]

- 실행 전략:
  * 월별 저축액: [금액]
  * 투자 방향: [텍스트]
  * 주의사항: [텍스트]

**실행가이드 섹션**

- 타이틀: "현재 실행가이드" (H2)
- 월별 저축 플랜:
  * 목표 저축액: [금액]/월
  * 프로그레스 바:
    - 현재 진행률: [%]
    - 레이블: "목표 대비 [%] 달성"
- 다음 단계까지:
  * D-[일수] 또는 [개월] 남음
  * 프로그레스 바

**하단 액션 버튼**
- "로드맵 수정" 버튼 (Secondary)
- "PDF 다운로드" 버튼 (Secondary)
- "공유하기" 버튼 (Primary)

#### 인터랙션
- **타임라인 스크롤**:
  * 수평 스크롤 (좌우)
  * 자동 스냅: 각 단계 카드 중앙 정렬
  * 현재 위치 고정 (좌측)

- **단계 카드 클릭**:
  * 하단 시트 슬라이드업 애니메이션
  * 배경 dim 처리
  * 카드 테두리 Primary 강조

- **생애주기 이벤트 클릭**:
  * 툴팁 표시: 이벤트 상세 정보
  * 연결된 단계로 자동 스크롤

- **하단 시트 스와이프**:
  * 아래로 스와이프: 시트 닫기
  * 위로 스와이프: 전체 화면 확장

- **로드맵 수정**:
  * 17-생애주기이벤트관리로 이동
  * 수정 후 재생성 옵션

- **PDF 다운로드**:
  * 로딩 스피너
  * 다운로드 완료: "다운로드가 완료되었습니다" 토스트
  * 파일명: "장기주거로드맵_[날짜].pdf"

- **공유하기**:
  * 공유 시트: 카카오톡, 링크 복사, 이미지 저장

#### 반응형 동작
- **Mobile**:
  * 타임라인: 수평 스크롤
  * 하단 시트: 전체 높이 70%

- **Tablet**:
  * 타임라인: 수평 스크롤 (카드 크기 증가)
  * 하단 시트: 높이 60%

- **Desktop**:
  * 좌측: 타임라인 (70%)
  * 우측: 상세 패널 (30%, 고정)
  * 하단 시트 대신 우측 패널 사용

---

## 5. 공통 컴포넌트

### 5.1 하단 탭 네비게이션 (Mobile)

#### 구조
- **위치**: 화면 하단 고정
- **높이**: 56px (안전 영역 + 패딩)
- **배경**: Surface (White)
- **테두리**: Border 색상 상단

#### 탭 항목 (5개)
1. **홈**
   - 아이콘: 🏠 (24px)
   - 레이블: "홈"
   - 이동: 00-대시보드

2. **주택**
   - 아이콘: 🏡 (24px)
   - 레이블: "주택"
   - 이동: 13-입주희망주택목록

3. **계산**
   - 아이콘: 💰 (24px)
   - 레이블: "계산"
   - 이동: 15-입주후지출계산

4. **로드맵**
   - 아이콘: 🗺️ (24px)
   - 레이블: "로드맵"
   - 이동: 18-장기주거로드맵조회

5. **더보기**
   - 아이콘: ☰ (24px)
   - 레이블: "더보기"
   - 이동: 메뉴 화면

#### 상태
- **활성 탭**:
  * 아이콘: Primary 색상
  * 레이블: Primary 색상, Semibold
  * 하단 바: Primary 색상 (2px)

- **비활성 탭**:
  * 아이콘: Text Secondary
  * 레이블: Text Secondary, Regular

#### 인터랙션
- **탭 전환**:
  * 즉시 화면 전환
  * 슬라이드 애니메이션 (좌우)
  * 활성 탭 강조

### 5.2 헤더 (공통)

#### 구조
- **높이**: 56px
- **배경**: Surface (White) 또는 투명 (스크롤 시 변경)
- **테두리**: Border 색상 하단 (스크롤 시 표시)

#### 레이아웃
- **좌측**:
  * 뒤로 가기 버튼 또는 메뉴 버튼
  * 크기: 44x44px (터치 타겟)

- **중앙**:
  * 타이틀 (H1 또는 H2)
  * 서브타이틀 (옵션)

- **우측**:
  * 액션 버튼 (최대 2개)
  * 크기: 44x44px

#### 상태
- **기본**:
  * 배경 투명 또는 Surface
  * 타이틀 Text Primary

- **스크롤 시** (Sticky Header):
  * 배경 Surface
  * Elevation Level 1
  * 테두리 표시

### 5.3 카드 (공통)

#### 기본 카드
- **배경**: Surface (White)
- **테두리**: 없음 또는 Border (1px)
- **Border Radius**: Large (12px)
- **Elevation**: Level 1
- **패딩**: md (16px)

#### 상태
- **기본**: Elevation Level 1
- **호버** (Desktop): Elevation Level 2, 테두리 Primary
- **선택**: 테두리 Primary (2px)
- **비활성**: 투명도 60%

### 5.4 버튼 (공통)

#### Primary 버튼
- **배경**: Primary 색상
- **텍스트**: White
- **Border Radius**: Medium (8px)
- **높이**: 48px (모바일), 44px (데스크톱)
- **패딩**: 좌우 24px
- **Hover**: Primary Hover 색상
- **Disabled**: Primary Disabled 색상, 텍스트 투명도 60%

#### Secondary 버튼
- **배경**: 투명
- **텍스트**: Primary 색상
- **테두리**: Primary 색상 (1px)
- **Border Radius**: Medium (8px)
- **높이**: 48px (모바일), 44px (데스크톱)
- **Hover**: 배경 Primary 색상 (투명도 10%)

#### 텍스트 버튼
- **배경**: 투명
- **텍스트**: Primary 색상
- **패딩**: 좌우 16px
- **Hover**: 텍스트 Primary Hover 색상

### 5.5 입력 필드 (공통)

#### 기본 구조
- **Label**: Caption, Text Secondary (상단 또는 Floating)
- **입력 필드**:
  * 배경: Surface
  * 테두리: Border 색상 (1px)
  * Border Radius: Medium (8px)
  * 높이: 48px
  * 패딩: 좌우 16px
  * 폰트: Body

#### 상태
- **기본**: 테두리 Border 색상
- **포커스**: 테두리 Primary 색상 (2px), Label Primary 색상
- **에러**: 테두리 Error 색상, 하단 에러 메시지 표시
- **성공**: 우측에 ✓ 아이콘 (Success 색상)
- **Disabled**: 배경 투명도 60%, 텍스트 투명도 60%

#### Placeholder
- 색상: Text Secondary
- 투명도: 60%

### 5.6 모달/다이얼로그 (공통)

#### 모달 (Mobile: 하단 시트)
- **배경**: Surface
- **Border Radius**: 상단만 XLarge (16px)
- **높이**: 최대 70%
- **Elevation**: Level 3
- **핸들**: 상단 중앙에 작은 회색 바 (드래그 힌트)

#### 모달 (Tablet/Desktop: 센터 모달)
- **배경**: Surface
- **Border Radius**: XLarge (16px)
- **최대 너비**: 600px
- **최대 높이**: 80vh
- **Elevation**: Level 3
- **배경 Dim**: rgba(0, 0, 0, 0.5)

#### 구조
- **헤더**:
  * 타이틀 (H2)
  * 닫기 버튼 (우측)

- **컨텐츠**:
  * 스크롤 가능
  * 패딩: lg (24px)

- **푸터** (옵션):
  * 액션 버튼
  * 패딩: md (16px)
  * 배경: Surface
  * 테두리: Border 색상 상단

### 5.7 Empty State (공통)

#### 구조
- 일러스트 또는 아이콘 (128px)
- 메시지 (H3)
- 설명 (Body, Text Secondary)
- CTA 버튼 (Primary)

#### 레이아웃
- 중앙 정렬
- 패딩: xl (32px)
- 최대 너비: 400px

#### 예시
```
[일러스트]

아직 등록한 주택이 없습니다

첫 주택을 등록하고
내집마련 계획을 시작하세요

[주택 등록하기 버튼]
```

### 5.8 로딩 상태 (공통)

#### 스켈레톤 로딩
- 카드 윤곽 표시
- 배경: Background 색상
- 애니메이션: 좌→우 그라데이션 이동
- 사용: 초기 데이터 로딩

#### 스피너
- 크기: 24px (인라인), 48px (전체 화면)
- 색상: Primary
- 애니메이션: 회전
- 사용: 버튼 클릭, 제출 등

#### 프로그레스 바
- 높이: 4px
- 배경: Border 색상
- 진행: Primary 색상
- 사용: 파일 업로드, 로드맵 생성

---

## 6. 화면 간 전환 및 네비게이션

### 6.1 전환 패턴

#### Mobile
- **기본 전환**: Slide (좌→우 진입, 우→좌 퇴장)
- **모달**: Slide Up (하단에서 상승)
- **탭 전환**: Fade
- **애니메이션 시간**: 300ms
- **Easing**: ease-in-out

#### Tablet/Desktop
- **기본 전환**: Fade
- **모달**: Scale + Fade (중앙에서 확대)
- **사이드 패널**: Slide (좌측에서 진입)
- **애니메이션 시간**: 200ms

### 6.2 네비게이션 계층

```
Level 1: 하단 탭 네비게이션 (00, 13, 15, 18, 더보기)
  └─ Level 2: 주요 기능 화면
      └─ Level 3: 상세/입력 화면
          └─ Level 4: 모달/다이얼로그
```

### 6.3 브레드크럼 (Desktop만)

- **위치**: 헤더 하단
- **형식**: 홈 > 주택 관리 > 주택 목록
- **인터랙션**: 각 항목 클릭 가능

---

## 7. 반응형 설계 전략

### 7.1 브레이크포인트

```css
/* Mobile */
@media (min-width: 320px) and (max-width: 767px)

/* Tablet */
@media (min-width: 768px) and (max-width: 1023px)

/* Desktop */
@media (min-width: 1024px)
```

### 7.2 레이아웃 전략

#### Mobile (320-767px) - 우선순위
- **싱글 컬럼** 레이아웃
- **하단 탭** 네비게이션
- **풀 너비** 버튼 및 입력 필드
- **스택 카드** 레이아웃 (세로 배치)
- **터치 최적화**: 최소 44x44px 타겟 크기
- **하단 시트** 모달
- **햄버거 메뉴** (필요시)

#### Tablet (768-1023px) - 점진적 향상
- **2컬럼** 레이아웃 (일부 화면)
- **사이드 네비게이션** (드로어) + 하단 탭 유지
- **카드 그리드**: 2열
- **모달 크기 조정**: 센터 정렬, max-width 600px
- **일부 패널** 형태로 표시
- **마우스 호버** 상태 추가

#### Desktop (1024px+) - 최대 활용
- **3컬럼** 레이아웃 (대시보드, 목록 화면)
- **고정 사이드바** 네비게이션
- **카드 그리드**: 3-4열
- **모달 대신 사이드 패널** 옵션
- **마스터-디테일** 레이아웃 (목록 + 상세)
- **키보드 단축키** 지원
- **드래그 앤 드롭** 향상

### 7.3 컴포넌트별 반응형 전략

#### 네비게이션
| 디바이스 | 구조 |
|----------|------|
| Mobile | 하단 탭 (5개 항목) |
| Tablet | 좌측 드로어 + 하단 탭 |
| Desktop | 고정 사이드바 (확장형) |

#### 카드 그리드
| 디바이스 | 컬럼 수 | 간격 |
|----------|---------|------|
| Mobile | 1열 | md (16px) |
| Tablet | 2열 | lg (24px) |
| Desktop | 3-4열 | lg (24px) |

#### 폼 입력
| 디바이스 | 너비 | 정렬 |
|----------|------|------|
| Mobile | 풀 너비 | - |
| Tablet | max-width 600px | 중앙 정렬 |
| Desktop | max-width 600px | 중앙 정렬 |

#### 모달/다이얼로그
| 디바이스 | 형태 | 위치 |
|----------|------|------|
| Mobile | 하단 시트 또는 풀스크린 | 하단 |
| Tablet | 센터 모달 (max-width) | 중앙 |
| Desktop | 센터 모달 또는 사이드 패널 | 중앙/우측 |

### 7.4 이미지 반응형

- **srcset 사용**: 디바이스별 최적 이미지
- **sizes 속성**: 레이아웃에 따른 크기 지정
- **WebP 포맷**: 용량 최적화 (fallback: JPEG/PNG)
- **Lazy Loading**: 뷰포트 진입 시 로드

```html
<img
  srcset="image-320w.webp 320w,
          image-640w.webp 640w,
          image-1280w.webp 1280w"
  sizes="(max-width: 767px) 100vw,
         (max-width: 1023px) 50vw,
         33vw"
  src="image-640w.webp"
  alt="주택 이미지"
  loading="lazy"
/>
```

### 7.5 폰트 크기 반응형

- **상대 단위 사용**: rem, em
- **기본 폰트 크기**: 16px (1rem)
- **Mobile**: 기본 스케일
- **Tablet**: 1.05배 (선택적)
- **Desktop**: 1.1배 (선택적)

```css
:root {
  --font-size-h1: 1.75rem; /* 28px */
  --font-size-h2: 1.375rem; /* 22px */
  --font-size-body: 1rem; /* 16px */
}

@media (min-width: 1024px) {
  :root {
    --font-size-h1: 1.925rem; /* ~31px */
    --font-size-h2: 1.5125rem; /* ~24px */
  }
}
```

---

## 8. 접근성 보장 방안

### 8.1 WCAG 2.1 AA 준수

#### 1. 인식 가능성 (Perceivable)

**대체 텍스트**
- 모든 이미지, 아이콘에 `alt` 속성 제공
- 장식적 이미지: `alt=""` (빈 문자열)
- 의미있는 이미지: 구체적 설명

```html
<!-- 좋은 예 -->
<img src="house.png" alt="강남구 역삼동 아파트 전경" />
<img src="decorative.png" alt="" role="presentation" />

<!-- 나쁜 예 -->
<img src="house.png" alt="이미지" />
```

**색상 의존 금지**
- 정보를 색상만으로 전달하지 않음
- 오류: 색상 + 아이콘 + 텍스트 조합

```html
<!-- 좋은 예 -->
<div class="error">
  <span class="icon">❌</span>
  <span>필수 항목입니다</span>
</div>

<!-- 나쁜 예 -->
<div style="color: red;">필수 항목입니다</div>
```

**명료한 대비**
- 텍스트 대비율: 최소 4.5:1
- 큰 텍스트 (18px+): 최소 3:1
- UI 컴포넌트: 최소 3:1

**텍스트 크기 조절**
- 200%까지 확대 가능
- 상대 단위 사용 (rem, em)
- 레이아웃 깨지지 않음

**콘텐츠 구조**
- 의미론적 HTML 사용

```html
<header>
  <nav>
    <ul>
      <li><a href="/">홈</a></li>
    </ul>
  </nav>
</header>

<main>
  <article>
    <h1>대시보드</h1>
    <section>
      <h2>재무 현황</h2>
    </section>
  </article>
</main>

<footer>
  <!-- 푸터 내용 -->
</footer>
```

#### 2. 운용 가능성 (Operable)

**키보드 접근성**
- 모든 기능 키보드로 조작 가능
- Tab 키로 순차 이동
- Enter/Space로 활성화
- Esc로 모달 닫기

**포커스 표시**
- 명확한 포커스 링 (outline)
- 최소 2px, Primary 색상
- `:focus-visible` 사용

```css
button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}
```

**터치 타겟**
- 최소 크기: 44x44px
- 간격: 최소 8px

**충분한 시간**
- 타임아웃 경고: 1분 전
- 연장 옵션 제공
- 자동 로그아웃: 30분 (조정 가능)

**발작 예방**
- 깜빡임 효과: 3Hz 이하
- 자동 재생 비디오: 5초 이하

**탐색 지원**
- Skip to content 링크
- 일관된 네비게이션
- 명확한 페이지 타이틀

```html
<a href="#main-content" class="skip-link">
  본문으로 건너뛰기
</a>

<main id="main-content">
  <!-- 콘텐츠 -->
</main>
```

#### 3. 이해 가능성 (Understandable)

**명확한 레이블**
- 모든 입력 필드에 `<label>` 연결
- `aria-label` 사용 (레이블 없을 때)

```html
<!-- 좋은 예 -->
<label for="email">이메일</label>
<input id="email" type="email" />

<!-- aria-label 사용 -->
<button aria-label="메뉴 열기">
  <span class="icon">☰</span>
</button>
```

**오류 식별**
- 구체적 오류 메시지
- 수정 방법 제안
- `aria-invalid`, `aria-describedby`

```html
<label for="password">비밀번호</label>
<input
  id="password"
  type="password"
  aria-invalid="true"
  aria-describedby="password-error"
/>
<span id="password-error" class="error">
  비밀번호는 8자 이상이어야 합니다
</span>
```

**일관성**
- 동일 기능 = 동일 아이콘/위치
- 네비게이션 일관성
- 버튼 스타일 일관성

**도움말**
- 복잡한 입력: 힌트 텍스트
- `placeholder` + `aria-describedby`

```html
<label for="price">가격</label>
<input
  id="price"
  type="text"
  placeholder="1,000,000"
  aria-describedby="price-hint"
/>
<span id="price-hint" class="hint">
  천 단위로 콤마가 자동 입력됩니다
</span>
```

**언어 명시**
- `<html lang="ko">`

#### 4. 견고성 (Robust)

**시맨틱 HTML**
- 적절한 HTML5 요소 사용
- `<button>` vs `<div onClick>`
- `<nav>`, `<main>`, `<article>` 등

**ARIA 레이블**
- 복잡한 UI: `role`, `aria-label`
- 적절한 ARIA 속성 사용

```html
<!-- 탭 UI -->
<div role="tablist">
  <button
    role="tab"
    aria-selected="true"
    aria-controls="panel-1"
  >
    자산
  </button>
</div>
<div
  role="tabpanel"
  id="panel-1"
  aria-labelledby="tab-1"
>
  <!-- 콘텐츠 -->
</div>
```

**폼 검증**
- `aria-invalid="true/false"`
- `aria-describedby`: 에러 메시지 연결
- `required` 속성

**스크린 리더 지원**
- 동적 콘텐츠 변경: `aria-live`
- 로딩 상태: `aria-busy="true"`

```html
<!-- 로딩 중 -->
<div aria-live="polite" aria-busy="true">
  데이터를 불러오는 중입니다...
</div>

<!-- 성공 메시지 -->
<div role="alert" aria-live="assertive">
  저장이 완료되었습니다
</div>
```

### 8.2 구체적 구현 가이드

#### 버튼
```html
<button aria-label="메뉴 열기">
  <span class="icon" aria-hidden="true">☰</span>
</button>
```

#### 입력 폼
```html
<div class="form-group">
  <label for="username">아이디 *</label>
  <input
    id="username"
    type="text"
    required
    aria-required="true"
    aria-invalid="false"
    aria-describedby="username-hint username-error"
  />
  <span id="username-hint" class="hint">
    5자 이상 영문/숫자 조합
  </span>
  <span id="username-error" class="error" hidden>
    아이디는 5자 이상이어야 합니다
  </span>
</div>
```

#### 모달
```html
<div
  role="dialog"
  aria-modal="true"
  aria-labelledby="modal-title"
>
  <h2 id="modal-title">주택 추가</h2>
  <!-- 모달 콘텐츠 -->
  <button aria-label="닫기">×</button>
</div>
```

#### 네비게이션
```html
<nav role="navigation" aria-label="주요 메뉴">
  <ul>
    <li>
      <a href="/" aria-current="page">홈</a>
    </li>
    <li>
      <a href="/housing">주택</a>
    </li>
  </ul>
</nav>
```

#### 로딩/에러
```html
<!-- 로딩 -->
<div role="status" aria-live="polite">
  <span class="sr-only">로딩 중...</span>
  <div class="spinner" aria-hidden="true"></div>
</div>

<!-- 에러 -->
<div role="alert" aria-live="assertive">
  오류가 발생했습니다. 다시 시도해주세요.
</div>
```

### 8.3 스크린 리더 전용 텍스트

```css
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
```

```html
<button>
  <span class="sr-only">장바구니에 추가</span>
  <span class="icon" aria-hidden="true">🛒</span>
</button>
```

---

## 9. 성능 최적화 방안

### 9.1 초기 로딩 성능

#### Code Splitting (라우트 기반)
- **전략**: 라우트별 청크 분리
- **초기 번들**: 로그인, 회원가입만
- **Lazy Loading**: 나머지 화면 필요 시 로드

```javascript
// React Router 예시
const Dashboard = lazy(() => import('./pages/Dashboard'));
const HousingList = lazy(() => import('./pages/HousingList'));

<Suspense fallback={<Loading />}>
  <Routes>
    <Route path="/" element={<Dashboard />} />
    <Route path="/housing" element={<HousingList />} />
  </Routes>
</Suspense>
```

#### Critical CSS
- **인라인 CSS**: 초기 뷰포트 스타일만
- **지연 로드**: 나머지 CSS

```html
<head>
  <style>
    /* Critical CSS (인라인) */
    body { font-family: Pretendard; }
    .header { height: 56px; }
  </style>
  <link rel="preload" href="styles.css" as="style" onload="this.rel='stylesheet'">
</head>
```

#### 이미지 최적화
1. **포맷**: WebP (fallback: JPEG/PNG)
2. **반응형**: srcset, sizes
3. **Lazy Loading**: `loading="lazy"`
4. **적절한 크기**: 디바이스별 최적화
5. **압축**: TinyPNG, ImageOptim

```html
<img
  srcset="image-320w.webp 320w,
          image-640w.webp 640w"
  sizes="(max-width: 767px) 100vw, 50vw"
  src="image-640w.webp"
  alt="주택"
  loading="lazy"
/>
```

#### 폰트 최적화
1. **포맷**: WOFF2 (최우선)
2. **font-display**: swap (FOIT 방지)
3. **서브셋**: 한글 최적화 (용량 50% 감소)
4. **가변 폰트**: 여러 웨이트를 하나로

```css
@font-face {
  font-family: 'Pretendard';
  src: url('pretendard-subset.woff2') format('woff2');
  font-display: swap;
  font-weight: 100 900;
}
```

### 9.2 런타임 성능

#### 가상 스크롤
- **사용처**: 긴 리스트 (주택 목록, 자산 목록)
- **라이브러리**: react-window, react-virtualized
- **이점**: 수백 개 항목도 부드러운 스크롤

```javascript
import { FixedSizeList } from 'react-window';

<FixedSizeList
  height={600}
  itemCount={1000}
  itemSize={120}
  width="100%"
>
  {Row}
</FixedSizeList>
```

#### 디바운싱/쓰로틀링
- **검색 입력**: 300ms debounce
- **스크롤 이벤트**: throttle
- **자동 저장**: 3초 debounce

```javascript
// Debounce
const debouncedSearch = debounce((value) => {
  searchAPI(value);
}, 300);

// Throttle
const throttledScroll = throttle(() => {
  handleScroll();
}, 100);
```

#### 메모이제이션
- **React.memo**: 불필요한 리렌더 방지
- **useMemo**: 계산 결과 캐싱
- **useCallback**: 함수 재생성 방지

```javascript
const ExpensiveComponent = React.memo(({ data }) => {
  // ...
});

const memoizedValue = useMemo(() => {
  return computeExpensiveValue(a, b);
}, [a, b]);

const memoizedCallback = useCallback(() => {
  doSomething(a, b);
}, [a, b]);
```

#### 상태 관리 최적화
- **지역 상태 우선**: useState
- **전역 상태 최소화**: Zustand
- **셀렉터 메모이제이션**

```javascript
// Zustand 스토어
const useStore = create((set) => ({
  assets: [],
  addAsset: (asset) => set((state) => ({
    assets: [...state.assets, asset]
  }))
}));

// 셀렉터 사용
const totalAssets = useStore(
  useCallback(
    (state) => state.assets.reduce((sum, a) => sum + a.amount, 0),
    []
  )
);
```

### 9.3 네트워크 성능

#### API 최적화
1. **GraphQL** 또는 **REST with field selection**
2. **응답 gzip 압축**
3. **HTTP/2** 사용
4. **배치 요청**: 여러 API를 하나로

```javascript
// Field selection
GET /api/housing?fields=id,name,price

// 배치 요청
POST /api/batch
{
  "requests": [
    { "url": "/api/user", "method": "GET" },
    { "url": "/api/assets", "method": "GET" }
  ]
}
```

#### 캐싱 전략
1. **정적 자산**: 장기 캐시 (1년)
2. **API 응답**: SWR (stale-while-revalidate)
3. **주소 검색**: 메모리 캐시

```javascript
// SWR 패턴
import useSWR from 'swr';

const { data, error } = useSWR('/api/housing', fetcher, {
  revalidateOnFocus: false,
  revalidateOnReconnect: false,
  dedupingInterval: 60000 // 1분
});
```

#### 프리페칭
- **다음 화면** 데이터 미리 로드
- **Link hover** 시 프리페치
- **Intersection Observer**: 뷰포트 진입 시

```javascript
// Next.js Link prefetch
<Link href="/housing" prefetch>
  주택 보기
</Link>

// Manual prefetch
const prefetchData = () => {
  queryClient.prefetchQuery('housing', fetchHousing);
};

<Link onMouseEnter={prefetchData}>
  주택 보기
</Link>
```

#### 오프라인 지원
- **Service Worker**: 정적 자산 캐싱
- **IndexedDB**: 중요 데이터 로컬 저장
- **Background Sync**: 오프라인 작업 동기화

```javascript
// Service Worker 등록
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/sw.js');
}

// IndexedDB 저장
import { openDB } from 'idb';

const db = await openDB('my-db', 1, {
  upgrade(db) {
    db.createObjectStore('assets');
  }
});

await db.put('assets', asset, asset.id);
```

### 9.4 렌더링 성능

#### 애니메이션 최적화
- **CSS transform/opacity**: GPU 가속
- **requestAnimationFrame**: JS 애니메이션
- **will-change**: 신중히 사용

```css
/* 좋은 예 - GPU 가속 */
.card {
  transition: transform 0.3s;
}
.card:hover {
  transform: translateY(-4px);
}

/* 나쁜 예 - 레이아웃 변경 */
.card:hover {
  margin-top: -4px;
}
```

#### 레이아웃 쓰래싱 방지
- **읽기/쓰기 분리**
- **getBoundingClientRect 최소화**
- **IntersectionObserver 사용**

```javascript
// 나쁜 예 - 레이아웃 쓰래싱
elements.forEach(el => {
  const height = el.offsetHeight; // 읽기
  el.style.height = height + 10 + 'px'; // 쓰기
});

// 좋은 예 - 읽기/쓰기 분리
const heights = elements.map(el => el.offsetHeight);
elements.forEach((el, i) => {
  el.style.height = heights[i] + 10 + 'px';
});
```

#### 리플로우 최소화
- **position: absolute/fixed** 활용
- **레이아웃 변경 최소화**
- **DocumentFragment** 사용 (다중 DOM 조작)

```javascript
// DocumentFragment
const fragment = document.createDocumentFragment();
items.forEach(item => {
  const el = document.createElement('div');
  el.textContent = item;
  fragment.appendChild(el);
});
container.appendChild(fragment); // 한 번만 리플로우
```

### 9.5 번들 크기 최적화

#### Tree Shaking
- **ES6 모듈** 사용
- **사이드 이펙트 제거**

```json
// package.json
{
  "sideEffects": false
}
```

#### 라이브러리 선택
- **date-fns** > moment.js (더 작음)
- **react-window** > react-virtualized
- **중복 제거**: 동일 라이브러리 버전 통일

```javascript
// 좋은 예 - 필요한 함수만 import
import { format } from 'date-fns';

// 나쁜 예 - 전체 라이브러리 import
import _ from 'lodash';
```

#### Compression
- **Brotli** (최우선) 또는 **Gzip**
- **서버 설정**: nginx, Apache

```nginx
# nginx Brotli 설정
brotli on;
brotli_comp_level 6;
brotli_types text/plain text/css application/json application/javascript;
```

### 9.6 성능 모니터링

#### Core Web Vitals
- **LCP** (Largest Contentful Paint): < 2.5s
- **FID** (First Input Delay): < 100ms
- **CLS** (Cumulative Layout Shift): < 0.1

#### 성능 측정 도구
- **Lighthouse**: 자동화된 감사
- **Chrome DevTools**: Performance 탭
- **Web Vitals**: 실시간 모니터링

```javascript
// Web Vitals 측정
import { getCLS, getFID, getLCP } from 'web-vitals';

getCLS(console.log);
getFID(console.log);
getLCP(console.log);
```

---

## 10. 기술 스택 및 라이브러리

### 10.1 추천 기술 스택

#### 프론트엔드 프레임워크
- **React 18+**: 컴포넌트 기반, 대규모 생태계
- **Next.js** (선택): SSR, SSG, 최적화된 라우팅

#### 상태 관리
- **Zustand**: 경량, 간단한 API, TypeScript 친화적
- 대안: Redux Toolkit, Jotai

#### UI 컴포넌트
- **Tailwind CSS**: 유틸리티 우선, 빠른 개발
- **Radix UI**: 접근성 우수, Headless 컴포넌트
- **shadcn/ui**: Radix + Tailwind 통합

#### 폼 관리
- **React Hook Form**: 성능 우수, 간단한 검증
- **Zod**: TypeScript 스키마 검증

#### 데이터 페칭
- **TanStack Query** (React Query): 캐싱, 동기화, 상태 관리
- **Axios**: HTTP 클라이언트

#### 차트
- **Recharts**: React 친화적, 커스터마이징 용이
- 대안: Chart.js, Victory

#### 드래그 앤 드롭
- **@dnd-kit/core**: 현대적, 접근성 우수
- 이전: react-beautiful-dnd (유지보수 중단)

#### 주소 검색
- **Kakao 주소 검색 API**: 한국 주소 최적화

#### 날짜 처리
- **date-fns**: 경량, 모듈화, Tree-shaking 지원
- 대안: Day.js

#### 아이콘
- **Lucide React**: 깔끔한 디자인, Tree-shaking
- 대안: Heroicons, Phosphor Icons

#### 애니메이션
- **Framer Motion**: 선언적 API, React 최적화
- 대안: React Spring

### 10.2 라이브러리 버전 (권장)

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "next": "^14.0.0",
    "zustand": "^4.4.0",
    "@tanstack/react-query": "^5.0.0",
    "react-hook-form": "^7.48.0",
    "zod": "^3.22.0",
    "axios": "^1.6.0",
    "recharts": "^2.10.0",
    "@dnd-kit/core": "^6.1.0",
    "@dnd-kit/sortable": "^8.0.0",
    "date-fns": "^3.0.0",
    "lucide-react": "^0.300.0",
    "framer-motion": "^10.16.0",
    "tailwindcss": "^3.3.0",
    "@radix-ui/react-dialog": "^1.0.5",
    "@radix-ui/react-dropdown-menu": "^2.0.6"
  },
  "devDependencies": {
    "typescript": "^5.3.0",
    "@types/react": "^18.2.0",
    "eslint": "^8.55.0",
    "prettier": "^3.1.0"
  }
}
```

### 10.3 개발 도구

- **TypeScript**: 타입 안정성
- **ESLint**: 코드 품질
- **Prettier**: 코드 포매팅
- **Husky**: Git hooks
- **lint-staged**: Pre-commit 검증

---

## 11. 변경 이력

| 날짜 | 버전 | 작성자 | 변경 내용 |
|------|------|--------|-----------|
| 2025-12-14 | 1.0 | 강지수 | 초안 작성 |

---

## 부록

### A. 용어 정의

| 용어 | 정의 |
|------|------|
| Mobile First | 모바일 화면을 기본으로 설계하고 점진적으로 큰 화면을 지원하는 설계 철학 |
| Progressive Disclosure | 복잡한 정보를 단계적으로 공개하는 UX 패턴 |
| WCAG | Web Content Accessibility Guidelines (웹 콘텐츠 접근성 가이드라인) |
| LCP | Largest Contentful Paint (최대 콘텐츠 렌더링 시간) |
| FID | First Input Delay (최초 입력 지연) |
| CLS | Cumulative Layout Shift (누적 레이아웃 이동) |

### B. 참고 자료

- [WCAG 2.1 가이드라인](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material Design 3](https://m3.material.io/)
- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [Web.dev Performance](https://web.dev/performance/)
- [React 공식 문서](https://react.dev/)

---

**문서 종료**
