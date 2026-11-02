package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

/**
 * Запрос на добавление тура или авиабилета в корзину.
 */
@Schema(description = "Запрос добавления позиции в корзину")
public record AddToCartRequest(
        @Schema(description = "ID тура (если добавляется тур)", example = "1")
        Long tourId,

        @Schema(description = "ID рейса (если добавляется авиабилет)", example = "1")
        Long flightId,

        @Schema(description = "Количество", example = "1")
        @Min(value = 1, message = "Количество должно быть не менее 1")
        Integer quantity
) {
    public int getSafeQuantity() {
        return (quantity != null && quantity >= 1) ? quantity : 1;
    }
}

