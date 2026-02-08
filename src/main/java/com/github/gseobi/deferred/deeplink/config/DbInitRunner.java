package com.github.gseobi.deferred.deeplink.config;

import com.github.gseobi.deferred.deeplink.domain.entity.AppConfig;
import com.github.gseobi.deferred.deeplink.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class DbInitRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final AppConfigRepository appConfigRepository;

    @Override
    public void run(String... args) throws Exception {
        execSqlIfExists("db/oralce_schema.sql");
        execSqlIfExists("db/oracle_functions.sql");

        if (appConfigRepository.count() == 0) {
            appConfigRepository.save(AppConfig.builder()
                    .provider("app_a")
                    .appName("APP A")
                    .appIconUrl("https://placehold.co/96x96/png")
                    .androidScheme("appa://open")
                    .iosScheme("appa://open")
                    .playStoreUrl("https://play.google.com/store/apps/details?id=com.example.appa")
                    .appStoreUrl("https://apps.apple.com/app/id000000000")
                    .build());

            appConfigRepository.save(AppConfig.builder()
                    .provider("app_b")
                    .appName("APP B")
                    .appIconUrl("https://placehold.co/96x96/png")
                    .androidScheme("appb://open")
                    .iosScheme("appb://open")
                    .playStoreUrl("https://play.google.com/store/apps/details?id=com.example.appb")
                    .appStoreUrl("https://apps.apple.com/app/id111111111")
                    .build());
        }
    }

    private void execSqlIfExists(String path) {
        try {
            ClassPathResource r = new ClassPathResource(path);
            String sql = new String(r.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
        }
    }
}
