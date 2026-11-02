package com.nativetravel.service;

import com.nativetravel.dto.CreateReviewRequest;
import com.nativetravel.dto.ReviewDto;
import com.nativetravel.entity.Review;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.mapper.ReviewMapper;
import com.nativetravel.repository.ReviewRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ReviewService")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User sampleUser;
    private Tour sampleTour;
    private Review sampleReview;
    private ReviewDto sampleReviewDto;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("user@example.com").build();
        sampleTour = Tour.builder().id(2L).title("Тур в Дубай").build();
        sampleReview = Review.builder().id(5L).user(sampleUser).tour(sampleTour).rating(5).comment("Отлично!").approved(true).build();
        sampleReviewDto = new ReviewDto(5L, 2L, "Тур в Дубай", "user@example.com", 5, "Отлично!", true, null);
    }

    @Test
    @DisplayName("Добавление отзыва сохраняет отзыв и возвращает DTO")
    void addReview_valid_savesReview() {
        CreateReviewRequest request = new CreateReviewRequest(2L, 5, "Отличный отдых и сервис!");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(2L)).thenReturn(Optional.of(sampleTour));
        when(reviewRepository.existsByUserIdAndTourId(1L, 2L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);
        when(reviewMapper.toDto(sampleReview)).thenReturn(sampleReviewDto);

        ReviewDto result = reviewService.addReview(request, "user@example.com");

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Повторный отзыв на один и тот же тур выбрасывает BadRequestException")
    void addReview_duplicate_throwsException() {
        CreateReviewRequest request = new CreateReviewRequest(2L, 4, "Второй отзыв");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(2L)).thenReturn(Optional.of(sampleTour));
        when(reviewRepository.existsByUserIdAndTourId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReview(request, "user@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Разрешен только 1 отзыв");
    }

    @Test
    @DisplayName("Одобрение отзыва переводит флаг approved в true")
    void approveReview_setsApprovedTrue() {
        sampleReview.setApproved(false);
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(sampleReview));

        reviewService.approveReview(5L);

        assertThat(sampleReview.getApproved()).isTrue();
        verify(reviewRepository).save(sampleReview);
    }
}
