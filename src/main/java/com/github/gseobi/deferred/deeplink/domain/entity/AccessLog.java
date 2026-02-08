package com.github.gseobi.deferred.deeplink.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dl_access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_seq")
    private Long accessSeq;

    @Column(name = "click_id", nullable = false, length = 30)
    private String clickId;

    @Column(name = "crypt", nullable = false)
    private String crypt;

    @Column(name = "platform", length = 20)
    private String platform;

    @Column(name = "userPin", length = 100)
    private String userPin;

    @Column(name = "verified_yn", nullable = false)
    private boolean verifiedYn;

    @Column(name = "landing_at", nullable = false)
    private OffsetDateTime landingAt;

    @Column(name = "first_open_at")
    private OffsetDateTime firstOpenAt;

    public void verify(String userPin, String platform) {
        this.userPin = userPin;
        this.platform = platform;
        this.verifiedYn = true;
        this.firstOpenAt = OffsetDateTime.now();
    }
}
