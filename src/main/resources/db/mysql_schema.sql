-- Click ID 발급용 sequence
CREATE TABLE deeplink_click_id_seq (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);

-- 광고앱(app_type)별 분기 설정 테이블
CREATE TABLE dl_app_config (
    provider        VARCHAR(50) PRIMARY KEY,
    app_name        VARCHAR(100) NOT NULL,
    app_icon_url    TEXT,
    android_scheme  TEXT NOT NULL,
    ios_scheme      TEXT NOT NULL,
    play_store_url  TEXT NOT NULL,
    app_store_url   TEXT NOT NULL
) ENGINE=InnoDB;

-- 클릭 정보(광고 클릭 -> 서버에서 crypt 발급 시 저장)
CREATE TABLE dl_click_referrer (
    click_id      VARCHAR(30) PRIMARY KEY,
    provider      VARCHAR(50) NOT NULL,
    campaign_id   VARCHAR(100) NOT NULL,
    os            VARCHAR(20) NOT NULL,
    client_ip     VARCHAR(50),
    user_agent    TEXT,
    crypt         TEXT NOT NULL,
    payload_json  TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_click_crypt ON dl_click_referrer (crypt);

-- 랜딩 접근 / 최초 실행 검증 로그
CREATE TABLE dl_access_log (
    access_seq     BIGINT AUTO_INCREMENT PRIMARY KEY,
    click_id       VARCHAR(30),
    crypt          TEXT NOT NULL,
    platform       VARCHAR(20),
    user_pin           VARCHAR(100),
    verified_yn    TINYINT(1) DEFAULT 0,
    landing_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    first_open_at  TIMESTAMP,
    CONSTRAINT fk_access_click
        FOREIGN KEY (click_id) REFERENCES dl_click_referrer(click_id)
) ENGINE=InnoDB;

CREATE INDEX idx_access_crypt ON dl_access_log (crypt);

