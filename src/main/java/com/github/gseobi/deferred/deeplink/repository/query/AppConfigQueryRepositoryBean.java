package com.github.gseobi.deferred.deeplink.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class AppConfigQueryRepositoryBean extends AppConfigQueryRepositoryImpl {
    public AppConfigQueryRepositoryBean(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}
