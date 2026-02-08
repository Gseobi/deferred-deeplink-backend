package com.github.gseobi.deferred.deeplink.domain.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dl_click_referrer")
public class ClickReferrer {

    @Id
    @Column(name = "click_id", length = 30)
    private String clickId;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "campaign_id", nullable = false, length = 100)
    private String campaignId;

    @Column(name = "os", nullable = false, length = 20)
    private String os;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "crypt", nullable = false)
    private String crypt;

    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
