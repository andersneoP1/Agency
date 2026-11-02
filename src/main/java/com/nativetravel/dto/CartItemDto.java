package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO представления элемента корзины.
 */
@Schema(description = "Элемент корзины")
public record CartItemDto(
        @Schema(description = "ID элемента корзины", example = "1")
        Long id,

        @Schema(description = "Тур (если в корзине тур)")
        TourDto tour,

        @Schema(description = "Авиабилет (если в корзине авиабилет)")
        FlightDto flight,

        @Schema(description = "Количество", example = "1")
        int quantity,

        @Schema(description = "Подытог по позиции", example = "89900.00")
        BigDecimal subtotalPrice,

        @Schema(description = "Время добавления в корзину")
        Instant createdAt
) {
}

