package com.nativetravel.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис ограничения частоты запросов (Rate Limiting) на базе Bucket4j.
 */
@Service
public class RateLimitService {

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    /**
     * Сбрасывает состояние всех бакетов (используется для тестов).
     */
    public void reset() {
        authBuckets.clear();
        generalBuckets.clear();
    }

    /**
     * Проверяет и списывает токен для чувствительных запросов аутентификации (вход, регистрация, восстановление).
     * Лимит: 10 запросов в минуту на один IP.
     *
     * @param clientIp IP-адрес клиента
     * @return true, если запрос разрешен
     */
    public boolean tryConsumeAuth(String clientIp) {
        Bucket bucket = authBuckets.computeIfAbsent(clientIp, this::createAuthBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Проверяет и списывает токен для общих запросов.
     * Лимит: 60 запросов в минуту на один IP.
     *
     * @param clientIp IP-адрес клиента
     * @return true, если запрос разрешен
     */
    public boolean tryConsumeGeneral(String clientIp) {
        Bucket bucket = generalBuckets.computeIfAbsent(clientIp, this::createGeneralBucket);
        return bucket.tryConsume(1);
    }

    private Bucket createAuthBucket(String key) {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createGeneralBucket(String key) {
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
