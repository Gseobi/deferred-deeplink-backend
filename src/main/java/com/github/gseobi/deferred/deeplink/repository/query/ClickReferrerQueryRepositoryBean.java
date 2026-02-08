package com.github.gseobi.deferred.deeplink.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ClickReferrerQueryRepositoryBean extends ClickReferrerQueryRepositoryImpl {
    public ClickReferrerQueryRepositoryBean(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}