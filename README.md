# deferred-deeplink-backend

Spring Boot + JPA + Querydsl을 활용하여  
**지연 딥링크(Deferred Deep Link) 백엔드 처리 흐름**를 구현한 포트폴리오 프로젝트입니다.

앱 미설치 상태에서 광고 링크를 클릭한 사용자를 대상으로  
설치 → 최초 실행까지의 흐름을 서버 기준으로 추적·검증하는 구조를 설계했습니다.

클라이언트 SDK나 외부 서비스에 의존하지 않고,  
**DB Function + 암호화 토큰 + 서버 검증 로직**을 중심으로 구현했습니다.

---

## 프로젝트 목적
- 지연 딥링크(Deferred Deep Link) 서버 처리 구조 설계
- DB Function 기반 Click ID 발급 패턴 정리
- 서버 주도(Server-driven) 상태 관리 흐름 구현
- JPA + Querydsl 기반 조회/검증 로직 구성
- app_type(PathVariable)를 활용한 다중 앱 광고 분기 처리
- Local / Prod 환경 분리 및 로깅 전략 구성

---

## 패키지 구조
com.github.gseobi.deferred.deeplink
- config
  - QuerydslConfig.java
  - DbInitRunner.java
- controller
  - DeepLinkController.java
- domain
  - entity
    - AppConfig.java
    - ClickReferrer.java
    - AccessLog.java
  - enums
    - ClientOs.java
    - StoreType.java
- repository
  - AppConfigRepository.java
  - ClickReferrerRepository.java
  - AccessLogRepository.java
  - query
    - AppConfigQueryRepository.java
    - AppConfigQueryRepositoryImpl.java
    - AppConfigQueryRepositoryBean.java
    - ClickReferrerQueryRepository.java
    - ClickReferrerQueryRepositoryImpl.java
    - ClickReferrerQueryRepositoryBean.java
    - AccessLogQueryRepository.java
    - AccessLogQueryRepositoryImpl.java
    - AccessLogQueryRepositoryBean.java
- service
  - DeepLinkService.java
- util
  - ClientUtils.java
  - AES256Cipher.java
  - JsonUtils.java
- DeferredDeeplinkApplication.java

---

## 핵심 설계 포인트

### ✔ 지연 딥링크 서버 처리 흐름
- 광고 링크 클릭 시 서버에서 Click ID 발급
- 암호화된 crypt 토큰을 생성하여 설치 전 상태 보존
- 앱 설치 이후 최초 실행 시 crypt + access_seq 검증을 통해
  지연 딥링크 유입 사용자 식별

---

### ✔ DB Function 기반 Click ID 발급
- Click ID는 애플리케이션에서 생성하지 않고 DB Function에서 발급
- 동시성 환경에서 중복 ID 발생 방지
- DB별(PostgreSQL / MySQL / Oracle) Function 예제 제공

---

### ✔ app_type(PathVariable) 기반 다중 앱 분기
- `/install/{app_type}` 구조로 광고 대상 앱 식별
- app_type을 기준으로 스토어 URL, 앱 스킴, 랜딩 정보 분기
- 하나의 백엔드에서 다수 앱 광고 유입 처리 가능

---

### ✔ OS 기반 접근 제어
- User-Agent 기반 OS 판별
- Windows / Mac 접근 시 invalidRequest 페이지 반환
- 모바일(Android / IOS) 환경만 지연 딥링크 처리 허용

---

### ✔ JPA + Querydsl 조회/검증 구조
- 저장 로직: Spring Data JPA
- 조회/검증 로직: Querydsl 전용 QueryRepository 분리
- 조회 전용 로직을 Impl 레이어에 집중하여 책임 분리

---

## 지연 딥링크 처리 흐름

### 1️⃣ 광고 링크 클릭
- EndPoint : GET /install/{app_type}
- 서버에서 Click ID 발급 및 crypt 생성
- crypt를 포함한 랜딩 페이지로 Redirect

### 2️⃣ 랜딩 페이지 진입
- EndPoint : GET /install/landing
- crypt 복호화 및 접근 로그(access_seq) 생성
- 앱 실행 시도 → 미설치 시 스토어 이동

### 3️⃣ 앱 최초 실행 검증
- EndPoint : GET /install/check
- crypt + access_seq + user_pin(내부 고객 고유 키 예시 Parameter) 검증
- 지연 딥링크 유입 사용자 확정

---

## 사용 기술
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Querydsl
- JSP (Landing / Invalid Page)
- Oracle / PostgreSQL / MySQL (DDL 및 Function 예제 제공)
- Logback (Local / Prod 분리)

---

## 정리
광고 유입부터 앱 설치, 최초 실행까지의 흐름을  
클라이언트 의존 없이 서버 중심으로 관리하는  
지연 딥링크 백엔드 구조를 포트폴리오 형태로 정리한 프로젝트입니다.

실제 서비스 환경에서 고려해야 하는  
동시성, 위변조 방지, 운영 로그, 다중 앱 분기 구조를  
단순 예제가 아닌 실무 관점에서 설계했습니다.
