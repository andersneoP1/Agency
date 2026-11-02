package com.nativetravel.service;

import com.nativetravel.dto.SurveyRequest;
import com.nativetravel.dto.SurveyResponse;

/**
 * Сервис интеллектуального подбора туров на основе опроса предпочтений.
 */
public interface SurveyService {

    /**
     * Сохранение ответов пользователя на опрос и подбор персональной рекомендации тура.
     *
     * @param request ответы пользователя
     * @param userEmail email пользователя
     * @return результат анализа с рекомендованным туром
     */
    SurveyResponse submitSurvey(SurveyRequest request, String userEmail);

    /**
     * Получение последней рекомендации пользователя.
     *
     * @param userEmail email пользователя
     * @return последняя рекомендация
     */
    SurveyResponse getLatestRecommendation(String userEmail);
}

