package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO состояния корзины пользователя с итогами.
 */
@Schema(description = "Состояние корзины покупок")
public record CartResponse(
        @Schema(description = "Список элементов корзины")
        List<CartItemDto> items,

        @Schema(description = "Общее количество позиций", example = "2")
        int totalItems,

        @Schema(description = "Суммарная стоимость позиций корзины", example = "108400.00")
        BigDecimal totalPrice
) {
}

