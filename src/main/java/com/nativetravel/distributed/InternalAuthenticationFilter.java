package com.nativetravel.distributed;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class InternalAuthenticationFilter extends OncePerRequestFilter {
    private final byte[] token;
    public InternalAuthenticationFilter(@Value("${app.internal-token}") String token) {
        if (token.isBlank()) throw new IllegalArgumentException("app.internal-token must not be blank");
        this.token = token.getBytes(StandardCharsets.UTF_8);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) {
            String supplied = request.getHeader("X-Service-Token");
            if (supplied == null || !MessageDigest.isEqual(token, supplied.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(403, "Service authentication required");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
