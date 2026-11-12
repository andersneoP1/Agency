package com.nativetravel.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class GatewayRequestLogging implements GlobalFilter, Ordered {
    public int getOrder() { return -100; }
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        String id = incoming != null && incoming.matches("[A-Za-z0-9_-]{1,64}")
                ? incoming : UUID.randomUUID().toString();
        var request = exchange.getRequest().mutate().headers(headers -> {
            headers.set("X-Request-Id", id);
            headers.remove("X-Service-Token");
        }).build();
        exchange.getResponse().getHeaders().set("X-Request-Id", id);
        long started = System.nanoTime();
        return chain.filter(exchange.mutate().request(request).build()).doFinally(signal ->
                log.info("requestId={} method={} path={} status={} durationMs={}", id,
                        request.getMethod(), request.getURI().getPath(), exchange.getResponse().getStatusCode(),
                        (System.nanoTime() - started) / 1_000_000));
    }
}
