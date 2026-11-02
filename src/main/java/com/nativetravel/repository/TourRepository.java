package com.nativetravel.repository;

import com.nativetravel.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий туров с поддержкой динамической фильтрации (JPA Specifications).
 */
@Repository
public interface TourRepository extends JpaRepository<Tour, Long>, JpaSpecificationExecutor<Tour> {

    /**
     * Поиск горящих предложений.
     *
     * @param hotOffer признак горящего тура
     * @param pageable параметры пагинации
     * @return страница туров
     */
    Page<Tour> findByHotOfferTrue(Pageable pageable);

    /**
     * Получение списка всех уникальных стран из базы.
     *
     * @return список названий стран
     */
    @Query("SELECT DISTINCT t.country FROM Tour t ORDER BY t.country ASC")
    List<String> findDistinctCountries();
}

