package com.nativetravel.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) { SpringApplication.run(DiscoveryServerApplication.class, args); }
}
