package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Параметры фильтрации и пагинации каталога туров.
 */
@Schema(description = "Фильтры для поиска туров")
public record TourFilterRequest(
        @Schema(description = "Страна", example = "Турция")
        String country,

        @Schema(description = "Город", example = "Анталья")
        String city,

        @Schema(description = "Минимальная цена", example = "30000")
        BigDecimal minPrice,

        @Schema(description = "Максимальная цена", example = "150000")
        BigDecimal maxPrice,

        @Schema(description = "Дата вылета от", example = "2026-06-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "Минимальная звездность отеля (1-5)", example = "4")
        Integer minStars,

        @Schema(description = "Только горящие предложения", example = "false")
        Boolean hotOfferOnly,

        @Schema(description = "Поле сортировки: price, startDate, durationDays", example = "price")
        String sortBy,

        @Schema(description = "Направление сортировки: asc, desc", example = "asc")
        String sortDirection,

        @Schema(description = "Номер страницы (0-based)", example = "0")
        Integer page,

        @Schema(description = "Количество элементов на странице", example = "9")
        Integer size
) {
    public static TourFilterRequest defaultFilter() {
        return new TourFilterRequest(null, null, null, null, null, null, null, "price", "asc", 0, 20);
    }

    public int getPageNumber() {
        return (page != null && page >= 0) ? page : 0;
    }

    public int getPageSize() {
        return (size != null && size > 0 && size <= 50) ? size : 9;
    }

    public String getSortField() {
        if ("startDate".equalsIgnoreCase(sortBy) || "durationDays".equalsIgnoreCase(sortBy)) {
            return sortBy;
        }
        return "price";
    }

    public boolean isAscending() {
        return sortDirection == null || !"desc".equalsIgnoreCase(sortDirection);
    }

    public Integer stars() {
        return minStars;
    }

    public Boolean hotOffer() {
        return hotOfferOnly;
    }
}
