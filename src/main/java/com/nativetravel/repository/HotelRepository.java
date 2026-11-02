package com.nativetravel.repository;

import com.nativetravel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для управления отелями.
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Поиск отелей по стране.
     *
     * @param country название страны
     * @return список отелей
     */
    List<Hotel> findByCountryIgnoreCase(String country);

    /**
     * Поиск отелей по количеству звёзд.
     *
     * @param stars количество звезд
     * @return список отелей
     */
    List<Hotel> findByStars(int stars);
}

