package com.nativetravel.distributed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Map;

/** Shared bootstrap, but each executable contains only its own domain implementation. */
@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EntityScan("com.nativetravel.entity")
@EnableJpaRepositories("com.nativetravel.repository")
@ComponentScan(basePackages = {"com.nativetravel.config", "com.nativetravel.controller",
        "com.nativetravel.service", "com.nativetravel.security", "com.nativetravel.util",
        "com.nativetravel.mapper", "com.nativetravel.exception", "com.nativetravel.distributed"})
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class ServiceRuntime {
    public static void start(String name, int port, String[] args) {
        System.setProperty("spring.config.name", "service");
        SpringApplication app = new SpringApplication(ServiceRuntime.class);
        app.setDefaultProperties(Map.of("spring.application.name", name,
                "server.port", port, "app.service.kind", name, "app.distributed", true));
        app.run(args);
    }
}
