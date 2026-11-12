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

        @Schema(description = "Дата возвращения / окончания до", example = "2026-06-30")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "Минимальная звездность отеля (1-5)", example = "4")
        Integer minStars,

        @Schema(description = "Точная звездность отеля (1-5)", example = "4")
        Integer stars,

        @Schema(description = "Только горящие предложения", example = "false")
        Boolean hotOfferOnly,

        @Schema(description = "Поле сортировки: price, startDate, durationDays, hotel.stars", example = "price")
        String sortBy,

        @Schema(description = "Направление сортировки: asc, desc", example = "asc")
        String sortDirection,

        @Schema(description = "Номер страницы (0-based)", example = "0")
        Integer page,

        @Schema(description = "Количество элементов на странице", example = "9")
        Integer size
) {
    public int getPageNumber() {
        return (page != null && page >= 0) ? page : 0;
    }

    public int getPageSize() {
        return (size != null && size > 0 && size <= 50) ? size : 9;
    }

    public String getSortField() {
        if (sortBy == null || sortBy.isBlank()) {
            return "price";
        }
        String lower = sortBy.trim().toLowerCase();
        if (lower.startsWith("star") || lower.contains("hotel.stars") || lower.contains("hotel_stars")) {
            return "hotel.stars";
        }
        if (lower.startsWith("duration")) {
            return "durationDays";
        }
        if (lower.startsWith("startdate") || lower.startsWith("date")) {
            return "startDate";
        }
        return "price";
    }

    public boolean isAscending() {
        if (sortBy != null && !sortBy.isBlank()) {
            String lower = sortBy.trim().toLowerCase();
            if (lower.endsWith("_desc") || lower.endsWith("-desc") || lower.endsWith(":desc")) {
                return false;
            }
            if (lower.endsWith("_asc") || lower.endsWith("-asc") || lower.endsWith(":asc")) {
                return true;
            }
        }
        if ("desc".equalsIgnoreCase(sortDirection)) {
            return false;
        }
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return true;
        }
        if (sortBy != null && !sortBy.isBlank()) {
            String lower = sortBy.trim().toLowerCase();
            if (lower.startsWith("star")) {
                return false;
            }
            if (lower.startsWith("duration")) {
                return false;
            }
        }
        return true;
    }

    public Integer getEffectiveStars() {
        return stars != null ? stars : minStars;
    }

    @Override
    public Integer stars() {
        return getEffectiveStars();
    }

    public Boolean hotOffer() {
        return hotOfferOnly;
    }
}
