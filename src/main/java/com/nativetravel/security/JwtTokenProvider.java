package com.nativetravel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Провайдер для генерации, валидации и извлечения данных из JWT-токенов.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:c3VwZXJTZWNyZXRUb2tlbktleUZvck5hdGl2ZVRyYXZlbEFwcGxpY2F0aW9uMjAyNlNlY3VyZUtleUZvcldlYg==}") String secret,
            @Value("${app.jwt.expiration-ms:900000}") long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs
    ) {
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;

        SecretKey parsedKey;
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            parsedKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            parsedKey = Keys.hmacShaKeyFor(keyBytes);
        }
        this.key = parsedKey;
    }

    /**
     * Генерирует access-токен для пользователя.
     *
     * @param userDetails данные пользователя
     * @return строка JWT access-токена
     */
    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("token_type", "ACCESS")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Генерирует refresh-токен для пользователя.
     *
     * @param email адрес электронной почты
     * @return строка JWT refresh-токена
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("token_type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Извлекает email (subject) из токена.
     *
     * @param token JWT токен
     * @return email пользователя
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Проверяет валидность и срок действия JWT-токена.
     *
     * @param token JWT токен
     * @return true, если токен корректен и не просрочен
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, что токен является именно refresh-токеном.
     *
     * @param token JWT токен
     * @return true, если это валидный refresh-токен
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return "REFRESH".equals(claims.get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Возвращает время жизни access-токена в миллисекундах.
     */
    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    /**
     * Возвращает время жизни refresh-токена в миллисекундах.
     */
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
