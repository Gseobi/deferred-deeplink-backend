# Test Report

## 1. Overview

본 문서는 `deferred-deeplink-backend` 프로젝트에서 검증한 주요 시나리오를 정리한 문서입니다.

이 프로젝트는 광고 링크 클릭 이후 앱 설치 및 최초 실행까지의 흐름을  
서버 기준으로 추적·검증하는 deferred deeplink 구조를 설명하는 데 목적이 있습니다.

검증 목적은 다음과 같습니다.

- Click ID 발급과 crypt 생성 흐름 확인
- app_type 기준 앱 분기 확인
- OS 기반 접근 제어 확인
- 랜딩 페이지 진입 시 access_seq 생성 흐름 확인
- 최초 실행 시 crypt + access_seq 검증 확인
- JPA / Querydsl 기반 저장 및 조회 흐름 확인

---

## 2. Test Environment

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Querydsl
- JSP
- JavaScript / Ajax
- Local Profile

---

## 3. Test Scenarios

### 3.1 Click ID Generation on Install Entry

**Scenario**  
광고 링크 진입 시 `GET /install/{app_type}` 요청을 통해  
Click ID가 발급되고 crypt가 생성되는지 확인

**Expected**
- app_type 기준 분기 가능
- Click ID 발급 정상 동작
- crypt 생성 후 랜딩 페이지 흐름으로 연결

**Result**
- 정상 동작 확인

---

### 3.2 app_type-based Routing

**Scenario**  
서로 다른 app_type 값으로 요청했을 때  
스토어 URL, 앱 스킴, 랜딩 정보가 올바르게 분기되는지 확인

**Expected**
- app_type 기준 분기 정상 동작
- 다중 앱 광고 유입 처리 가능

**Result**
- 정상 동작 확인

---

### 3.3 OS-based Access Control

**Scenario**  
User-Agent 기준으로 모바일 / 비모바일 접근을 구분하고,  
모바일 환경만 deferred deeplink 흐름을 허용하는지 확인

**Expected**
- Android / iOS 환경은 정상 처리
- Windows / Mac 접근 시 invalidRequest 페이지 반환

**Result**
- 정상 동작 확인

---

### 3.4 Landing Page Entry and Access Sequence Creation

**Scenario**  
`GET /install/landing` 진입 시 crypt 복호화와 함께  
접근 로그 및 access_seq가 생성되는지 확인

**Expected**
- crypt 복호화 가능
- access_seq 생성
- 접근 이력 저장
- 앱 실행 또는 스토어 이동 흐름 연결

**Result**
- 정상 동작 확인

---

### 3.5 First Launch Validation

**Scenario**  
`GET /install/check` 호출 시  
crypt + access_seq + user_pin 조합을 기준으로 최초 실행 검증이 가능한지 확인

**Expected**
- 전달값 검증 정상 수행
- deferred deeplink 유입 사용자 식별 가능
- 비정상 요청은 검증 실패 처리

**Result**
- 정상 동작 확인

---

### 3.6 JPA and Querydsl Responsibility Split

**Scenario**  
저장 로직과 조회/검증 로직이 분리된 구조로 동작하는지 확인

**Expected**
- 저장은 JPA 중심으로 수행
- 조회/검증은 Querydsl QueryRepository 중심으로 수행
- 책임 분리 구조 유지

**Result**
- 정상 동작 확인

---

### 3.7 Hybrid WebView Context Support

**Scenario**  
JSP 기반 랜딩 페이지와 JavaScript / Ajax 로직이  
하이브리드 WebView 환경과 연계 가능한 흐름으로 구성되어 있는지 확인

**Expected**
- 랜딩 페이지 기반 후속 흐름 연결 가능
- 앱-웹 호출 및 데이터 전달 테스트 가능
- 서버 검증 흐름과 WebView 페이지 구조가 연결됨

**Result**
- 정상 동작 확인

---

## 4. Verification Summary

본 프로젝트에서는 다음 항목을 확인했습니다.

- Click ID 발급 및 crypt 생성 흐름
- app_type 기반 다중 앱 분기
- OS 기반 접근 제어
- 랜딩 페이지 진입 및 access_seq 생성
- 최초 실행 검증 흐름
- JPA / Querydsl 기반 책임 분리
- 하이브리드 WebView 연계 가능 구조

이를 통해 `deferred-deeplink-backend`는  
단순 딥링크 샘플이 아니라,  
광고 유입부터 설치 및 최초 실행 검증까지의 흐름을  
서버 기준으로 추적·검증할 수 있는 구조로 동작함을 확인했습니다.

---

## 5. Notes

- 본 프로젝트는 외부 광고 SDK 의존 없이 서버 중심 구조를 기준으로 검증했습니다.
- 실제 광고 플랫폼 연동 정보나 고객 식별 규칙은 포함하지 않았습니다.
- 하이브리드 WebView 관련 검증은 앱 구현 자체보다 웹 페이지 및 서버 흐름 연계 관점에 초점을 두었습니다.
