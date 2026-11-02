package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO представления промокода.
 */
@Schema(description = "Информация о промокоде")
public record PromoCodeDto(
        @Schema(description = "ID промокода", example = "1")
        Long id,

        @Schema(description = "Кодовое слово промокода", example = "WELCOME10")
        String code,

        @Schema(description = "Размер скидки в процентах", example = "10")
        Integer discountPercent,

        @Schema(description = "Минимальная сумма заказа для применения", example = "10000.00")
        BigDecimal minOrderAmount,

        @Schema(description = "Максимальное количество использований", example = "500")
        Integer maxUses,

        @Schema(description = "Текущее количество использований", example = "42")
        Integer currentUses,

        @Schema(description = "Срок действия промокода")
        Instant validUntil,

        @Schema(description = "Признак активности промокода", example = "true")
        Boolean active
) {
}
