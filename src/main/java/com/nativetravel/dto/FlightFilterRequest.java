package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Параметры фильтрации и пагинации авиабилетов.
 */
@Schema(description = "Фильтры для поиска авиабилетов")
public record FlightFilterRequest(
        @Schema(description = "Город вылета", example = "Москва")
        String departureCity,

        @Schema(description = "Город прилета", example = "Анталья")
        String arrivalCity,

        @Schema(description = "Дата вылета", example = "2026-06-15")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate departureDate,

        @Schema(description = "Максимальная цена", example = "25000")
        BigDecimal maxPrice,

        @Schema(description = "Номер страницы", example = "0")
        Integer page,

        @Schema(description = "Количество на странице", example = "10")
        Integer size
) {
    public int getPageNumber() {
        return (page != null && page >= 0) ? page : 0;
    }

    public int getPageSize() {
        return (size != null && size > 0 && size <= 50) ? size : 10;
    }
}
