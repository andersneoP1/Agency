package com.nativetravel.dto;

import com.nativetravel.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос проведения оплаты бронирования.
 */
@Schema(description = "Запрос оплаты бронирования")
public record PaymentRequest(
        @Schema(description = "ID бронирования", example = "1")
        @NotNull(message = "ID бронирования обязателен")
        Long bookingId,

        @Schema(description = "Выбранный способ оплаты", example = "CARD")
        @NotNull(message = "Выберите способ оплаты")
        PaymentMethod paymentMethod,

        @Schema(description = "Номер карты (для оплаты картой)", example = "4276 3800 1234 5678")
        String cardNumber,

        @Schema(description = "Имя держателя карты", example = "IVAN PETROV")
        String cardHolder,

        @Schema(description = "Срок действия карты MM/YY", example = "12/28")
        String cardExpiry,

        @Schema(description = "Код CVV/CVC", example = "123")
        String cardCvv
) {
}
