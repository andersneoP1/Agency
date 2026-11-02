package com.nativetravel.repository;

import com.nativetravel.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий авиарейсов с поддержкой фильтрации.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {

    /**
     * Поиск рейсов по городам вылета и прилета.
     */
    Page<Flight> findByDepartureCityIgnoreCaseAndArrivalCityIgnoreCase(
            String departureCity, String arrivalCity, Pageable pageable
    );

    /**
     * Список уникальных городов вылета.
     */
    @Query("SELECT DISTINCT f.departureCity FROM Flight f ORDER BY f.departureCity ASC")
    List<String> findDistinctDepartureCities();

    /**
     * Список уникальных городов прилета.
     */
    @Query("SELECT DISTINCT f.arrivalCity FROM Flight f ORDER BY f.arrivalCity ASC")
    List<String> findDistinctArrivalCities();
}

