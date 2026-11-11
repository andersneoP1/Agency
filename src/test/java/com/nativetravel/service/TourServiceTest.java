package com.nativetravel.service;

import com.nativetravel.dto.HotelDto;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.TourFilterRequest;
import com.nativetravel.entity.Hotel;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.repository.ViewedTourRepository;
import com.nativetravel.service.impl.TourServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование TourService")
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private ViewedTourRepository viewedTourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private TourServiceImpl tourService;

    private Tour sampleTour;
    private TourDto sampleDto;

    @BeforeEach
    void setUp() {
        Hotel sampleHotel = Hotel.builder()
                .id(1L)
                .name("Rixos Premium")
                .stars(5)
                .country("Турция")
                .city("Белек")
                .build();

        sampleTour = Tour.builder()
                .id(1L)
                .title("Жемчужина Антальи")
                .description("Роскошный отдых")
                .country("Турция")
                .city("Белек")
                .hotel(sampleHotel)
                .price(new BigDecimal("89900.00"))
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(17))
                .durationDays(7)
                .hotOffer(true)
                .availableSlots(12)
                .build();

        sampleDto = new TourDto(
                1L, "Жемчужина Антальи", "Роскошный отдых", "Турция", "Белек",
                new HotelDto(1L, "Rixos Premium", "Турция", "Белек", null, 5, null, null, null),
                new BigDecimal("89900.00"), LocalDate.now().plusDays(10), LocalDate.now().plusDays(17),
                7, "Питание, перелет", null, true, 12);
    }

    @Test
    @DisplayName("Поиск туров с фильтрацией возвращает страницу DTO")
    void getTours_withValidFilter_returnsPage() {
        TourFilterRequest filter = new TourFilterRequest("Турция", null, null, null, null, null, null, null, "price",
                "asc", 0, 10);
        Page<Tour> pagedTours = new PageImpl<>(List.of(sampleTour));

        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pagedTours);
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleDto);

        Page<TourDto> result = tourService.getTours(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Жемчужина Антальи");
    }

    @Test
    @DisplayName("Поиск туров с фильтрацией по диапазону дат startDate и endDate")
    void getTours_withDateRangeFilter_returnsPage() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(20);
        TourFilterRequest filter = new TourFilterRequest(
                null, null, null, null, start, end, null, null, "price", "asc", 0, 10);
        Page<Tour> pagedTours = new PageImpl<>(List.of(sampleTour));

        when(tourRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pagedTours);
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleDto);

        Page<TourDto> result = tourService.getTours(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение тура по ID завершается успешно при существующем туре")
    void getTourById_whenTourExists_returnsDto() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(sampleTour));
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleDto);

        TourDto result = tourService.getTourById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.price()).isEqualByComparingTo(new BigDecimal("89900.00"));
    }

    @Test
    @DisplayName("Получение тура по ID выбрасывает ResourceNotFoundException, если тур не найден")
    void getTourById_whenTourNotFound_throwsException() {
        when(tourRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tourService.getTourById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Тур с id 999 не найден");
    }

    @Test
    @DisplayName("Получение горящих туров возвращает список актуальных туров")
    void getHotOffers_returnsHotOffersList() {
        when(tourRepository.findByHotOfferTrue(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleTour)));
        when(tourMapper.toDto(sampleTour)).thenReturn(sampleDto);

        List<TourDto> hotOffers = tourService.getHotOffers(5);

        assertThat(hotOffers).isNotEmpty();
        assertThat(hotOffers.get(0).hotOffer()).isTrue();
    }

    @Test
    @DisplayName("Получение списка уникальных стран возвращает перечень")
    void getAvailableCountries_returnsDistinctCountries() {
        when(tourRepository.findDistinctCountries()).thenReturn(List.of("Турция", "ОАЭ", "Египет"));

        List<String> countries = tourService.getAvailableCountries();

        assertThat(countries).containsExactly("Турция", "ОАЭ", "Египет");
    }

    @Test
    @DisplayName("Фиксация просмотра сохраняет запись для авторизованного пользователя")
    void recordTourView_whenUserAuthenticated_savesView() {
        User user = User.builder().id(2L).email("ivan@nativetravel.local").build();

        when(userRepository.findByEmail("ivan@nativetravel.local")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(sampleTour));
        when(viewedTourRepository.findByUserAndTour(user, sampleTour)).thenReturn(Optional.empty());

        tourService.recordTourView(1L, "ivan@nativetravel.local");

        verify(viewedTourRepository).save(any());
    }
}
