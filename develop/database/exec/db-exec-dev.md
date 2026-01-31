# 데이터베이스설치결과서

## 1. User 서비스
- DB 유형: PostgreSQL
- DB Host: 20.249.153.213
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: userdb

---

## 2. Asset 서비스
- DB 유형: PostgreSQL
- DB Host: 4.230.48.72
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: assetdb

---

## 3. Loan 서비스
- DB 유형: PostgreSQL
- DB Host: 4.230.159.143
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: loandb

---

## 4. Housing 서비스
- DB 유형: PostgreSQL
- DB Host: 4.230.65.89
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: housingdb

---

## 5. Calculator 서비스
- DB 유형: PostgreSQL
- DB Host: 20.214.121.121
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: calculatordb

---

## 6. Roadmap 서비스
- DB 유형: PostgreSQL
- DB Host: 20.214.121.121
- DB Port: 5432
- DB Username: homestarteruser
- DB Password: Vnawmd135*
- DB Name: roadmapdb

---

## 설치 요약

### PostgreSQL 데이터베이스 (5개)
| 서비스 | Host | Port | Database | Username | Password |
|--------|------|------|----------|----------|----------|
| user | 20.249.153.213 | 5432 | userdb | homestarteruser | Vnawmd135* |
| asset | 4.230.48.72 | 5432 | assetdb | homestarteruser | Vnawmd135* |
| loan | 4.230.159.143 | 5432 | loandb | homestarteruser | Vnawmd135* |
| housing | 4.230.65.89 | 5432 | housingdb | homestarteruser | Vnawmd135* |
| calculator | 20.214.121.121 | 5432 | calculatordb | homestarteruser | Vnawmd135* |
| roadmap | 20.214.121.121 | 5432 | roadmapdb | homestarteruser | Vnawmd135* |

---

## 접속 정보 확인

### PostgreSQL 접속 예시
```bash
psql -h 20.249.153.213 -p 5432 -U homestarteruser -d userdb
```

---

## 비고
- 모든 PostgreSQL 데이터베이스는 동일한 인증 정보를 사용합니다 (homestarteruser/Vnawmd135*)
- 개발 환경(dev)을 위한 설치 결과입니다
