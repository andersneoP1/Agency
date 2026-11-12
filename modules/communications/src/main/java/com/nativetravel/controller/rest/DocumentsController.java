package com.nativetravel.controller.rest;

import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.repository.BookingRepository;
import com.nativetravel.service.PdfVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Booking PDF vouchers")
public class DocumentsController {
    private final BookingRepository bookings;
    private final PdfVoucherService pdf;

    @GetMapping(value = "/{id}/voucher", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download a voucher belonging to the authenticated customer")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> voucher(@PathVariable Long id, Principal principal) {
        if (principal == null) throw new UnauthorizedException("Authentication required");
        var booking = bookings.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getUser().getEmail().equals(principal.getName())) {
            throw new UnauthorizedException("Booking belongs to another customer");
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=voucher-" + id + ".pdf").body(pdf.generateBookingVoucherPdf(booking));
    }
}
