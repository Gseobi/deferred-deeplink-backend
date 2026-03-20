# Troubleshooting Notes

## 1. Overview

`deferred-deeplink-backend`는  
광고 링크 클릭 이후 앱 설치 및 최초 실행까지의 흐름을  
서버 기준으로 추적·검증하는 deferred deeplink 구조를 정리한 프로젝트입니다.

이 문서는 프로젝트 구현 및 검증 과정에서 중요하게 본 이슈와  
그에 대한 대응 방향을 정리한 문서입니다.

---

## 2. State Loss Between Click and First Launch

### Problem
deferred deeplink는 광고 클릭 시점과 앱 최초 실행 시점 사이에  
시간 차이가 발생할 수 있습니다.

이 과정에서 클릭 시점 상태를 적절히 보존하지 않으면  
설치 후 최초 실행 시 유입 사용자를 다시 식별하기 어려워질 수 있습니다.

### Why It Matters
일반 딥링크는 즉시 앱으로 연결되지만,  
deferred deeplink는 설치 전후 상태 연결이 핵심입니다.  
따라서 클릭 시점 정보 보존이 실패하면 전체 흐름이 끊길 수 있습니다.

### Response
본 프로젝트에서는 다음 구조를 사용했습니다.

- Click ID 발급
- crypt 토큰 생성
- 랜딩 페이지 진입 시 access_seq 생성
- 최초 실행 시 crypt + access_seq 검증

이를 통해 클릭 시점과 최초 실행 시점을 연결하는 구조를 구성했습니다.

---

## 3. Click ID Duplication Risk

### Problem
광고 클릭 식별자가 중복되면 유입 추적 정확도가 떨어질 수 있습니다.

### Why It Matters
deferred deeplink 구조에서는 Click ID가  
광고 유입 식별의 기준값 역할을 하므로,  
중복 발생 시 상태 추적과 검증 흐름 전체에 영향을 줄 수 있습니다.

### Response
본 프로젝트에서는 Click ID를 애플리케이션에서 직접 생성하지 않고  
DB Function에서 발급하는 방식으로 구성했습니다.

### Limitation
포트폴리오에서는 DB Function 예제를 통해 구조를 설명하는 데 초점을 두었으며,  
실제 운영 환경에서는 DBMS별 발급 정책, 성능, 관리 전략을 더 정교하게 다뤄야 할 수 있습니다.

---

## 4. Token Tampering Risk

### Problem
설치 전후 상태 연결을 위해 사용하는 전달값이  
평문 파라미터 형태로만 유지되면 위변조 위험이 커질 수 있습니다.

### Why It Matters
최초 실행 검증 시 전달값이 신뢰할 수 없으면  
잘못된 사용자 식별 또는 비정상 유입 처리로 이어질 수 있습니다.

### Response
본 프로젝트에서는 crypt 토큰을 사용해  
설치 전 상태를 암호화된 형태로 유지하고,  
최초 실행 시 서버 기준 검증이 가능하도록 구성했습니다.

### Future Improvement
- 토큰 만료 시간 명시
- 재사용 방지 정책
- 서명 기반 추가 검증
- 만료/위변조 로그 추적 강화

---

## 5. Invalid or Non-mobile Access

### Problem
deferred deeplink는 모바일 환경을 전제로 한 흐름인데,  
Windows / Mac 등 비모바일 환경에서 접근하는 경우  
정상적인 설치 및 앱 실행 흐름이 성립하지 않을 수 있습니다.

### Why It Matters
비정상 접근을 그대로 허용하면  
운영 로그에 노이즈가 증가하고,  
사용자 경험도 혼란스러워질 수 있습니다.

### Response
본 프로젝트에서는 User-Agent 기반 OS 판별을 통해  
Windows / Mac 접근 시 invalidRequest 페이지를 반환하고,  
모바일(Android / iOS) 환경에 한해 흐름을 허용했습니다.

### Limitation
User-Agent 기반 판별은 단순하고 실용적이지만,  
운영 환경에서는 보다 정교한 접근 제어와 예외 처리 정책이 필요할 수 있습니다.

---

## 6. Multi-app Routing Complexity

### Problem
하나의 백엔드가 여러 앱 또는 여러 광고 경로를 지원하면  
앱별 스토어 URL, 앱 스킴, 랜딩 방식, 검증 흐름이 달라질 수 있습니다.

### Why It Matters
분기 기준이 명확하지 않으면  
앱별 설정이 혼재되거나 잘못된 랜딩 흐름이 발생할 수 있습니다.

