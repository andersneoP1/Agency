package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Запрос создания или редактирования тура в панели администратора.
 */
@Schema(description = "Запрос создания/редактирования тура")
public record TourCreateRequest(
        @Schema(description = "Название тура", example = "Жемчужина Антальи: All Inclusive")
        @NotBlank(message = "Название тура обязательно")
        String title,

        @Schema(description = "Подробное описание тура")
        @NotBlank(message = "Описание обязательно")
        String description,

        @Schema(description = "Страна", example = "Турция")
        @NotBlank(message = "Страна обязательна")
        String country,

        @Schema(description = "Город / Курорт", example = "Белек")
        @NotBlank(message = "Город обязателен")
        String city,

        @Schema(description = "ID отеля", example = "1")
        Long hotelId,

        @Schema(description = "Стоимость тура в рублях", example = "89900.00")
        @NotNull(message = "Стоимость обязательна")
        @DecimalMin(value = "1.0", message = "Стоимость должна быть больше 0")
        BigDecimal price,

        @Schema(description = "Дата начала тура")
        @NotNull(message = "Дата начала обязательна")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @Schema(description = "Дата окончания тура")
        @NotNull(message = "Дата окончания обязательна")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,

        @Schema(description = "Длительность в днях", example = "7")
        @Min(value = 1, message = "Минимум 1 день")
        int durationDays,

        @Schema(description = "Включенные в стоимость услуги", example = "Перелет, Трансфер, Все включено")
        String includedServices,

        @Schema(description = "URL изображения", example = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e")
        String imageUrl,

        @Schema(description = "Горящее предложение", example = "true")
        boolean hotOffer,

        @Schema(description = "Количество свободных мест", example = "15")
        @Min(value = 0, message = "Мест не может быть меньше 0")
        int availableSlots
) {
}
