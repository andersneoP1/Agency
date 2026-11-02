package com.nativetravel.service;

import com.nativetravel.dto.SurveyRequest;
import com.nativetravel.dto.SurveyResponse;
import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.Survey;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.SurveyRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.SurveyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование SurveyService")
class SurveyServiceTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private SurveyServiceImpl surveyService;

    private User sampleUser;
    private Tour sampleTour;
    private TourDto sampleTourDto;
    private Survey sampleSurvey;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("traveler@example.com").build();
        sampleTour = Tour.builder().id(1L).title("Пляжный рай в Белеке").price(BigDecimal.valueOf(80000)).build();
        sampleTourDto = new TourDto(1L, "Пляжный рай в Белеке", null, "Турция", "Белек", null, BigDecimal.valueOf(80000), null, null, 7, null, null, false, 5);
        sampleSurvey = Survey.builder().id(10L).user(sampleUser).recommendedTour(sampleTour).build();
    }

    @Test
    @DisplayName("Отправка опроса сохраняет выбор и возвращает подобранный тур")
    void submitSurvey_returnsRecommendedTour() {
        SurveyRequest request = new SurveyRequest("MEDIUM", "BEACH", "SUMMER", "WARM");

        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findAll()).thenReturn(List.of(sampleTour));
        when(surveyRepository.save(any(Survey.class))).thenReturn(sampleSurvey);
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleTourDto);

        SurveyResponse response = surveyService.submitSurvey(request, "traveler@example.com");

        assertThat(response).isNotNull();
        assertThat(response.recommendedTour()).isNotNull();
        assertThat(response.recommendedTour().title()).isEqualTo("Пляжный рай в Белеке");
        verify(surveyRepository).save(any(Survey.class));
    }

    @Test
    @DisplayName("Получение последней рекомендации возвращает ранее подобранный тур")
    void getLatestRecommendation_returnsExisting() {
        when(userRepository.findByEmail("traveler@example.com")).thenReturn(Optional.of(sampleUser));
        when(surveyRepository.findLatestByUserId(1L)).thenReturn(Optional.of(sampleSurvey));
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleTourDto);

        SurveyResponse response = surveyService.getLatestRecommendation("traveler@example.com");

        assertThat(response).isNotNull();
        assertThat(response.recommendedTour()).isNotNull();
        assertThat(response.recommendedTour().id()).isEqualTo(1L);
    }
}

