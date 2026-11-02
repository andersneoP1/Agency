package com.nativetravel.repository;

import com.nativetravel.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий отзывов пользователей.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Поиск одобренных отзывов к туру с предзагрузкой пользователей.
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.tour.id = :tourId AND r.approved = true ORDER BY r.createdAt DESC")
    List<Review> findByTourIdAndApprovedTrueWithUser(@Param("tourId") Long tourId);

    /**
     * Проверка, оставлял ли пользователь уже отзыв на этот тур.
     */
    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    /**
     * Отзывы, ожидающие модерации администратора.
     */
    Page<Review> findByApprovedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Все отзывы для админ-панели.
     */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Количество отзывов на модерации.
     */
    long countByApprovedFalse();
}

