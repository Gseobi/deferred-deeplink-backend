package com.github.gseobi.deferred.deeplink.repository;

import com.github.gseobi.deferred.deeplink.domain.entity.ClickReferrer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClickReferrerRepository extends JpaRepository<ClickReferrer, String> {
    @Query(value = "select fn_next_click_id()", nativeQuery = true)
    String nextClickId();
}
