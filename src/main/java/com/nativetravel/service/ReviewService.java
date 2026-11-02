package com.nativetravel.service;

import com.nativetravel.dto.CreateReviewRequest;
import com.nativetravel.dto.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Сервис управления отзывами пользователей и модерации.
 */
public interface ReviewService {

    /**
     * Получить все одобренные отзывы к туру.
     *
     * @param tourId ID тура
     * @return список отзывов
     */
    List<ReviewDto> getTourReviews(Long tourId);

    /**
     * Добавить новый отзыв о туре от авторизованного пользователя.
     *
     * @param request данные отзыва
     * @param userEmail email автора
     * @return DTO созданного отзыва
     */
    ReviewDto addReview(CreateReviewRequest request, String userEmail);

    /**
     * Получить отзывы, ожидающие одобрения модератором.
     */
    Page<ReviewDto> getPendingReviews(Pageable pageable);

    /**
     * Получить все отзывы для админ-панели.
     */
    Page<ReviewDto> getAllReviews(Pageable pageable);

    /**
     * Одобрить отзыв (модерация).
     *
     * @param reviewId ID отзыва
     */
    void approveReview(Long reviewId);

    /**
     * Удалить отзыв.
     *
     * @param reviewId ID отзыва
     */
    void deleteReview(Long reviewId);
}
