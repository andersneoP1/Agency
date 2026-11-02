package com.nativetravel.service;

import com.nativetravel.dto.TourDto;

import java.util.List;

/**
 * Сервис сравнения туристических туров по ключевым характеристикам.
 */
public interface ComparisonService {

    /**
     * Получить детальные характеристики туров для сравнительной таблицы (до 4 туров).
     *
     * @param tourIds список идентификаторов туров
     * @return список DTO туров для сравнения
     */
    List<TourDto> compareTours(List<Long> tourIds);
}
