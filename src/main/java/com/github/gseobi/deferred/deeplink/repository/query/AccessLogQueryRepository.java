package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.AccessLog;

import java.util.Optional;

public interface AccessLogQueryRepository {
    Optional<AccessLog> findByAccessSeqAndCrypt(Long accessSeq, String crypt);
}