### Response
본 프로젝트에서는 `/install/{app_type}` 구조를 사용해  
광고 대상 앱을 구분하고,  
app_type 기준으로 설정을 분기하도록 설계했습니다.

### Future Improvement
- app_type별 설정 관리 고도화
- 채널 / 캠페인 파라미터 확장
- 다국어 / 환경별 설정 분리

---

## 7. Coupling Between WebView Page and App Flow

### Problem
하이브리드 WebView 기반 랜딩 페이지는  
앱 측 호출 흐름과 웹 측 페이지 동작이 서로 연결되어 있어  
양쪽 중 하나가 바뀌면 테스트와 운영 부담이 커질 수 있습니다.

### Why It Matters
앱-웹 연동은 브라우저 단독 페이지보다  
OS별 차이, WebView 정책, URL interception 방식 등에 영향을 받기 쉽습니다.

### Response
본 프로젝트에서는 WebView 페이지를 JSP 및 JavaScript 기반으로 구성하고,  
서버 검증 흐름과 연결되는 최소한의 페이지 역할에 집중했습니다.

또한 실제 협업 경험을 반영해 다음 관점을 고려했습니다.

- 앱-웹 호출 흐름 확인
- JavaScript / Ajax 기반 데이터 전달
- OS별 WebView 동작 차이 대응
- 페이지 역할을 서버 검증 흐름과 느슨하게 연결

### Note
이 프로젝트는 앱 구현 자체보다  
서버와 WebView가 연결되는 구조를 설명하는 데 초점을 두고 있습니다.

---

## 8. Query Complexity in Validation Flow

### Problem
최초 실행 검증 흐름에서는 단순 저장보다  
다중 조건 조회 및 검증 로직이 더 중요해질 수 있습니다.

### Why It Matters
조회/검증 로직이 단순 CRUD 계층에 섞이면  
조건이 늘어날수록 코드 가독성과 유지보수성이 떨어질 수 있습니다.

### Response
본 프로젝트에서는 저장과 조회/검증 책임을 나누기 위해  
JPA와 Querydsl 계층을 분리했습니다.

- 저장: JPA
- 조회 및 검증: Querydsl QueryRepository

이를 통해 복잡한 검증 조건을 별도 계층으로 분리했습니다.

---

## 9. Logging and Traceability Limitations

### Problem
deferred deeplink는 클릭, 랜딩, 최초 실행 검증까지  
여러 단계를 거치므로 추적 가능한 로그가 중요합니다.

### Why It Matters
운영 환경에서는 특정 사용자의 유입 흐름이나  
어느 단계에서 실패했는지를 파악할 수 있어야 합니다.

### Current Position
본 프로젝트는 Local / Prod 로그 분리 구조와 access 로그 흐름을 반영했지만,  
상용 수준의 추적 체계 전체를 구현하는 데 목적을 두지는 않았습니다.

### Future Improvement
- traceId 또는 correlation id 도입
- 단계별 structured logging 강화
- click / access / check 흐름 연결 로그 정교화
- 운영 메트릭 연계

---

## 10. What This Project Focuses On

이 프로젝트는 광고 플랫폼이나 앱 SDK 전체를 구현하는 것이 목적이 아닙니다.  
대신 다음 설계 포인트를 명확히 보여주는 데 초점을 두고 있습니다.

- 서버 주도형 deferred deeplink 흐름
- Click ID / crypt / access_seq를 통한 상태 연결
- app_type 기반 다중 앱 분기
- OS 기반 접근 제어
- JPA / Querydsl 책임 분리
- 하이브리드 WebView 협업 맥락 반영

즉, 이 문서에서 다루는 troubleshooting 포인트들은  
단순 예외 처리 목록이 아니라,  
설치 전후 상태 추적과 검증이 필요한 딥링크 구조에서  
실제로 중요할 수 있는 이슈를 기준으로 정리한 것입니다.

---

## 11. Summary

본 프로젝트에서 중요하게 본 이슈는 다음과 같습니다.

- 클릭 시점과 최초 실행 시점 사이의 상태 연결 문제
- Click ID 중복 가능성
- 전달 토큰 위변조 위험
- 비모바일 환경 접근 제어 문제
- 다중 앱 분기 복잡도
- WebView 페이지와 앱 흐름 간 결합 문제
- 검증 쿼리 복잡도
- 운영 로그와 추적성 한계

이를 통해 `deferred-deeplink-backend`는  
단순한 딥링크 예제가 아니라,  
광고 유입부터 최초 실행 검증까지의 상태 추적과 검증 흐름을  
운영 관점에서 정리한 포트폴리오로 구성되었습니다.
