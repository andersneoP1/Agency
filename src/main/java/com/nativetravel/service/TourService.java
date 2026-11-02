package com.nativetravel.service;

import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.TourFilterRequest;
import com.nativetravel.entity.Tour;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Сервис управления каталогом туров, поиском и фильтрацией.
 */
public interface TourService {

    /**
     * Поиск туров по гибким фильтрам с пагинацией и сортировкой.
     *
     * @param filter критерии фильтрации
     * @return страница найденных туров
     */
    Page<TourDto> getTours(TourFilterRequest filter);

    /**
     * Получение DTO тура по идентификатору.
     *
     * @param id идентификатор тура
     * @return DTO тура
     */
    TourDto getTourById(Long id);

    /**
     * Получение сущности тура для внутренних сервисов.
     *
     * @param id идентификатор тура
     * @return JPA-сущность тура
     */
    Tour getTourEntity(Long id);

    /**
     * Получение списка горящих предложений.
     *
     * @param limit максимальное количество
     * @return список горящих туров
     */
    List<TourDto> getHotOffers(int limit);

    /**
     * Получение списка всех доступных стран для селектора поиска.
     *
     * @return список названий стран
     */
    List<String> getAvailableCountries();

    /**
     * Фиксация факта просмотра тура авторизованным пользователем.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя (если авторизован)
     */
    void recordTourView(Long tourId, String userEmail);
}
