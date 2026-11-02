package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO представления авиарейса.
 */
@Schema(description = "Информация об авиарейсе")
public record FlightDto(
        @Schema(description = "ID рейса", example = "1")
        Long id,

        @Schema(description = "Номер рейса", example = "SU-2144")
        String flightNumber,

        @Schema(description = "Авиакомпания", example = "Аэрофлот")
        String airline,

        @Schema(description = "Город вылета", example = "Москва")
        String departureCity,

        @Schema(description = "Город прилета", example = "Анталья")
        String arrivalCity,

        @Schema(description = "Время вылета (UTC)")
        Instant departureTime,

        @Schema(description = "Время прилета (UTC)")
        Instant arrivalTime,

        @Schema(description = "Стоимость билета", example = "18500.00")
        BigDecimal price,

        @Schema(description = "Доступные места", example = "42")
        int availableSeats
) {
}

