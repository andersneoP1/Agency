package com.nativetravel.dto;

import com.nativetravel.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Запрос оформления нового бронирования.
 */
@Schema(description = "Запрос создания бронирования")
public record BookingRequest(
        @Schema(description = "ID тура (если бронируется тур)", example = "1")
        Long tourId,

        @Schema(description = "ID рейса (если бронируется авиабилет)", example = "1")
        Long flightId,

        @Schema(description = "Промокод на скидку", example = "WELCOME10")
        String promoCode,

        @Schema(description = "Способ оплаты", example = "CARD")
        PaymentMethod paymentMethod
) {
}
