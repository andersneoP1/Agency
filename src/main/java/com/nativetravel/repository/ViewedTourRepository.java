package com.nativetravel.repository;

import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.entity.ViewedTour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий истории просмотров туров.
 */
@Repository
public interface ViewedTourRepository extends JpaRepository<ViewedTour, Long> {

    /**
     * Получение недавно просмотренных туров пользователя.
     */
    Page<ViewedTour> findByUserOrderByViewedAtDesc(User user, Pageable pageable);

    /**
     * Поиск записи просмотра для обновления времени.
     */
    Optional<ViewedTour> findByUserAndTour(User user, Tour tour);
}
