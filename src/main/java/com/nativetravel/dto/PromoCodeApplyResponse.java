package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Ответ с результатом валидации промокода и пересчитанной суммой.
 */
@Schema(description = "Результат применения промокода")
public record PromoCodeApplyResponse(
        @Schema(description = "Примененный промокод", example = "WELCOME10")
        String code,

        @Schema(description = "Размер скидки в %", example = "10")
        int discountPercent,

        @Schema(description = "Сумма скидки в рублях", example = "8990.00")
        BigDecimal discountAmount,

        @Schema(description = "Итоговая сумма к оплате с учетом скидки", example = "80910.00")
        BigDecimal finalAmount,

        @Schema(description = "Информационное сообщение", example = "Промокод успешно применен")
        String message
) {
}

