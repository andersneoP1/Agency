package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ с подобранной рекомендацией тура на основе опроса.
 */
@Schema(description = "Результат прохождения опроса и персональная рекомендация")
public record SurveyResponse(
        @Schema(description = "ID опроса", example = "1")
        Long surveyId,

        @Schema(description = "Рекомендованный тур")
        TourDto recommendedTour,

        @Schema(description = "Обоснование рекомендации", example = "На основе вашего выбора пляжного отдыха и теплого климата мы подобрали идеальный курорт!")
        String recommendationReason
) {
}

