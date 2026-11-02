package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Запрос создания нового промокода в админке.
 */
@Schema(description = "Запрос создания промокода")
public record PromoCodeCreateRequest(
        @Schema(description = "Кодовое слово", example = "AUTUMN25")
        @NotBlank(message = "Код обязателен")
        String code,

        @Schema(description = "Скидка в процентах (1-100)", example = "25")
        @NotNull(message = "Укажите процент скидки")
        @Min(value = 1, message = "Минимум 1%")
        @Max(value = 100, message = "Максимум 100%")
        Integer discountPercent,

        @Schema(description = "Минимальная сумма заказа", example = "20000.00")
        @NotNull(message = "Укажите минимальную сумму")
        @DecimalMin(value = "0.0", message = "Сумма не может быть отрицательной")
        BigDecimal minOrderAmount,

        @Schema(description = "Максимальное число использований", example = "200")
        @NotNull(message = "Укажите лимит использований")
        @Min(value = 1, message = "Минимум 1 использование")
        Integer maxUses,

        @Schema(description = "Срок действия в днях от текущего момента", example = "60")
        @NotNull(message = "Укажите срок действия в днях")
        @Min(value = 1, message = "Минимум 1 день")
        Integer validDays
) {
}
