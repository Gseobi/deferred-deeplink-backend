package com.github.gseobi.deferred.deeplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DeferredDeeplinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeferredDeeplinkApplication.class, args);
    }
}
