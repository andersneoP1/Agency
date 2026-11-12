package com.nativetravel.distributed;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
@Slf4j
public class CloudTransport {
    @Bean
    @LoadBalanced
    RestClient.Builder serviceRestClientBuilder(@Value("${app.internal-token}") String token) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder().requestFactory(factory).defaultHeader("X-Service-Token", token)
                .requestInterceptor((request, body, execution) -> {
                    String id = MDC.get("requestId");
                    if (id != null) request.getHeaders().set("X-Request-Id", id);
                    return execution.execute(request, body);
                });
    }

    @Bean
    Calls serviceCalls(@LoadBalanced RestClient.Builder builder, CircuitBreakerFactory<?, ?> breakers) {
        return new Calls(builder.build(), breakers);
    }

    public record Calls(RestClient client, CircuitBreakerFactory<?, ?> breakers) {
        public <T> T post(String service, String operation, Object body, Class<T> type) {
            String id = MDC.get("requestId");
            return breakers.create(service).run(() -> client.post()
                    .uri("http://" + service + "/internal/" + operation)
                    .header("X-Request-Id", id == null ? java.util.UUID.randomUUID().toString() : id).body(body)
                    .retrieve().body(type), failure -> {
                if (failure instanceof HttpClientErrorException ex) {
                    throw new ResponseStatusException(ex.getStatusCode(), "Downstream request rejected");
                }
                log.warn("Downstream service unavailable: {} operation={}", service, operation);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Service temporarily unavailable: " + service);
            });
        }
    }
}
