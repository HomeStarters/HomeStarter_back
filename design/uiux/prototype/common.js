/**
 * 내집마련 도우미 플랫폼 - 공통 JavaScript
 * 작성일: 2025-12-15
 */

// ========== LocalStorage 관리 ==========
const Storage = {
  // 사용자 정보
  getUser() {
    return JSON.parse(localStorage.getItem('user') || 'null');
  },
  setUser(user) {
    localStorage.setItem('user', JSON.stringify(user));
  },
  clearUser() {
    localStorage.removeItem('user');
  },

  // 기본정보
  getBasicInfo() {
    return JSON.parse(localStorage.getItem('basicInfo') || 'null');
  },
  setBasicInfo(info) {
    localStorage.setItem('basicInfo', JSON.stringify(info));
  },

  // 자산정보
  getAssets(type) {
    // type: 'self' | 'spouse'
    return JSON.parse(localStorage.getItem(`assets_${type}`) || '{"assets": [], "loans": [], "incomes": [], "expenses": []}');
  },
  setAssets(type, data) {
    localStorage.setItem(`assets_${type}`, JSON.stringify(data));
  },

  // 주택정보
  getHousings() {
    return JSON.parse(localStorage.getItem('housings') || '[]');
  },
  setHousings(housings) {
    localStorage.setItem('housings', JSON.stringify(housings));
  },
  addHousing(housing) {
    const housings = this.getHousings();
    housings.push({ ...housing, id: Date.now() });
    this.setHousings(housings);
    return housings[housings.length - 1];
  },

  // 계산 결과
  getCalculations() {
    return JSON.parse(localStorage.getItem('calculations') || '[]');
  },
  setCalculations(calculations) {
    localStorage.setItem('calculations', JSON.stringify(calculations));
  },
  addCalculation(calculation) {
    const calculations = this.getCalculations();
    calculations.push({ ...calculation, id: Date.now(), date: new Date().toISOString() });
    this.setCalculations(calculations);
  },

  // 전체 초기화
  clearAll() {
    localStorage.clear();
  }
};

// ========== 유틸리티 함수 ==========
const Utils = {
  // 금액 포맷팅 (천 단위 콤마)
  formatCurrency(amount) {
    if (amount === null || amount === undefined || amount === '') return '0';
    return Number(amount).toLocaleString('ko-KR');
  },

  // 금액 파싱 (콤마 제거)
  parseCurrency(str) {
    if (!str) return 0;
    return Number(str.toString().replace(/,/g, ''));
  },

  // 날짜 포맷팅 (YYYY년 MM월)
  formatDate(date) {
    if (!date) return '';
    const d = new Date(date);
    return `${d.getFullYear()}년 ${String(d.getMonth() + 1).padStart(2, '0')}월`;
  },

  // 날짜 포맷팅 (YYYY-MM-DD)
  formatDateISO(date) {
    if (!date) return '';
    const d = new Date(date);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  },

  // 전화번호 포맷팅 (010-0000-0000)
  formatPhone(phone) {
    if (!phone) return '';
    const cleaned = phone.replace(/\D/g, '');
    if (cleaned.length === 11) {
      return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`;
    }
    return phone;
  },

  // 이메일 유효성 검증
  validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  },

  // 비밀번호 강도 체크
  checkPasswordStrength(password) {
    if (!password || password.length < 8) return 'weak';

    let strength = 0;
    if (password.match(/[a-zA-Z]/)) strength++;
    if (password.match(/[0-9]/)) strength++;
    if (password.match(/[^a-zA-Z0-9]/)) strength++;

    if (strength === 1) return 'weak';
    if (strength === 2) return 'medium';
    return 'strong';
  },

  // 랜덤 ID 생성
  generateId() {
    return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  },

  // Debounce 함수
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }
};

// ========== UI 컴포넌트 ==========
const UI = {
  // 토스트 메시지 표시
  showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');

    document.body.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  },

  // 로딩 스피너 표시
  showLoading() {
    const overlay = document.createElement('div');
    overlay.id = 'loading-overlay';
    overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    `;

    const spinner = document.createElement('div');
    spinner.className = 'spinner spinner-large';
    spinner.style.borderTopColor = '#FFFFFF';

    overlay.appendChild(spinner);
    document.body.appendChild(overlay);
  },

  hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
      overlay.remove();
    }
  },

  // 확인 다이얼로그
  confirm(message, onConfirm, onCancel) {
    const result = window.confirm(message);
    if (result && onConfirm) {
      onConfirm();
    } else if (!result && onCancel) {
      onCancel();
    }
    return result;
  },

  // 모달 표시
  showModal(content, options = {}) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.setAttribute('role', 'dialog');
    overlay.setAttribute('aria-modal', 'true');

    const modal = document.createElement('div');
    modal.className = 'modal';

    const handle = document.createElement('div');
    handle.className = 'modal-handle';

    modal.appendChild(handle);
    modal.innerHTML += content;
    overlay.appendChild(modal);

    // 배경 클릭 시 닫기
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.remove();
        if (options.onClose) options.onClose();
      }
    });

    document.body.appendChild(overlay);
    return overlay;
  }
};

