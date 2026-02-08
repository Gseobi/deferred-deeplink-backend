package com.github.gseobi.deferred.deeplink.repository;

import com.github.gseobi.deferred.deeplink.domain.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
