package com.nativetravel.config;

import com.nativetravel.security.JwtAuthenticationFilter;
import com.nativetravel.security.RateLimitFilter;
import com.nativetravel.util.CookieUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                        .ignoringRequestMatchers("/api/**", "/internal/**", "/ws/**", "/actuator/**", "/logout")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (!request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                                response.sendRedirect(request.getContextPath() + "/login");
                                return;
                            }
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
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers("/internal/**").permitAll()
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
                        .requestMatchers("/profile/**", "/cart/**", "/booking/**", "/payment/**", "/survey", "/favorites").authenticated()
                        // Админка
                        .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                        // Все остальные запросы
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/?logout=true")
                        .addLogoutHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            HttpSession session = request.getSession(false);
                            if (session != null) {
                                session.invalidate();
                            }
                            ResponseCookie delAccess = CookieUtils.deleteAccessCookie();
                            ResponseCookie delRefresh = CookieUtils.deleteRefreshCookie();
                            ResponseCookie delRefreshRoot = ResponseCookie.from(CookieUtils.REFRESH_TOKEN_COOKIE, "")
                                    .httpOnly(true)
                                    .path("/")
                                    .maxAge(0)
                                    .sameSite("Strict")
                                    .build();
                            response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());
                            response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());
                            response.addHeader(HttpHeaders.SET_COOKIE, delRefreshRoot.toString());
                        })
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", CookieUtils.ACCESS_TOKEN_COOKIE, CookieUtils.REFRESH_TOKEN_COOKIE)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
