package com.nativetravel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record PromoCodeUpdateRequest(
    @NotBlank @Size(max = 50) String code,
    @NotNull @Min(1) @Max(100) Integer discountPercent,
    @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal minOrderAmount,
    @NotNull @Min(1) Integer maxUses,
    @NotNull Instant validUntil,
    @NotNull Boolean active
) {
}
