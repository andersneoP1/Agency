package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Запрос проверки и валидации промокода.
 */
@Schema(description = "Запрос проверки и расчета скидки по промокоду")
public record PromoCodeValidateRequest(
        @Schema(description = "Промокод", example = "WELCOME10")
        @NotBlank(message = "Укажите промокод")
        String code,

        @Schema(description = "Текущая сумма заказа", example = "89900.00")
        @NotNull(message = "Сумма заказа обязательна")
        @DecimalMin(value = "0.0", inclusive = false, message = "Сумма заказа должна быть больше 0")
        BigDecimal orderAmount
) {
}
