package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;
import com.github.gseobi.deferred.deeplink.domain.entity.QAppConfig;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class AppConfigQueryRepositoryImpl implements AppConfigQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<AppConfig> findByProvider(String provider) {
        QAppConfig a = QAppConfig.appConfig;

        return Optional.ofNullable(
                queryFactory.selectFrom(a)
                        .where(a.provider.eq(provider))
                        .fetchOne()
        );
    }
}
