package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO представления туристического тура.
 */
@Schema(description = "Информация о туре")
public record TourDto(
        @Schema(description = "ID тура", example = "1")
        Long id,

        @Schema(description = "Название тура", example = "Grand Resort & All Inclusive")
        String title,

        @Schema(description = "Подробное описание тура")
        String description,

        @Schema(description = "Страна", example = "Турция")
        String country,

        @Schema(description = "Город / Курорт", example = "Анталья")
        String city,

        @Schema(description = "Отель")
        HotelDto hotel,

        @Schema(description = "Стоимость тура", example = "64500.00")
        BigDecimal price,

        @Schema(description = "Дата начала тура", example = "2026-06-10")
        LocalDate startDate,

        @Schema(description = "Дата окончания тура", example = "2026-06-17")
        LocalDate endDate,

        @Schema(description = "Длительность в днях", example = "7")
        int durationDays,

        @Schema(description = "Включенные услуги", example = "Перелет, Трансфер, Все включено, Страховка")
        String includedServices,

        @Schema(description = "URL изображения", example = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
        String imageUrl,

        @Schema(description = "Признак горящего тура", example = "true")
        boolean hotOffer,

        @Schema(description = "Количество доступных мест", example = "5")
        int availableSlots
) {
}