// ========== 폼 검증 ==========
const Validation = {
  // 필수 입력 체크
  required(value, errorElement) {
    if (!value || value.trim() === '') {
      if (errorElement) {
        errorElement.textContent = '필수 항목입니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (errorElement) {
      errorElement.classList.add('hidden');
    }
    return true;
  },

  // 이메일 검증
  email(value, errorElement) {
    if (!Utils.validateEmail(value)) {
      if (errorElement) {
        errorElement.textContent = '올바른 이메일 형식이 아닙니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (errorElement) {
      errorElement.classList.add('hidden');
    }
    return true;
  },

  // 비밀번호 검증
  password(value, errorElement) {
    if (value.length < 8) {
      if (errorElement) {
        errorElement.textContent = '비밀번호는 8자 이상이어야 합니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }

    const hasLetter = /[a-zA-Z]/.test(value);
    const hasNumber = /[0-9]/.test(value);
    const hasSpecial = /[^a-zA-Z0-9]/.test(value);

    if (!hasLetter || !hasNumber || !hasSpecial) {
      if (errorElement) {
        errorElement.textContent = '영문, 숫자, 특수문자를 모두 포함해야 합니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }

    if (errorElement) {
      errorElement.classList.add('hidden');
    }
    return true;
  },

  // 전화번호 검증
  phone(value, errorElement) {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length !== 11 || !cleaned.startsWith('010')) {
      if (errorElement) {
        errorElement.textContent = '올바른 전화번호 형식이 아닙니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (errorElement) {
      errorElement.classList.add('hidden');
    }
    return true;
  },

  // 숫자 검증
  number(value, min, max, errorElement) {
    const num = Number(value);
    if (isNaN(num)) {
      if (errorElement) {
        errorElement.textContent = '숫자만 입력 가능합니다';
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (min !== undefined && num < min) {
      if (errorElement) {
        errorElement.textContent = `최소값은 ${min}입니다`;
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (max !== undefined && num > max) {
      if (errorElement) {
        errorElement.textContent = `최대값은 ${max}입니다`;
        errorElement.classList.remove('hidden');
      }
      return false;
    }
    if (errorElement) {
      errorElement.classList.add('hidden');
    }
    return true;
  }
};

// ========== 화면 전환 ==========
const Navigation = {
  // 화면 이동
  goto(page) {
    window.location.href = page;
  },

  // 뒤로 가기
  goBack() {
    window.history.back();
  },

  // 인증 체크
  checkAuth() {
    const user = Storage.getUser();
    if (!user) {
      this.goto('01-로그인.html');
      return false;
    }
    return true;
  },

  // 로그아웃
  logout() {
    UI.confirm('로그아웃 하시겠습니까?', () => {
      Storage.clearAll();
      this.goto('01-로그인.html');
    });
  }
};

// ========== 폼 헬퍼 ==========
const FormHelper = {
  // 입력 필드에 금액 포맷 자동 적용
  setupCurrencyInput(inputElement) {
    inputElement.addEventListener('input', (e) => {
      const value = Utils.parseCurrency(e.target.value);
      e.target.value = Utils.formatCurrency(value);
    });
  },

  // 전화번호 자동 포맷
  setupPhoneInput(inputElement) {
    inputElement.addEventListener('input', (e) => {
      e.target.value = Utils.formatPhone(e.target.value);
    });
  },

  // 폼 데이터 수집
  getFormData(formElement) {
    const formData = new FormData(formElement);
    const data = {};
    for (let [key, value] of formData.entries()) {
      data[key] = value;
    }
    return data;
  },

  // 폼 리셋
  resetForm(formElement) {
    formElement.reset();
    // 모든 에러 메시지 숨기기
    formElement.querySelectorAll('.error-message').forEach(el => {
      el.classList.add('hidden');
    });
    // 모든 input에서 error 클래스 제거
    formElement.querySelectorAll('.error').forEach(el => {
      el.classList.remove('error');
    });
  }
};

// ========== 예제 데이터 ==========
const SampleData = {
  // 예제 사용자
  user: {
    id: 'user_001',
    username: 'testuser',
    name: '김민준',
    email: 'minjun@example.com',
    phone: '010-1234-5678'
  },

  // 예제 기본정보
  basicInfo: {
    birthdate: '1990-05-15',
    gender: 'male',
    currentAddress: '서울특별시 강남구 역삼동 123-45',
    workAddress: '서울특별시 강남구 테헤란로 123',
    spouseWorkAddress: '서울특별시 서초구 서초대로 456',
    investmentStyle: 'medium'
  },

  // 예제 자산정보 (본인)
  selfAssets: {
    assets: [
      { id: 1, name: '주택청약저축', amount: 5000000 },
      { id: 2, name: '예금', amount: 15000000 }
    ],
    loans: [
      { id: 1, name: '학자금 대출', amount: 3000000 }
    ],
    incomes: [
      { id: 1, name: '급여', amount: 4000000 }
    ],
    expenses: [
      { id: 1, name: '생활비', amount: 1500000 },
      { id: 2, name: '통신비', amount: 100000 }
    ]
  },

  // 예제 주택정보
  housings: [
    {
      id: 1,
      type: '매매',
      name: '아크로리버뷰 84㎡',
      address: '서울특별시 마포구 공덕동 456-78',
      price: 850000000,
      moveInDate: '2026-03',
      buildDate: '2024-12',
      areaType: '84㎡',
      isFinalGoal: true
    }
  ],

  // 예제 대출상품
  loanProducts: [
    {
      id: 1,
      name: '신혼부부 특별대출',
      limit: 300000000,
      ltv: 70,
      dti: 60,
      dsr: 40,
      interestRate: 2.5,
      targetHousing: '6억 이하 아파트',
      incomeRequirement: '부부합산 연소득 7천만원 이하'
    },
    {
      id: 2,
      name: '디딤돌 대출',
      limit: 400000000,
      ltv: 70,
      dti: 60,
      dsr: 40,
      interestRate: 3.0,
      targetHousing: '9억 이하 주택',
      incomeRequirement: '연소득 6천만원 이하'
    }
  ],

  // 로컬스토리지에 예제 데이터 로드
  loadAll() {
    Storage.setUser(this.user);
    Storage.setBasicInfo(this.basicInfo);
    Storage.setAssets('self', this.selfAssets);
    Storage.setHousings(this.housings);
    UI.showToast('예제 데이터가 로드되었습니다', 'success');
  }
};

// ========== 페이지 초기화 ==========
document.addEventListener('DOMContentLoaded', () => {
  // 스크롤 시 헤더 스타일 변경
  const header = document.querySelector('.header');
  if (header) {
    window.addEventListener('scroll', () => {
      if (window.scrollY > 0) {
        header.classList.add('scrolled');
      } else {
        header.classList.remove('scrolled');
      }
    });
  }

  // 뒤로 가기 버튼
  const backButtons = document.querySelectorAll('.back-button, .btn-back');
  backButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      Navigation.goBack();
    });
  });

  // 현재 페이지에 맞는 네비게이션 활성화
  const currentPage = window.location.pathname.split('/').pop();
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    const href = item.getAttribute('href');
    if (href === currentPage) {
      item.classList.add('active');
    }
  });

  // 로그아웃 버튼
  const logoutButtons = document.querySelectorAll('.logout-button, .btn-logout');
  logoutButtons.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      Navigation.logout();
    });
  });
});

// ========== 전역 객체 노출 ==========
window.Storage = Storage;
window.Utils = Utils;
window.UI = UI;
window.Validation = Validation;
window.Navigation = Navigation;
window.FormHelper = FormHelper;
window.SampleData = SampleData;
