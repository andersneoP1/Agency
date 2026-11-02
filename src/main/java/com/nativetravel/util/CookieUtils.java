package com.nativetravel.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

/**
 * Утилитный класс для безопасной работы с HttpOnly Cookie.
 */
public final class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE = "NT_ACCESS";
    public static final String REFRESH_TOKEN_COOKIE = "NT_REFRESH";

    private CookieUtils() {
    }

    /**
     * Создает безопасную HttpOnly ResponseCookie для access-токена.
     *
     * @param token access-токен
     * @param maxAgeSeconds время жизни в секундах
     * @return ResponseCookie
     */
    public static ResponseCookie createAccessCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false) // В dev разрешен HTTP, в prod через прокси/SSL переопределяется
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    /**
     * Создает безопасную HttpOnly ResponseCookie для refresh-токена.
     *
     * @param token refresh-токен
     * @param maxAgeSeconds время жизни в секундах
     * @return ResponseCookie
     */
    public static ResponseCookie createRefreshCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    /**
     * Создает ResponseCookie для удаления access-токена.
     *
     * @return ResponseCookie с нулевым временем жизни
     */
    public static ResponseCookie deleteAccessCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    /**
     * Создает ResponseCookie для удаления refresh-токена.
     *
     * @return ResponseCookie с нулевым временем жизни
     */
    public static ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /**
     * Извлекает значение cookie по имени из HttpServletRequest.
     *
     * @param request HTTP-запрос
     * @param name имя cookie
     * @return Optional со значением cookie
     */
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
