package com.nativetravel.service;

import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.FavoriteTour;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.FavoriteTourRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование FavoriteService")
class FavoriteServiceTest {

    @Mock
    private FavoriteTourRepository favoriteTourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    private User sampleUser;
    private Tour sampleTour;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("user@example.com").firstName("Иван").lastName("Петров").build();
        sampleTour = Tour.builder().id(10L).title("Тур в Сочи").price(BigDecimal.valueOf(35000)).build();
    }

    @Test
    @DisplayName("Добавление тура в избранное сохраняет запись")
    void addFavorite_savesFavorite() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(sampleTour));
        when(favoriteTourRepository.existsByUserAndTour(sampleUser, sampleTour)).thenReturn(false);

        favoriteService.addFavorite(10L, "user@example.com");

        verify(favoriteTourRepository).save(any(FavoriteTour.class));
    }

    @Test
    @DisplayName("Удаление тура из избранного вызывает delete")
    void removeFavorite_deletesFavorite() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(sampleTour));

        favoriteService.removeFavorite(10L, "user@example.com");

        verify(favoriteTourRepository).deleteByUserAndTour(sampleUser, sampleTour);
    }

    @Test
    @DisplayName("Проверка избранного возвращает true, если тур добавлен")
    void isFavorite_returnsTrue() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(sampleTour));
        when(favoriteTourRepository.existsByUserAndTour(sampleUser, sampleTour)).thenReturn(true);

        boolean fav = favoriteService.isFavorite(10L, "user@example.com");

        assertThat(fav).isTrue();
    }

    @Test
    @DisplayName("Получение избранных туров пользователя с пагинацией")
    void getUserFavorites_returnsPage() {
        FavoriteTour ft = FavoriteTour.builder().id(1L).user(sampleUser).tour(sampleTour).build();
        Page<FavoriteTour> page = new PageImpl<>(List.of(ft));
        PageRequest pageable = PageRequest.of(0, 10);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(favoriteTourRepository.findByUser(sampleUser, pageable)).thenReturn(page);
        when(tourMapper.toDto(sampleTour)).thenReturn(new TourDto(10L, "Тур в Сочи", "Описание", "Россия", "Сочи", null, BigDecimal.valueOf(35000), null, null, 7, "Все включено", null, false, 10));

        Page<TourDto> result = favoriteService.getUserFavorites("user@example.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Тур в Сочи");
    }
}
