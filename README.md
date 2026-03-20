# deferred-deeplink-backend

Spring Boot + JPA + Querydsl을 활용하여  
**지연 딥링크(Deferred Deep Link) 백엔드 처리 흐름**을 구현한 포트폴리오 프로젝트입니다.

앱 미설치 상태에서 광고 링크를 클릭한 사용자를 대상으로  
설치 → 최초 실행까지의 흐름을 서버 기준으로 추적·검증하는 구조를 설계했습니다.

클라이언트 SDK나 외부 서비스에 의존하지 않고,  
**DB Function + 암호화 토큰 + 서버 검증 로직**을 중심으로 구현했습니다.

---

## 1. Project Overview

이 프로젝트는 광고 링크 클릭 이후  
앱이 설치되지 않은 사용자에 대해 설치 및 최초 실행까지의 흐름을  
서버 기준으로 추적·검증하는 deferred deeplink 백엔드 구조를 재구성한 포트폴리오 프로젝트입니다.

핵심 목적은 단순히 딥링크 URL을 만드는 것이 아니라,  
광고 유입 → 설치 유도 → 최초 실행 검증까지의 상태를  
서버가 일관되게 관리할 수 있도록 하는 것입니다.

본 프로젝트는 다음과 같은 요소를 중심으로 구성했습니다.

- DB Function 기반 Click ID 발급
- 암호화된 crypt 토큰 생성
- access sequence 기반 접근 이력 관리
- app_type 기반 다중 앱 분기
- OS 기반 접근 제어
- JPA / Querydsl 기반 조회 및 검증 구조 분리

---

## 2. Why This Project

deferred deeplink는 앱이 이미 설치된 사용자를 대상으로 한 일반 딥링크와 달리,  
앱이 없는 상태에서 광고를 클릭한 사용자의 설치 이후 흐름까지 연결해야 합니다.

이 과정에서는 다음과 같은 문제가 중요합니다.

- 광고 클릭 시점 식별자 발급
- 설치 전 상태 보존
- 최초 실행 시 위변조 방지
- 다중 앱 또는 다중 광고 경로 분기
- 접근 로그 및 운영 추적 가능성

이 프로젝트는 이러한 문제를  
외부 SDK 의존 없이 서버 중심으로 처리하는 구조를 목표로 했습니다.

---

## 3. Key Design Points

### Deferred Deeplink Server Flow
- 광고 링크 클릭 시 서버에서 Click ID 발급
- 암호화된 crypt 토큰 생성
- 앱 설치 이후 최초 실행 시 crypt + access_seq 검증을 통해 유입 사용자 식별

### DB Function-based Click ID Generation
- Click ID는 애플리케이션이 아닌 DB Function에서 발급
- 동시성 환경에서의 중복 ID 발생 가능성 완화
- PostgreSQL / MySQL / Oracle 예제 포함

### app_type-based Multi-app Routing
- `/install/{app_type}` 구조를 통해 광고 대상 앱 식별
- app_type 기준으로 스토어 URL, 앱 스킴, 랜딩 정보 분기
- 하나의 백엔드에서 다수 앱 광고 유입 처리 가능

### OS-based Access Control
- User-Agent 기반 OS 판별
- Windows / Mac 접근 시 invalidRequest 페이지 반환
- 모바일(Android / iOS) 환경에 한해 deferred deeplink 처리 허용

### JPA + Querydsl Responsibility Split
- 저장 로직은 Spring Data JPA 담당
- 조회 및 검증 로직은 Querydsl 전용 QueryRepository로 분리
- 조회 전용 조건 및 검증 흐름을 별도 계층에 집중

---

## 4. Supported Flow

### 1. 광고 링크 클릭
- `GET /install/{app_type}`
- 서버에서 Click ID 발급 및 crypt 생성
- crypt를 포함한 랜딩 페이지로 Redirect

### 2. 랜딩 페이지 진입
- `GET /install/landing`
- crypt 복호화 및 접근 로그(access_seq) 생성
- 앱 실행 시도 후, 미설치 시 스토어 이동

### 3. 앱 최초 실행 검증
- `GET /install/check`
- crypt + access_seq + user_pin 검증
- deferred deeplink 유입 사용자 확정

---

## 5. Hybrid WebView Integration Context

이 프로젝트는 앱 자체를 개발하는 목적의 프로젝트는 아닙니다.  
대신 앱 개발자와 협업하면서 하이브리드 WebView 환경에서 사용할  
JSP 및 JavaScript 기반 페이지를 제작하고,  
앱-웹 간 호출 및 데이터 전달 흐름을 테스트·운영했던 경험을 반영하고 있습니다.

특히 실제 협업 과정에서는 다음과 같은 점을 함께 고려했습니다.

- WebView 기반 랜딩 페이지 동작
- JavaScript / Ajax 기반 데이터 전달 및 후속 처리
- 앱 측 URL interception 흐름과의 연계
- OS별 WebView 동작 차이에 따른 테스트 및 운영 대응

즉, 이 프로젝트는 Flutter 앱 자체를 구현한 것이 아니라,  
하이브리드 앱 환경에서 서버와 WebView가 어떻게 연결되고 검증되는지를  
백엔드 및 웹 페이지 관점에서 정리한 구조입니다.

---

## 6. Package Structure

```text
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
```

---

## 7. Tech Stack
- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Querydsl**
- **JSP (Landing / Invalid Page)**
- **JavaScript / Ajax**
- **Oracle / PostgreSQL / MySQL (DDL 및 Function 예제 제공)**
- **Logback (Local / Prod 분리)**

---

## 8. Future Improvements

- 토큰 만료 및 재사용 방지 정책 보강
- Click / Access 추적 메트릭 강화
- 다중 광고 채널 파라미터 추상화
- error response 및 운영 로그 구조 정리
- 하이브리드 WebView 연동 흐름 문서화 보강

---

## 9. Documents

- [Design Notes](docs/design-notes.md)
- [Test Report](docs/test-report.md)
- [Error Handling Notes](docs/error-handling.md)
