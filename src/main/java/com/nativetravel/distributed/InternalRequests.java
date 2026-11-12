package com.nativetravel.distributed;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.PaymentRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Explicit transport contracts: no user credentials or JPA graphs cross HTTP. */
public final class InternalRequests {
    private InternalRequests() {}
    public record Email(@NotBlank @jakarta.validation.constraints.Email String recipient,
                        String code, String bookingNumber, byte[] qr, byte[] pdf, String content) {}
    public record Qr(@NotBlank @Size(max = 4096) String content,
                     @Min(32) @Max(1024) int width, @Min(32) @Max(1024) int height) {}
    public record Promo(@NotBlank String code, @NotNull @DecimalMin("0") BigDecimal amount) {}
    public record Id(@NotNull @Positive Long id) {}
    public record Referral(@NotNull @Positive Long userId, String code) {}
    public record EmailLookup(@NotBlank String email) {}
    public record Payment(@NotNull @Valid PaymentRequest request, @NotBlank String userEmail) {}
    public record Voucher(@NotNull BookingDto booking, String firstName, String lastName, String email) {}
    public record PromoSnapshot(Long id, String code, Integer discountPercent) {
        public com.nativetravel.entity.PromoCode toEntity() {
            return com.nativetravel.entity.PromoCode.builder().id(id).code(code)
                    .discountPercent(discountPercent).build();
        }
    }
}
