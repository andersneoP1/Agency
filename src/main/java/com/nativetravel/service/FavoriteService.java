package com.nativetravel.service;

import com.nativetravel.dto.TourDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис управления списком избранных туров пользователя.
 */
public interface FavoriteService {

    /**
     * Добавление тура в избранное.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     */
    void addFavorite(Long tourId, String userEmail);

    /**
     * Удаление тура из избранного.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     */
    void removeFavorite(Long tourId, String userEmail);

    /**
     * Проверка, находится ли тур в избранном.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     * @return true, если тур в избранном
     */
    boolean isFavorite(Long tourId, String userEmail);

    /**
     * Получение списка всех избранных туров пользователя.
     *
     * @param userEmail email пользователя
     * @param pageable параметры пагинации
     * @return страница избранных туров
     */
    Page<TourDto> getUserFavorites(String userEmail, Pageable pageable);
}
