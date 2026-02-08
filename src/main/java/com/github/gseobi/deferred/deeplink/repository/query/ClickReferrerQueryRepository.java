package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.ClickReferrer;

import java.util.Optional;

public interface ClickReferrerQueryRepository {
    Optional<ClickReferrer> findByCrypt(String crypt);
}
