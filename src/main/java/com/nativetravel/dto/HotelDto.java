package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO представления отеля.
 */
@Schema(description = "Информация об отеле")
public record HotelDto(
        @Schema(description = "ID отеля", example = "1")
        Long id,

        @Schema(description = "Название отеля", example = "Grand Resort Antalya")
        String name,

        @Schema(description = "Страна", example = "Турция")
        String country,

        @Schema(description = "Город", example = "Анталья")
        String city,

        @Schema(description = "Адрес", example = "Lara Beach, 12")
        String address,

        @Schema(description = "Количество звезд (1-5)", example = "5")
        int stars,

        @Schema(description = "Описание отеля")
        String description,

        @Schema(description = "Удобства и сервисы (Wi-Fi, Бассейн, СПА)", example = "Wi-Fi, Бассейн, All Inclusive, SPA")
        String amenities,

        @Schema(description = "URL изображения", example = "https://images.unsplash.com/photo-1566073771259-6a8506099945")
        String imageUrl
) {
}

