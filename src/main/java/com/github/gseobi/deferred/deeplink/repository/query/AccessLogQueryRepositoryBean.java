package com.github.gseobi.deferred.deeplink.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class AccessLogQueryRepositoryBean extends AccessLogQueryRepositoryImpl {
    public AccessLogQueryRepositoryBean(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}