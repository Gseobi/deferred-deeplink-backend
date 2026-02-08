package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;

import java.util.Optional;

public interface AppConfigQueryRepository {
    Optional<AppConfig> findByProvider(String provider);
}
