package com.nativetravel.service;

import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request, String userEmail);
}
