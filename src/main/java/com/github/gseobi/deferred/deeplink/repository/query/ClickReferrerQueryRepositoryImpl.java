package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.ClickReferrer;
import com.github.gseobi.deferred.deeplink.domain.entity.QClickReferrer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ClickReferrerQueryRepositoryImpl implements ClickReferrerQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ClickReferrer> findByCrypt(String crypt) {
        QClickReferrer c = QClickReferrer.clickReferrer;

        return Optional.ofNullable(
                queryFactory.selectFrom(c)
                        .where(c.crypt.eq(crypt))
                        .fetchFirst()
        );
    }
}