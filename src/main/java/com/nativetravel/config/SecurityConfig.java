package com.nativetravel.config;

import com.nativetravel.security.JwtAuthenticationFilter;
import com.nativetravel.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Основная конфигурация Spring Security для Native Travel.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/ws/**", "/actuator/**")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            org.springframework.http.ProblemDetail problem = org.springframework.http.ProblemDetail.forStatusAndDetail(
                                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Требуется авторизация в системе"
                            );
                            problem.setType(java.net.URI.create("urn:problem-type:unauthorized"));
                            problem.setTitle("Не авторизован");
                            problem.setInstance(java.net.URI.create(request.getRequestURI()));
                            new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), problem);
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Статические ресурсы
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        // Swagger UI & OpenAPI
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()
                        // Открытые публичные веб-страницы
                        .requestMatchers("/", "/tours/**", "/flights/**", "/compare/**", "/about", "/faq").permitAll()
                        // Веб-страницы авторизации и сброса
                        .requestMatchers("/login", "/register", "/recovery/**").permitAll()
                        // Публичные эндпоинты API авторизации и подписок
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/recovery",
                                "/api/v1/auth/recovery/confirm",
                                "/api/v1/subscriptions/**"
                        ).permitAll()
                        // Защищенный профиль текущего пользователя
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        // Защищенные разделы приложения (личный кабинет, корзина, бронь)
                        .requestMatchers("/profile/**", "/cart/**", "/booking/**", "/payment/**").authenticated()
                        // Админка
                        .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                        // Все остальные запросы
                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
