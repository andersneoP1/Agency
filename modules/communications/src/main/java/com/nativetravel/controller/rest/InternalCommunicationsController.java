package com.nativetravel.controller.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.distributed.InternalRequests.*;
import com.nativetravel.entity.*;
import com.nativetravel.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Hidden
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class InternalCommunicationsController {
    private final EmailService email;
    private final QrCodeService qr;
    private final PdfVoucherService pdf;
    private final ObjectMapper mapper;

    @PostMapping("/email/reset")
    public void reset(@Valid @RequestBody Email request) {
        email.sendPasswordResetCode(request.recipient(), request.code());
    }
    @PostMapping("/email/booking")
    public void confirmation(@Valid @RequestBody Email request) {
        email.sendBookingConfirmation(request.recipient(), request.bookingNumber(), request.qr(), request.pdf());
    }
    @PostMapping("/email/newsletter")
    public void newsletter(@Valid @RequestBody Email request) {
        email.sendHotOffersNewsletter(request.recipient(), request.content());
    }
    @PostMapping(value = "/qr/base64", produces = MediaType.TEXT_PLAIN_VALUE)
    public String qrBase64(@Valid @RequestBody Qr request) {
        return qr.generateQrCodeBase64(request.content(), request.width(), request.height());
    }
    @PostMapping(value = "/qr/png", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrPng(@Valid @RequestBody Qr request) {
        return qr.generateQrCodePng(request.content(), request.width(), request.height());
    }
    @PostMapping(value = "/documents/voucher", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] voucher(@Valid @RequestBody Voucher request) {
        var dto = request.booking();
        ObjectMapper transport = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Booking snapshot = Booking.builder().id(dto.id()).bookingNumber(dto.bookingNumber())
                .createdAt(dto.createdAt()).status(dto.status()).paymentMethod(dto.paymentMethod())
                .originalPrice(dto.originalPrice()).discountAmount(dto.discountAmount()).totalPrice(dto.totalPrice())
                .user(User.builder().firstName(request.firstName()).lastName(request.lastName()).email(request.email()).build())
                .tour(dto.tour() == null ? null : transport.convertValue(dto.tour(), Tour.class))
                .flight(dto.flight() == null ? null : transport.convertValue(dto.flight(), Flight.class)).build();
        return pdf.generateBookingVoucherPdf(snapshot);
    }
}
