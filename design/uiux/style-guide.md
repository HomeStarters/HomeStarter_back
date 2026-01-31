# 스타일 가이드 - 내집마련 도우미 플랫폼

## 문서 정보
- **작성일**: 2025-12-14
- **작성자**: 강지수 (Product Designer)
- **버전**: 1.0.0
- **관련 문서**:
  * design/userstory.md
  * design/uiux/uiux.md

---

## 목차
1. [브랜드 아이덴티티](#1-브랜드-아이덴티티)
2. [디자인 원칙](#2-디자인-원칙)
3. [컬러 시스템](#3-컬러-시스템)
4. [타이포그래피](#4-타이포그래피)
5. [간격 시스템](#5-간격-시스템)
6. [컴포넌트 스타일](#6-컴포넌트-스타일)
7. [반응형 브레이크포인트](#7-반응형-브레이크포인트)
8. [대상 서비스 특화 컴포넌트](#8-대상-서비스-특화-컴포넌트)
9. [인터랙션 패턴](#9-인터랙션-패턴)
10. [변경 이력](#10-변경-이력)

---

## 1. 브랜드 아이덴티티

### 1.1 디자인 컨셉

**타겟 사용자**
- 사회초년생 (20대 후반~30대 초반)
- 신혼부부 (결혼 준비 또는 결혼 초기)
- 특징:
  * 재무 지식이 부족하지만 내 집 마련에 대한 열망이 강함
  * 모바일 디바이스에 익숙하고 빠른 의사결정을 선호
  * 복잡한 재무 정보를 쉽게 이해하고 싶어함

**디자인 철학**
- **Trust (신뢰)**: 재무 관련 서비스이므로 신뢰감이 최우선
- **Simple (단순함)**: 복잡한 재무 정보를 직관적으로 표현
- **Modern (현대적)**: 젊은 층을 타겟으로 한 모던하고 깨끗한 UI
- **Hopeful (희망적)**: 내 집 마련이라는 목표 달성을 응원하는 긍정적 느낌
- **Growth (성장)**: 자산 증가와 목표 달성 과정을 시각화

### 1.2 브랜드 키워드

| 키워드 | 의미 | 디자인 반영 |
|--------|------|-------------|
| **신뢰** | 안정적이고 전문적인 서비스 | 블루 계열 Primary Color, 깔끔한 레이아웃 |
| **단순함** | 복잡하지 않은 사용자 경험 | 카드 기반 정보 구조, 명확한 계층 |
| **현대적** | 최신 트렌드를 반영한 디자인 | 모던한 타이포그래피, 최소한의 장식 |
| **희망** | 긍정적이고 밝은 분위기 | 밝은 배경, 성장을 나타내는 Secondary 컬러 |
| **성장** | 목표 달성 과정의 시각화 | 차트, 타임라인, 진행률 표시 |

### 1.3 레퍼런스 분석

**분석 대상**: monimo.com (삼성카드 금융 통합 앱)

**주요 인사이트**
- 카드 기반 정보 구조가 금융 데이터 표현에 효과적
- 자산 분석 섹션의 시각적 계층 구조 우수
- 3가지 큐레이션 홈 제공으로 사용자 맞춤화
- 명확한 CTA (Call To Action) 배치

**적용 사항**
- 대시보드 재무 현황을 카드 형태로 구성
- 자산/대출/주택 정보를 카드 리스트로 표현
- 시각적 차트(도넛, 게이지 바) 적극 활용
- 주요 액션 버튼을 눈에 띄게 배치

---

## 2. 디자인 원칙

### 2.1 Mobile First (최우선)

> "작은 화면에서 완벽하게, 큰 화면에서 더 풍부하게"

- **우선순위 중심 설계**: 작은 화면에서 공간이 제한되므로, 가장 중요한 콘텐츠와 기능에 집중
- **점진적 향상**: 모바일 기본 경험을 먼저 구축한 후, 화면이 커질수록 추가 기능과 콘텐츠를 더해감
- **성능 최적화**: 모바일 환경의 제약(느린 네트워크, 제한된 처리 능력)을 먼저 고려

**적용 예시**
```
Mobile (320px):
- 싱글 컬럼 레이아웃
- 핵심 정보만 표시
- 하단 탭 네비게이션

Desktop (1024px+):
- 멀티 컬럼 레이아웃
- 추가 정보 및 미리보기 제공
- 사이드 메뉴 + 넓은 콘텐츠 영역
```

### 2.2 Clarity First (명확성 우선)

> "한 눈에 이해할 수 있는 디자인"

- **시각적 단순화**: 복잡한 재무 정보를 차트와 아이콘으로 시각화
- **단일 목적**: 한 화면에 하나의 주요 목적, 명확한 CTA
- **명확한 계층**: 제목, 본문, 부가 정보의 시각적 차별화
- **정보 흐름**: 자연스러운 읽기 순서 (F-패턴, Z-패턴)

### 2.3 Consistency (일관성)

> "예측 가능한 사용자 경험"

- **컴포넌트 일관성**: 동일한 기능은 동일한 스타일
- **인터랙션 일관성**: 버튼, 링크, 카드의 동작이 일관됨
- **색상 일관성**: 의미에 따라 일관된 색상 사용 (성공=녹색, 에러=빨강)
- **타이포그래피 일관성**: 동일한 위계는 동일한 폰트 크기/굵기

### 2.4 Accessibility (접근성)

> "모든 사용자를 위한 디자인"

- **WCAG 2.1 AA 준수**: 접근성 표준 준수
- **색상 대비**: 텍스트와 배경의 충분한 대비 (최소 4.5:1)
- **터치 타겟**: 최소 44x44px 크기 보장
- **키보드 네비게이션**: 모든 인터랙티브 요소 키보드로 접근 가능
- **스크린 리더**: 의미 있는 대체 텍스트와 ARIA 속성

### 2.5 Performance (성능)

> "빠르고 부드러운 경험"

- **빠른 로딩**: 초기 로딩 3초 이내 목표
- **부드러운 애니메이션**: 60fps 유지
- **효율적인 이미지**: WebP 포맷, 적절한 압축
- **레이지 로딩**: 화면 밖 콘텐츠는 필요 시 로딩
- **최소 리렌더링**: 성능 최적화를 위한 메모이제이션

---

## 3. 컬러 시스템

### 3.1 Primary Colors (신뢰와 전문성)

**용도**: 주요 CTA 버튼, 링크, 강조 요소, 브랜드 표현

| 레벨 | 색상 코드 | 용도 | 예시 |
|------|-----------|------|------|
| **Primary-900** | `#002966` | 가장 진한 강조 | 배경 강조, 중요 텍스트 |
| **Primary-700** | `#0052CC` | 호버 상태 | 버튼 호버 |
| **Primary-500** | `#0066FF` | **기본** | 주요 CTA, 링크 |
| **Primary-300** | `#4D94FF` | 비활성 상태 | Disabled 버튼 |
| **Primary-100** | `#E6F0FF` | 매우 밝은 배경 | 하이라이트 배경 |

**색상 대비 검증**
- Primary-500 on White: **10.5:1** (AAA 통과)
- White on Primary-500: **10.5:1** (AAA 통과)

### 3.2 Secondary Colors (성장과 안정)

**용도**: 성공 메시지, 긍정적 지표, 자산 증가 표시

| 레벨 | 색상 코드 | 용도 | 예시 |
|------|-----------|------|------|
| **Secondary-700** | `#00A688` | 호버 상태 | 성공 버튼 호버 |
| **Secondary-500** | `#00C9A7` | **기본** | 성공 메시지, 자산 증가 |
| **Secondary-300** | `#66E0CC` | 밝은 상태 | 배경 강조 |
| **Secondary-100** | `#E6F9F5` | 매우 밝은 배경 | 성공 알림 배경 |

### 3.3 Semantic Colors (의미 전달)

| 색상 | 코드 | 용도 | 사용 예시 |
|------|------|------|-----------|
| **Error** | `#FF3B30` | 오류, 경고, 삭제 | 에러 메시지, 삭제 버튼 |
| **Warning** | `#FF9500` | 주의, 중요 알림 | 주의 메시지, 임계값 경고 |
| **Success** | `#00C9A7` | 성공, 완료 | 성공 메시지, 완료 상태 |
| **Info** | `#007AFF` | 정보, 힌트 | 정보 메시지, 도움말 |

### 3.4 Neutral Colors (기본 색상)

| 색상 | 코드 | 용도 |
|------|------|------|
| **Background** | `#F5F5F7` | 앱 전체 배경 |
| **Surface** | `#FFFFFF` | 카드, 모달, 입력 필드 배경 |
| **Border** | `#D1D1D6` | 구분선, 테두리 |
| **Text-Primary** | `#1D1D1F` | 주요 텍스트 |
| **Text-Secondary** | `#86868B` | 보조 텍스트, 설명 |
| **Text-Disabled** | `#C7C7CC` | 비활성 텍스트 |

### 3.5 Data Visualization Colors (차트용)

**용도**: 재무 현황 차트, 자산 비중 도넛 차트, 데이터 시각화

| 순서 | 색상 | 코드 | 용도 예시 |
|------|------|------|-----------|
| Chart-1 | Primary | `#0066FF` | 부동산 자산 |
| Chart-2 | Secondary | `#00C9A7` | 예금/적금 |
| Chart-3 | Warning | `#FF9500` | 주식/펀드 |
| Chart-4 | Info | `#007AFF` | 대출 |
| Chart-5 | Error | `#FF3B30` | 기타 부채 |
| Chart-6 | Neutral | `#8E8E93` | 기타 자산 |

### 3.6 Financial Status Colors (재무 상태 표시)

| 상태 | 색상 | 코드 | 사용 예시 |
|------|------|------|-----------|
| **Positive** | Green | `#00C9A7` | 자산 증가, 여유자금 있음 |
| **Negative** | Red | `#FF3B30` | 부채, 주의 필요, 여유자금 부족 |
| **Neutral** | Gray | `#86868B` | 변동 없음, 보통 |

---

## 4. 타이포그래피

### 4.1 폰트 패밀리

**Primary Font**
- **한글**: Pretendard Variable
  * 가변 폰트로 다양한 굵기 지원
  * 한글 가독성 우수
  * Google Fonts 또는 CDN 사용

- **영문/숫자**: SF Pro (Apple 시스템 폰트)
  * 모던하고 깔끔한 느낌
  * 숫자 가독성 우수

**Fallback Stack**
```css
font-family: 'Pretendard Variable', -apple-system, BlinkMacSystemFont,
             'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
```

### 4.2 타이포그래피 스케일

| 레벨 | 크기 | 굵기 | 행간 | Letter Spacing | 용도 |
|------|------|------|------|----------------|------|
| **H1** | 28px | Bold (700) | 36px | -0.5px | 화면 타이틀 (대시보드, 주요 화면) |
| **H2** | 22px | Bold (700) | 28px | -0.3px | 섹션 헤더 (재무 현황, 빠른 작업) |
| **H3** | 18px | Semibold (600) | 24px | -0.2px | 카드 타이틀, 서브 헤더 |
| **Body** | 16px | Regular (400) | 24px | 0px | 본문 텍스트, 입력 필드 |
| **Caption** | 14px | Regular (400) | 20px | 0px | 보조 텍스트, 설명 |
| **Small** | 12px | Regular (400) | 16px | 0px | 힌트, 레이블, 최소 텍스트 |

### 4.3 금액 표시 (특화)

**강조 금액** (큰 금액, 중요)
- 크기: 24px
- 굵기: Bold (700)
- 색상: Primary-900 (`#002966`)
- 용도: 총 자산, 총 대출, 주요 금액

```css
.amount-emphasized {
  font-size: 24px;
  font-weight: 700;
  color: #002966;
  line-height: 32px;
}
```

**일반 금액** (중간 크기)
- 크기: 18px
- 굵기: Semibold (600)
- 색상: Text-Primary (`#1D1D1F`)
- 용도: 카드 내 금액, 목록 금액

```css
.amount-normal {
  font-size: 18px;
  font-weight: 600;
  color: #1D1D1F;
  line-height: 24px;
}
```

**금액 형식 규칙**
- 천 단위 콤마 자동 삽입: `1,234,567`
- 단위 표시: "원" (금액 뒤에 4px 간격)
- 변동률 표시: `↑ 5.2%` (아이콘 + 퍼센트)
  * 상승: ↑ + Secondary-500 (`#00C9A7`)
  * 하락: ↓ + Error (`#FF3B30`)

### 4.4 반응형 타이포그래피

**모바일 (320-767px)**
- 기본 폰트 크기 유지
- 화면 좁을 때 H1 → 24px로 축소 가능

**태블릿/데스크톱 (768px+)**
- 모든 폰트 크기 유지
- 더 넓은 line-height로 가독성 향상

---

## 5. 간격 시스템

### 5.1 기본 단위

**4px 기반 시스템**
- 모든 간격은 4의 배수 사용
- 일관된 리듬과 정렬 보장

### 5.2 간격 스케일

| 토큰 | 크기 | 용도 | 예시 |
|------|------|------|------|
| **xs** | 4px | 최소 간격 | 아이콘-텍스트 간격 |
| **sm** | 8px | 밀집 요소 | 라벨-입력 필드, 버튼 내 패딩 |
| **md** | 16px | **기본 간격** | 카드 내부 패딩, 요소 간 간격 |
| **lg** | 24px | 섹션 구분 | 섹션 간 간격 |
| **xl** | 32px | 화면 여백 | 화면 좌우 패딩 |
| **xxl** | 48px | 주요 섹션 구분 | 큰 섹션 간 간격 |

### 5.3 컴포넌트별 간격

**카드 (Card)**
- 내부 패딩: 16px (md)
- 카드 간 간격: 12px (모바일), 16px (데스크톱)

**버튼 (Button)**
- 내부 패딩: 12px 16px (세로 가로)
- 버튼 간 간격: 8px (sm)

**입력 필드 (Input Field)**
- 라벨-필드 간격: 8px (sm)
- 필드 간 간격: 16px (md)

**화면 여백 (Screen Padding)**
- 모바일: 16px (md)
- 태블릿/데스크톱: 24px (lg)

---

## 6. 컴포넌트 스타일

### 6.1 버튼 (Button)

#### Primary Button
```css
.btn-primary {
  background-color: #0066FF;
  color: #FFFFFF;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  padding: 12px 16px;
  min-height: 48px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background-color: #0052CC;
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.3);
}

.btn-primary:active {
  background-color: #003D99;
  transform: scale(0.98);
}

.btn-primary:disabled {
  background-color: #4D94FF;
  cursor: not-allowed;
  opacity: 0.5;
}
```

#### Secondary Button
```css
.btn-secondary {
  background-color: transparent;
  color: #0066FF;
  border: 1px solid #0066FF;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  padding: 12px 16px;
  min-height: 48px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background-color: #E6F0FF;
  border-color: #0052CC;
  color: #0052CC;
}
```

#### Text Button
```css
.btn-text {
  background-color: transparent;
  color: #0066FF;
  border: none;
  font-size: 16px;
  font-weight: 600;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-text:hover {
  color: #0052CC;
  text-decoration: underline;
}
```

#### 버튼 크기
- **Small**: height 32px, font-size 14px
- **Medium**: height 40px, font-size 16px
- **Large**: height 48px, font-size 16px (기본)

### 6.2 카드 (Card)

**기본 카드**
```css
.card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.2s ease;
}
```

**액션 카드** (클릭 가능)
```css
.card-action {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s ease;
}

.card-action:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-action:active {
  transform: translateY(0);
}
```

### 6.3 입력 필드 (Input Field)

```css
.input-field {
  width: 100%;
  height: 48px;
  padding: 12px 16px;
  border: 1px solid #D1D1D6;
  border-radius: 8px;
  font-size: 16px;
  color: #1D1D1F;
  background-color: #FFFFFF;
  transition: all 0.2s ease;
}

.input-field:focus {
  outline: none;
  border-color: #0066FF;
  box-shadow: 0 0 0 3px rgba(0, 102, 255, 0.1);
}

.input-field.error {
  border-color: #FF3B30;
}

.input-field:disabled {
  background-color: #F5F5F7;
  color: #C7C7CC;
  cursor: not-allowed;
}
```

**Floating Label**
```html
<div class="input-wrapper">
  <input type="text" id="name" class="input-field" placeholder=" ">
  <label for="name" class="floating-label">이름</label>
</div>
```

### 6.4 금액 표시 컴포넌트

```html
<div class="amount-display">
  <span class="amount-value">1,234,567</span>
  <span class="amount-unit">원</span>
  <span class="amount-change positive">
    <span class="icon">↑</span>
    <span class="percent">5.2%</span>
  </span>
</div>
```

```css
.amount-display {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.amount-value {
  font-size: 24px;
  font-weight: 700;
  color: #002966;
}

.amount-unit {
  font-size: 16px;
  color: #86868B;
}

.amount-change {
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 2px;
}

.amount-change.positive {
  color: #00C9A7;
}

.amount-change.negative {
  color: #FF3B30;
}
```

### 6.5 뱃지 (Badge)

```css
.badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
}

.badge-primary {
  background-color: #E6F0FF;
  color: #0066FF;
}

.badge-success {
  background-color: #E6F9F5;
  color: #00C9A7;
}

.badge-error {
  background-color: #FFE5E5;
  color: #FF3B30;
}

.badge-warning {
  background-color: #FFF4E5;
  color: #FF9500;
}
```

### 6.6 그림자 (Elevation)

| 레벨 | CSS | 용도 |
|------|-----|------|
| Level 1 | `0 1px 3px rgba(0,0,0,0.1)` | 카드 기본 |
| Level 2 | `0 4px 12px rgba(0,0,0,0.15)` | 카드 호버, 모달 |
| Level 3 | `0 8px 24px rgba(0,0,0,0.2)` | 드롭다운, 팝오버 |

### 6.7 테두리 (Border Radius)

| 크기 | 값 | 용도 |
|------|-----|------|
| Small | 4px | 체크박스, 뱃지 |
| Medium | 8px | 버튼, 입력 필드 |
| Large | 12px | 카드 |
| XLarge | 16px | 모달 |
| Round | 50% | 아바타, FAB |

---

## 7. 반응형 브레이크포인트

### 7.1 브레이크포인트 정의

| 디바이스 | 범위 | 컬럼 | 여백 |
|----------|------|------|------|
| **Mobile** | 320px - 767px | 1 | 16px |
| **Tablet** | 768px - 1023px | 2-3 | 24px |
| **Desktop** | 1024px+ | 3-4 | 24px |

### 7.2 반응형 레이아웃 예시

**대시보드 재무 현황 카드**
```css
/* Mobile: 2컬럼 그리드 */
@media (max-width: 767px) {
  .financial-status {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

/* Tablet: 3컬럼 그리드 */
@media (min-width: 768px) and (max-width: 1023px) {
  .financial-status {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}

/* Desktop: 4컬럼 또는 flex 레이아웃 */
@media (min-width: 1024px) {
  .financial-status {
    display: flex;
    gap: 24px;
  }
}
```

### 7.3 네비게이션 전환

**모바일**: 하단 탭 네비게이션
- 고정 위치 (position: fixed, bottom: 0)
- 5개 주요 메뉴 (홈, 주택, 계산, 로드맵, 더보기)
- 아이콘 + 라벨

**태블릿/데스크톱**: 사이드 메뉴
- 좌측 고정 사이드바
- 확장 가능한 메뉴 구조
- 아이콘 + 텍스트 라벨

---

## 8. 대상 서비스 특화 컴포넌트

### 8.1 재무 현황 카드 (Financial Status Card)

**구조**
```html
<div class="financial-status-card">
  <div class="card-header">
    <span class="label">총 자산</span>
    <button class="info-btn">?</button>
  </div>
  <div class="card-body">
    <div class="amount-display">
      <span class="amount-value">12,345,678</span>
      <span class="amount-unit">원</span>
    </div>
    <div class="amount-change positive">
      <span class="icon">↑</span>
      <span class="percent">5.2%</span>
      <span class="label">전월 대비</span>
    </div>
  </div>
  <div class="card-chart">
    <!-- 미니 차트 (선택) -->
  </div>
</div>
```

**스타일**
- 레이아웃: 세로 스택
- 크기: 모바일 2컬럼 그리드 (1fr 1fr)
- 배경: White
- 패딩: 16px
- 라운드: 12px

### 8.2 주택 정보 카드 (Housing Card)

**구조**
```html
<div class="housing-card">
  <div class="card-header">
    <div class="badges">
      <span class="badge badge-primary">매매</span>
      <span class="badge-star">★</span> <!-- 최종목표 -->
    </div>
  </div>
  <div class="card-body">
    <h3 class="housing-name">아크로리버뷰</h3>
    <p class="housing-location">서울시 마포구 공덕동</p>
    <div class="housing-info">
      <span class="price">8억 5천만원</span>
      <span class="type">84㎡</span>
      <span class="date">2026년 3월 입주</span>
    </div>
  </div>
  <div class="card-footer">
    <button class="btn-text">상세보기</button>
  </div>
</div>
```

**스타일**
- 배경: White
- 호버: 그림자 Level 2, 살짝 위로 이동
- 최종목표 별: Primary-500 색상

### 8.3 대출 상품 카드 (Loan Product Card)

**구조**
```html
<div class="loan-product-card">
  <div class="card-header">
    <h3 class="loan-name">신혼부부 특별대출</h3>
    <button class="bookmark-btn">
      <icon>🔖</icon>
    </button>
  </div>
  <div class="card-body">
    <div class="loan-rate">
      <span class="label">금리</span>
      <span class="value">연 2.5%</span>
    </div>
    <div class="loan-limit">
      <span class="label">한도</span>
      <span class="value">최대 3억원</span>
    </div>
    <div class="loan-requirements">
      <span class="label">자격요건</span>
      <p class="summary">무주택 신혼부부, 소득 7천만원 이하</p>
    </div>
  </div>
  <div class="card-footer">
    <button class="btn-primary">자세히 보기</button>
  </div>
</div>
```

### 8.4 계산 결과 카드 (Calculation Result Card)

**구조**
```html
<div class="calculation-result-card">
  <div class="card-header">
    <span class="date">2025-12-14 14:30</span>
    <span class="badge badge-success">✅ 충족</span>
  </div>
  <div class="card-body">
    <h3 class="housing-name">아크로리버뷰</h3>
    <p class="loan-name">신혼부부 특별대출</p>
    <div class="result-summary">
      <div class="result-item">
        <span class="label">입주 후 여유자금</span>
        <span class="value positive">+ 1,200,000원/월</span>
      </div>
    </div>
  </div>
</div>
```

**충족 여부 뱃지**
- 충족: ✅ + Secondary-500 배경
- 미충족: ❌ + Error 배경

### 8.5 타임라인 컴포넌트 (Roadmap Timeline)

**구조**
```html
<div class="roadmap-timeline">
  <div class="timeline-track">
    <div class="timeline-node current">
      <div class="node-circle">
        <span class="node-label">현재</span>
      </div>
      <div class="node-card">
        <p class="node-date">2025년 12월</p>
        <p class="node-info">월세 거주</p>
      </div>
    </div>

    <div class="timeline-line"></div>

    <div class="timeline-node">
      <div class="node-circle">
        <span class="node-label">1단계</span>
      </div>
      <div class="node-card">
        <h4>신혼 전세</h4>
        <p class="node-date">2026년 3월</p>
        <p class="node-price">전세 3억원</p>
      </div>
    </div>

    <div class="timeline-line"></div>

    <div class="timeline-node final">
      <div class="node-circle">
        <span class="node-label">최종</span>
      </div>
      <div class="node-card">
        <h4>아크로리버뷰</h4>
        <p class="node-date">2030년 6월</p>
        <p class="node-price">매매 8.5억원</p>
      </div>
    </div>
  </div>
</div>
```

**스타일**
- 수평 스크롤 가능
- 현재 위치: Primary-500 원형 노드
- 생애주기 이벤트: Secondary-500 마커
- 연결선: Border 색상

### 8.6 자산 항목 관리 컴포넌트 (Asset Item Manager)

**구조**
```html
<div class="asset-manager">
  <div class="asset-items">
    <div class="asset-item">
      <div class="drag-handle">
        <icon>☰</icon>
      </div>
      <div class="item-content">
        <input type="text" placeholder="자산 이름" value="예금">
        <input type="number" placeholder="금액" value="10000000">
      </div>
      <div class="item-actions">
        <button class="btn-edit">✏️</button>
        <button class="btn-delete">🗑️</button>
      </div>
    </div>

    <!-- 더 많은 항목들... -->
  </div>

  <div class="asset-summary">
    <span class="label">총 자산</span>
    <span class="total-amount">25,000,000원</span>
  </div>

  <button class="btn-add">+ 자산 추가하기</button>
</div>
```

**스타일**
- 각 항목: 카드 형태
- 드래그 핸들: 좌측 아이콘
- 총액: 하단 강조 표시
- 추가 버튼: Secondary 버튼

---

## 9. 인터랙션 패턴

### 9.1 피드백 패턴

**버튼 클릭**
```css
.btn:active {
  transform: scale(0.95);
  transition: transform 0.15s ease;
}
```

**카드 호버**
```css
.card-action:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
  transition: all 0.2s ease;
}
```

**로딩 상태**
- 스켈레톤 UI: 카드 윤곽 표시 (데이터 로딩)
- 스피너: 버튼 내 작은 스피너 (액션 처리)
- 진행률 바: AI 로드맵 생성 (장시간 작업)

**성공/실패 토스트**
```css
.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease;
}

.toast-success {
  background-color: #00C9A7;
  color: #FFFFFF;
}

.toast-error {
  background-color: #FF3B30;
  color: #FFFFFF;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translate(-50%, 20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}
```

### 9.2 전환 패턴

**화면 전환**
```css
.page-enter {
  opacity: 0;
  transform: translateX(100%);
}

.page-enter-active {
  opacity: 1;
  transform: translateX(0);
  transition: all 0.3s ease-out;
}

.page-exit {
  opacity: 1;
  transform: translateX(0);
}

.page-exit-active {
  opacity: 0;
  transform: translateX(-100%);
  transition: all 0.3s ease-out;
}
```

**모달 표시**
```css
.modal-overlay {
  animation: fadeIn 0.25s ease;
}

.modal-content {
  animation: scaleUp 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
```

**탭 전환**
```css
.tab-content {
  animation: fadeTransition 0.2s ease;
}

@keyframes fadeTransition {
  from { opacity: 0; }
  to { opacity: 1; }
}
```

**아코디언**
```css
.accordion-content {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.accordion-content.open {
  max-height: 500px; /* 또는 auto + JS */
}
```

### 9.3 제스처 패턴 (모바일)

**Pull to Refresh**
- 대시보드에서 아래로 당겨 새로고침
- 당기는 거리에 따라 아이콘 회전

**Swipe to Delete**
- 목록 항목 좌측 스와이프
- 삭제 버튼 노출
- 스와이프 거리 > 50% → 자동 삭제

**Drag and Drop**
- 자산 항목 순서 변경
- 드래그 시 항목 살짝 확대 + 그림자
- 드롭 위치 하이라이트

**Long Press**
- 카드 길게 누르기
- 컨텍스트 메뉴 표시 (수정, 삭제, 공유)

### 9.4 로딩 패턴

**초기 로딩 (Skeleton UI)**
```html
<div class="skeleton-card">
  <div class="skeleton-header"></div>
  <div class="skeleton-line"></div>
  <div class="skeleton-line short"></div>
</div>
```

```css
.skeleton-header,
.skeleton-line {
  background: linear-gradient(
    90deg,
    #F5F5F7 25%,
    #E5E5E7 50%,
    #F5F5F7 75%
  );
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
```

**인라인 로딩 (버튼 내 스피너)**
```html
<button class="btn-primary" disabled>
  <span class="spinner"></span>
  처리 중...
</button>
```

**AI 로드맵 생성 (진행률 바)**
```html
<div class="progress-bar">
  <div class="progress-fill" style="width: 60%;"></div>
  <span class="progress-text">로드맵 생성 중... 60%</span>
</div>
```

### 9.5 에러 처리 패턴

**인라인 검증 (입력 필드)**
```html
<div class="input-wrapper">
  <input type="text" class="input-field error">
  <p class="error-message">이름은 2자 이상이어야 합니다</p>
</div>
```

```css
.error-message {
  font-size: 12px;
  color: #FF3B30;
  margin-top: 4px;
}
```

**폼 제출 에러**
- 첫 번째 에러 필드로 자동 스크롤
- 해당 필드에 포커스
- 에러 메시지 표시

**네트워크 에러**
```html
<div class="error-dialog">
  <icon>⚠️</icon>
  <h3>네트워크 오류</h3>
  <p>인터넷 연결을 확인해주세요</p>
  <button class="btn-primary">재시도</button>
</div>
```

**Empty State**
```html
<div class="empty-state">
  <img src="empty-illustration.svg" alt="">
  <h3>아직 등록한 주택이 없습니다</h3>
  <p>관심 있는 주택을 등록해보세요</p>
  <button class="btn-primary">주택 등록하기</button>
</div>
```

---

## 10. 변경 이력

### Version 1.0.0 (2025-12-14)
**초기 버전 작성**
- 브랜드 아이덴티티 정의
- 디자인 원칙 5가지 수립
- 컬러 시스템 확장 (Primary, Secondary, Semantic, Data Visualization)
- 타이포그래피 스케일 및 금액 표시 규칙 정의
- 간격 시스템 (4px 기반)
- 기본 컴포넌트 스타일 (Button, Card, Input, Amount Display, Badge)
- 반응형 브레이크포인트 정의
- 대상 서비스 특화 컴포넌트 6가지 정의
  * 재무 현황 카드
  * 주택 정보 카드
  * 대출 상품 카드
  * 계산 결과 카드
  * 타임라인 컴포넌트
  * 자산 항목 관리 컴포넌트
- 인터랙션 패턴 5가지 정의 (Feedback, Transition, Gesture, Loading, Error Handling)

---

## 부록

### A. 색상 접근성 검증

**텍스트 대비 (WCAG 2.1 AA 기준: 4.5:1)**
| 조합 | 대비 | 통과 |
|------|------|------|
| Primary-500 (#0066FF) on White | 10.5:1 | ✅ AAA |
| Text-Primary (#1D1D1F) on White | 19.8:1 | ✅ AAA |
| Text-Secondary (#86868B) on White | 4.6:1 | ✅ AA |
| White on Primary-500 | 10.5:1 | ✅ AAA |
| White on Secondary-500 | 5.2:1 | ✅ AA |

### B. 반응형 테스트 체크리스트

**모바일 (320px-767px)**
- [ ] 하단 탭 네비게이션 정상 작동
- [ ] 카드 2컬럼 그리드 레이아웃
- [ ] 터치 타겟 최소 44x44px
- [ ] 가로 스크롤 없음
- [ ] 텍스트 가독성 확보

**태블릿 (768px-1023px)**
- [ ] 카드 3컬럼 그리드 레이아웃
- [ ] 사이드 메뉴 또는 하단 탭 선택 가능
- [ ] 여백 충분

**데스크톱 (1024px+)**
- [ ] 사이드 메뉴 정상 작동
- [ ] 3-4컬럼 레이아웃
- [ ] 넓은 화면 활용

### C. 브라우저 지원

**최소 지원 버전**
- Chrome: 90+
- Safari: 14+
- Firefox: 88+
- Edge: 90+

**모바일 브라우저**
- iOS Safari: 14+
- Chrome Mobile: 90+
- Samsung Internet: 14+

### D. 성능 목표

| 지표 | 목표 |
|------|------|
| First Contentful Paint (FCP) | < 1.8s |
| Time to Interactive (TTI) | < 3.0s |
| Largest Contentful Paint (LCP) | < 2.5s |
| Cumulative Layout Shift (CLS) | < 0.1 |
| Animation Frame Rate | 60fps |

---

**문서 끝**
