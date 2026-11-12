package com.nativetravel.controller.rest;

import com.nativetravel.distributed.InternalRequests.Payment;
import com.nativetravel.dto.PaymentResponse;
import com.nativetravel.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
@Hidden
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class InternalPaymentsController {
    private final PaymentService payments;
    @PostMapping("/process")
    public PaymentResponse process(@Valid @RequestBody Payment request) {
        return payments.processPayment(request.request(), request.userEmail());
    }
}
