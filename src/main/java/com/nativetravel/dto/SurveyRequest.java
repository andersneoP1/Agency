package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Запрос сохранения предпочтений туристического опроса.
 */
@Schema(description = "Ответы на опрос предпочтений путешественника")
public record SurveyRequest(
        @Schema(description = "Бюджет поездки (ECONOMY, MEDIUM, LUXURY)", example = "MEDIUM")
        @NotBlank(message = "Выберите предпочтительный бюджет")
        String preferredBudget,

        @Schema(description = "Тип отдыха (BEACH, SIGHTSEEING, MOUNTAIN, ACTIVE)", example = "BEACH")
        @NotBlank(message = "Выберите тип отдыха")
        String travelType,

        @Schema(description = "Желаемый сезон поездки (SUMMER, AUTUMN, WINTER, SPRING)", example = "SUMMER")
        String preferredSeason,

        @Schema(description = "Климатические предпочтения (WARM, MODERATE, COLD)", example = "WARM")
        String climatePreference
) {
}
