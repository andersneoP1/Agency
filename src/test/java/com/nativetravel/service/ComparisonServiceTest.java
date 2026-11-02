package com.nativetravel.service;

import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.Tour;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.service.impl.ComparisonServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ComparisonService")
class ComparisonServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private ComparisonServiceImpl comparisonService;

    @Test
    @DisplayName("Сравнение пустого списка возвращает пустой список")
    void compareTours_emptyList_returnsEmpty() {
        List<TourDto> result = comparisonService.compareTours(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Сравнение более 4 туров выбрасывает BadRequestException")
    void compareTours_moreThanFour_throwsException() {
        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L);

        assertThatThrownBy(() -> comparisonService.compareTours(ids))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("не более 4 туров");
    }

    @Test
    @DisplayName("Сравнение допустимого количества туров возвращает список DTO")
    void compareTours_validIds_returnsTourDtos() {
        Tour tour1 = Tour.builder().id(1L).title("Тур 1").price(BigDecimal.valueOf(50000)).build();
        Tour tour2 = Tour.builder().id(2L).title("Тур 2").price(BigDecimal.valueOf(70000)).build();

        TourDto dto1 = new TourDto(1L, "Тур 1", null, "Турция", "Анталья", null, BigDecimal.valueOf(50000), LocalDate.now(), LocalDate.now().plusDays(7), 7, null, null, false, 5);
        TourDto dto2 = new TourDto(2L, "Тур 2", null, "ОАЭ", "Дубай", null, BigDecimal.valueOf(70000), LocalDate.now(), LocalDate.now().plusDays(7), 7, null, null, false, 5);

        when(tourRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(tour1, tour2));
        when(tourMapper.toDto(tour1)).thenReturn(dto1);
        when(tourMapper.toDto(tour2)).thenReturn(dto2);

        List<TourDto> result = comparisonService.compareTours(List.of(1L, 2L));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TourDto::title).containsExactly("Тур 1", "Тур 2");
    }
}
