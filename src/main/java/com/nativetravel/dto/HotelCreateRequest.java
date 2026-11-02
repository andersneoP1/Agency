package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос создания или редактирования отеля в панели администратора.
 */
@Schema(description = "Запрос создания/редактирования отеля")
public record HotelCreateRequest(
        @Schema(description = "Название отеля", example = "Rixos Premium Belek")
        @NotBlank(message = "Название отеля обязательно")
        String name,

        @Schema(description = "Страна", example = "Турция")
        @NotBlank(message = "Страна обязательна")
        String country,

        @Schema(description = "Город", example = "Белек")
        @NotBlank(message = "Город обязателен")
        String city,

        @Schema(description = "Адрес отеля", example = "Ileribasi Mevkii, Belek")
        String address,

        @Schema(description = "Звёздность (1-5)", example = "5")
        @NotNull(message = "Укажите количество звезд")
        @Min(value = 1, message = "Минимум 1 звезда")
        @Max(value = 5, message = "Максимум 5 звезд")
        int stars,

        @Schema(description = "Описание отеля")
        String description,

        @Schema(description = "Удобства через запятую", example = "Пляж, Спа, Бассейн, All Inclusive")
        String amenities,

        @Schema(description = "URL фотографии", example = "https://images.unsplash.com/photo-1582719508461-905c673771fd")
        String imageUrl
) {
}
