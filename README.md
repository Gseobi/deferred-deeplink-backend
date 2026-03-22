# deferred-deeplink-backend

Spring Boot + JPA + Querydsl을 기반으로,
**광고 유입 이후 앱 설치 전/후가 분리되는 상황에서 deferred deeplink를 서버 중심으로 추적·검증하는 백엔드 프로젝트**입니다.

단순 딥링크 URL 생성이 아니라,
광고 클릭 → 설치 유도 → 앱 최초 실행 검증까지의 흐름을
서버 기준으로 일관되게 관리할 수 있도록 설계했습니다.

클라이언트 SDK나 외부 솔루션에 의존하지 않고,
**DB Function + 암호화 토큰 + 접근 이력 기반 검증 로직**을 중심으로 구현했습니다.

<br/>

## 1. What Problem This Project Solves

일반 딥링크는 이미 앱이 설치된 사용자를 대상으로 목적지 이동을 처리하지만,
deferred deeplink는 **앱이 없는 상태에서 링크를 클릭한 사용자의 설치 이후 흐름까지 연결**해야 합니다.

이 과정에서는 다음 문제가 중요합니다.

- 광고 클릭 시점 식별자 발급
- 설치 전 상태 보존
- 앱 최초 실행 시 위변조 방지
- 다중 앱 또는 다중 광고 경로 분기
- 접근 로그 및 운영 추적 가능성 확보

이 프로젝트는 위 흐름을 외부 SDK에 의존하지 않고,
**서버가 직접 식별·검증·분기할 수 있는 구조**로 재구성한 포트폴리오 프로젝트입니다.

<br/>

## 2. Verification Focus

이 프로젝트는 설치 전 유입 정보와 설치 후 앱 최초 실행 시점을
서버 기준으로 안전하게 연결하는 검증 흐름에 초점을 두고 있습니다.

- 광고 클릭 시점 식별자 발급 및 상태 저장
- crypt 기반 전달값 보호 및 검증
- access_seq 기반 접근 이력 추적
- app_type / OS 기준 분기 처리
- 비정상 요청 및 운영 관점 이슈 대응

자세한 내용은 아래 문서에 정리했습니다.

- [Test Report](docs/test-report.md)
- [Troubleshooting Notes](docs/troubleshooting.md)
- [Design Notes](docs/design-notes.md)

<br/>

## 3. Key Design Points

### Deferred Deeplink Server Flow
- 광고 링크 클릭 시 서버에서 Click ID 발급
- Click 정보를 기반으로 암호화된 crypt 토큰 생성
- 앱 설치 이후 최초 실행 시 crypt + access_seq + user_pin 조합으로 유입 사용자 검증

### DB Function-based Click ID Generation
- Click ID는 애플리케이션이 아닌 DB Function에서 발급
- 식별자 발급 책임을 DB 레벨로 분리
- PostgreSQL / MySQL / Oracle 예제를 함께 정리

### app_type-based Multi-app Routing
- `/install/{app_type}` 구조를 기준으로 앱별 진입 분리
- app_type에 따라 스토어 URL, 앱 스킴, 랜딩 정보 분기
- 하나의 백엔드에서 복수 앱 유입 처리 가능하도록 설계

### OS-based Access Control
- User-Agent 기반으로 OS를 판별
- 모바일(Android / iOS) 환경만 정상 처리 대상으로 허용
- Windows / Mac 환경은 invalidRequest 페이지로 분기

### JPA + Querydsl Responsibility Split
- 저장 로직은 Spring Data JPA가 담당
- 조회·검증 로직은 Querydsl 전용 QueryRepository로 분리
- 조건 기반 조회와 검증 책임을 별도 계층에 집중

<br/>

## 4. Supported Flow

### 1) 광고 링크 클릭
- `GET /install/{app_type}`
- 서버에서 Click ID 발급 및 crypt 생성
- crypt를 포함한 랜딩 페이지로 Redirect

### 2) 랜딩 페이지 진입
- `GET /install/landing`
- crypt 복호화 및 접근 로그(access_seq) 생성
- 앱 실행 시도 후, 미설치 시 스토어 이동

### 3) 앱 최초 실행 검증
- `GET /install/check`
- crypt + access_seq + user_pin 검증
- deferred deeplink 유입 사용자 여부 확정

<br/>

## 5. Hybrid WebView Integration Context

이 프로젝트는 앱 자체를 구현하는 목적의 프로젝트가 아니라,
**하이브리드 WebView 환경에서 서버와 웹 페이지가 어떻게 연결되고 검증되는지**를
백엔드 관점에서 정리한 프로젝트입니다.

실제 협업 경험을 반영하여 다음과 같은 요소를 함께 고려했습니다.

- WebView 기반 랜딩 페이지 동작
- JavaScript / Ajax 기반 데이터 전달 및 후속 처리
- 앱 측 URL interception 흐름과의 연계
- OS별 WebView 동작 차이에 따른 테스트 및 운영 대응

즉, Flutter 앱 구현 자체보다
**서버 + WebView + 앱 연계 흐름의 검증 구조**를 중심으로 정리한 포트폴리오입니다.

<br/>

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

<br/>

## 7. Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Querydsl
- JSP
- JavaScript / Ajax
- Oracle / PostgreSQL / MySQL
- Logback

<br/>

## 8. Future Improvements

- 토큰 만료 및 재사용 방지 정책 보강
- Click / Access 추적 메트릭 강화
- 다중 광고 채널 파라미터 추상화
- 운영 로그 및 error response 구조 정리
- WebView 연동 흐름 문서화 보강
- 동일 Client 필터링

<br/>

## 9. Documents

- [Test Report](docs/test-report.md)
- [Troubleshooting Notes](docs/troubleshooting.md)
- [Design Notes](docs/design-notes.md)