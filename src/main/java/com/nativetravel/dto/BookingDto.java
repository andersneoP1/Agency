package com.nativetravel.dto;

import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO представления бронирования.
 */
@Schema(description = "Информация о бронировании")
public record BookingDto(
        @Schema(description = "ID бронирования", example = "1")
        Long id,

        @Schema(description = "Уникальный номер бронирования", example = "NT-2026-83910")
        String bookingNumber,

        @Schema(description = "Тур (если бронь тура)")
        TourDto tour,

        @Schema(description = "Авиабилет (если бронь рейса)")
        FlightDto flight,

        @Schema(description = "Исходная цена до скидки", example = "89900.00")
        BigDecimal originalPrice,

        @Schema(description = "Размер скидки", example = "8990.00")
        BigDecimal discountAmount,

        @Schema(description = "Итоговая стоимость", example = "80910.00")
        BigDecimal totalPrice,

        @Schema(description = "Статус бронирования", example = "PAID")
        BookingStatus status,

        @Schema(description = "Способ оплаты", example = "CARD")
        PaymentMethod paymentMethod,

        @Schema(description = "QR-код в Base64 Data URI")
        String qrCodeBase64,

        @Schema(description = "Дата и время создания бронирования")
        Instant createdAt
) {
}
