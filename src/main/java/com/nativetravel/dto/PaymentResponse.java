package com.nativetravel.dto;

import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Ответ на проведение оплаты бронирования.
 */
@Schema(description = "Результат выполнения оплаты")
public record PaymentResponse(
        @Schema(description = "ID бронирования", example = "1")
        Long bookingId,

        @Schema(description = "Номер бронирования", example = "NT-2026-83910")
        String bookingNumber,

        @Schema(description = "Статус оплаты", example = "PAID")
        BookingStatus status,

        @Schema(description = "Способ оплаты", example = "CARD")
        PaymentMethod paymentMethod,

        @Schema(description = "Оплаченная сумма", example = "80910.00")
        BigDecimal paidAmount,

        @Schema(description = "QR-код в Base64 Data URI")
        String qrCodeBase64,

        @Schema(description = "Сообщение о статусе операции", example = "Оплата успешно завершена! Ваучер и QR-код отправлены на ваш email.")
        String message
) {
}
