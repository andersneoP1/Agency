package com.nativetravel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Базовая конфигурация Spring Security для Native Travel.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/ws/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Статические ресурсы
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        // Swagger UI & OpenAPI
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Открытые страницы и API
                        .requestMatchers("/", "/tours/**", "/flights/**", "/compare/**", "/about", "/faq").permitAll()
                        .requestMatchers("/login", "/register", "/recovery/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/api/v1/subscriptions/**").permitAll()
                        // Все остальные запросы временно разрешены в Phase 0, в Phase 1 будут защищены
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
