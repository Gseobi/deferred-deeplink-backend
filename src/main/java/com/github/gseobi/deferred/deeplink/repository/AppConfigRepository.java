package com.github.gseobi.deferred.deeplink.repository;

import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
}
