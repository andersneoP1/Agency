package com.nativetravel.repository;

import com.nativetravel.entity.FavoriteTour;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий избранных туров пользователя.
 */
@Repository
public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, Long> {

    /**
     * Поиск всех избранных туров пользователя с пагинацией.
     */
    Page<FavoriteTour> findByUser(User user, Pageable pageable);

    /**
     * Проверка, добавлен ли тур в избранное.
     */
    boolean existsByUserAndTour(User user, Tour tour);

    /**
     * Поиск конкретной записи избранного.
     */
    Optional<FavoriteTour> findByUserAndTour(User user, Tour tour);

    /**
     * Удаление тура из избранного.
     */
    void deleteByUserAndTour(User user, Tour tour);
}

