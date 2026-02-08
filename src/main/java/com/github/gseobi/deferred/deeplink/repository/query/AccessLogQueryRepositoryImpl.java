package com.github.gseobi.deferred.deeplink.repository.query;

import com.github.gseobi.deferred.deeplink.domain.entity.AccessLog;
import com.github.gseobi.deferred.deeplink.domain.entity.QAccessLog;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class AccessLogQueryRepositoryImpl implements AccessLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<AccessLog> findByAccessSeqAndCrypt(Long accessSeq, String crypt) {
        QAccessLog a = QAccessLog.accessLog;

        return Optional.ofNullable(
                queryFactory.selectFrom(a)
                        .where(a.accessSeq.eq(accessSeq)
                                .and(a.crypt.eq(crypt)))
                        .fetchOne()
        );
    }
}