package com.nativetravel.service.impl;

import com.nativetravel.dto.CreateReviewRequest;
import com.nativetravel.dto.ReviewDto;
import com.nativetravel.entity.Review;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.ReviewMapper;
import com.nativetravel.repository.ReviewRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса управления отзывами пользователей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getTourReviews(Long tourId) {
        return reviewRepository.findByTourIdAndApprovedTrueWithUser(tourId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ReviewDto addReview(CreateReviewRequest request, String userEmail) {
        User user = getUser(userEmail);
        Tour tour = tourRepository.findById(request.tourId())
                .orElseThrow(() -> new ResourceNotFoundException("Тур с id " + request.tourId() + " не найден"));

        if (reviewRepository.existsByUserIdAndTourId(user.getId(), tour.getId())) {
            throw new BadRequestException("Вы уже оставляли отзыв к этому туру. Разрешен только 1 отзыв на тур.");
        }

        Review review = Review.builder()
                .user(user)
                .tour(tour)
                .rating(request.rating())
                .comment(request.comment().trim())
                .approved(true) // Auto-approve or queue for moderation
                .build();

        Review saved = reviewRepository.save(review);
        log.info("User {} added review #{} for tour {}", userEmail, saved.getId(), tour.getTitle());
        return reviewMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByApprovedFalseOrderByCreatedAtDesc(pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(reviewMapper::toDto);
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));
        review.setApproved(true);
        reviewRepository.save(review);
        log.info("Review #{} approved by moderator", reviewId);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв не найден"));
        reviewRepository.delete(review);
        log.info("Review #{} deleted", reviewId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + email + " не найден"));
    }
}
