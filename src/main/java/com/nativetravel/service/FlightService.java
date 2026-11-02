package com.nativetravel.service;

import com.nativetravel.dto.FlightDto;
import com.nativetravel.dto.FlightFilterRequest;
import com.nativetravel.entity.Flight;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Сервис поиска и управления авиарейсами.
 */
public interface FlightService {

    /**
     * Поиск авиарейсов по параметрам с пагинацией.
     *
     * @param filter критерии фильтрации
     * @return страница рейсов
     */
    Page<FlightDto> getFlights(FlightFilterRequest filter);

    /**
     * Получение DTO рейса по идентификатору.
     *
     * @param id идентификатор рейса
     * @return DTO рейса
     */
    FlightDto getFlightById(Long id);

    /**
     * Получение сущности рейса для оформления заказа.
     *
     * @param id идентификатор рейса
     * @return JPA-сущность авиарейса
     */
    Flight getFlightEntity(Long id);

    /**
     * Список доступных городов вылета.
     */
    List<String> getDepartureCities();

    /**
     * Список доступных городов прилета.
     */
    List<String> getArrivalCities();
}
