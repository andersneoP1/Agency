package com.nativetravel.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class GatewayEndpoints {
    @RequestMapping("/fallback/{service}")
    public ResponseEntity<ProblemDetail> fallback(@PathVariable String service) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable: " + service + ". Check the result before repeating a write request.");
        problem.setTitle("Downstream service unavailable");
        problem.setProperty("service", service);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @GetMapping("/")
    public Mono<Void> home(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        exchange.getResponse().getHeaders().setLocation(java.net.URI.create("/swagger-ui.html"));
        return exchange.getResponse().setComplete();
    }
}
