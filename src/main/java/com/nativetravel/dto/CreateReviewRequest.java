package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Запрос добавления отзыва к туру.
 */
@Schema(description = "Запрос создания отзыва")
public record CreateReviewRequest(
        @Schema(description = "ID тура", example = "1")
        @NotNull(message = "ID тура обязателен")
        Long tourId,

        @Schema(description = "Оценка (от 1 до 5 звезд)", example = "5")
        @NotNull(message = "Укажите оценку от 1 до 5")
        @Min(value = 1, message = "Минимальная оценка 1")
        @Max(value = 5, message = "Максимальная оценка 5")
        Integer rating,

        @Schema(description = "Текст отзыва", example = "Прекрасный отель, отличный пляж и организация!")
        @NotBlank(message = "Напишите текст отзыва")
        @Size(min = 5, max = 2000, message = "Длина отзыва от 5 до 2000 символов")
        String comment
) {
}

