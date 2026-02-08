package com.github.gseobi.deferred.deeplink.domain.entity;

import lombok.*;

import jakarta.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dl_app_config")
public class AppConfig {

    @Id
    @Column(name = "provider", length = 50)
    private String provider; // app_type

    @Column(name = "app_name", nullable = false, length = 100)
    private String appName;

    @Column(name = "app_icon_url")
    private String appIconUrl;

    @Column(name = "android_scheme", nullable = false)
    private String androidScheme;

    @Column(name = "ios_scheme", nullable = false)
    private String iosScheme;

    @Column(name = "play_store_url", nullable = false)
    private String playStoreUrl;

    @Column(name = "app_store_url", nullable = false)
    private String appStoreUrl;
}
