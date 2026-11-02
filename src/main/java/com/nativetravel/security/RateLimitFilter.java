package com.nativetravel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.util.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

/**
 * Фильтр ограничения частоты запросов для эндпоинтов авторизации и восстановления пароля.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/v1/auth") || uri.startsWith("/recovery")) {
            String clientIp = extractClientIp(request);

            if (!rateLimitService.tryConsumeAuth(clientIp)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Слишком много запросов. Пожалуйста, подождите минуту перед следующей попыткой."
                );
                problem.setType(URI.create("urn:problem-type:rate-limit-exceeded"));
                problem.setTitle("Превышен лимит запросов");
                problem.setInstance(URI.create(uri));

                response.getWriter().write(objectMapper.writeValueAsString(problem));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return xfHeader != null ? xfHeader.trim() : request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
