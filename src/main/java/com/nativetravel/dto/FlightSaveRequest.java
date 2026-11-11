package com.nativetravel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record FlightSaveRequest(
    @NotBlank @Size(max = 50) String flightNumber,
    @NotBlank @Size(max = 100) String airline,
    @NotBlank @Size(max = 100) String departureCity,
    @NotBlank @Size(max = 100) String arrivalCity,
    @NotNull Instant departureTime,
    @NotNull Instant arrivalTime,
    @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal price,
    @NotNull @Min(0) Integer availableSeats
) {
}
