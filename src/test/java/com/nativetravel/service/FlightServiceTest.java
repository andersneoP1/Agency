package com.nativetravel.service;

import com.nativetravel.dto.FlightDto;
import com.nativetravel.dto.FlightFilterRequest;
import com.nativetravel.entity.Flight;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.FlightMapper;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.service.impl.FlightServiceImpl;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование FlightService")
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight sampleFlight;
    private FlightDto sampleDto;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        sampleFlight = Flight.builder()
                .id(1L)
                .flightNumber("SU-2130")
                .airline("Аэрофлот")
                .departureCity("Москва")
                .arrivalCity("Анталья")
                .departureTime(now.plus(2, ChronoUnit.DAYS))
                .arrivalTime(now.plus(2, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS))
                .price(BigDecimal.valueOf(18500))
                .availableSeats(45)
                .build();

        sampleDto = new FlightDto(
                1L, "SU-2130", "Аэрофлот", "Москва", "Анталья",
                sampleFlight.getDepartureTime(), sampleFlight.getArrivalTime(),
                BigDecimal.valueOf(18500), 45
        );
    }

    @Test
    @DisplayName("Поиск авиарейсов возвращает страницу результатов")
    void getFlights_returnsPage() {
        FlightFilterRequest filter = new FlightFilterRequest("Москва", "Анталья", null, null, 0, 10);
        Page<Flight> page = new PageImpl<>(List.of(sampleFlight));

        when(flightRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(flightMapper.toDto(sampleFlight)).thenReturn(sampleDto);

        Page<FlightDto> result = flightService.getFlights(filter);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).flightNumber()).isEqualTo("SU-2130");
    }

    @Test
    @DisplayName("Получение рейса по ID возвращает DTO")
    void getFlightById_existing_returnsDto() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(sampleFlight));
        when(flightMapper.toDto(sampleFlight)).thenReturn(sampleDto);

        FlightDto result = flightService.getFlightById(1L);

        assertThat(result).isNotNull();
        assertThat(result.airline()).isEqualTo("Аэрофлот");
    }

    @Test
    @DisplayName("Получение рейса по несуществующему ID выбрасывает ошибку")
    void getFlightById_nonExisting_throwsException() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Рейс с id 999 не найден");
    }

    @Test
    @DisplayName("Получение городов вылета и прилета возвращает списки")
    void getCities_returnsLists() {
        when(flightRepository.findDistinctDepartureCities()).thenReturn(List.of("Москва", "СПб"));
        when(flightRepository.findDistinctArrivalCities()).thenReturn(List.of("Анталья", "Дубай"));

        assertThat(flightService.getDepartureCities()).containsExactly("Москва", "СПб");
        assertThat(flightService.getArrivalCities()).containsExactly("Анталья", "Дубай");
    }
}
