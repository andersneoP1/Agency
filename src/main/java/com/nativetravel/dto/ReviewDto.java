package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO представления отзыва о туре.
 */
@Schema(description = "Отзыв о туре")
public record ReviewDto(
        @Schema(description = "ID отзыва", example = "1")
        Long id,

        @Schema(description = "ID тура", example = "1")
        Long tourId,

        @Schema(description = "Название тура", example = "Жемчужина Антальи")
        String tourTitle,

        @Schema(description = "Автор отзыва", example = "Иван Петров")
        String authorName,

        @Schema(description = "Оценка (от 1 до 5)", example = "5")
        Integer rating,

        @Schema(description = "Текст отзыва", example = "Прекрасный отель и отличный сервис!")
        String comment,

        @Schema(description = "Статус модерации", example = "true")
        Boolean approved,

        @Schema(description = "Дата создания")
        Instant createdAt
) {
}
