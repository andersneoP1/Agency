package com.nativetravel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.dto.AddToCartRequest;
import com.nativetravel.dto.BookingRequest;
import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PromoCodeValidateRequest;
import com.nativetravel.entity.Booking;
import com.nativetravel.entity.PaymentMethod;
import com.nativetravel.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("Интеграционные тесты корзины, бронирования, промокодов и оплаты (Phase 3)")
class BookingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Валидация промокода WELCOME10 возвращает 10% скидки")
    void validatePromoCode_success() throws Exception {
        PromoCodeValidateRequest req = new PromoCodeValidateRequest("WELCOME10", BigDecimal.valueOf(100000));

        mockMvc.perform(post("/api/v1/promocodes/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WELCOME10"))
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.discountAmount").value(10000.00))
                .andExpect(jsonPath("$.finalAmount").value(90000.00));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("Полный сквозной сценарий: Добавить в корзину -> Оформить бронь -> Оплатить -> Скачать ваучер")
    void endToEndCartBookingPaymentFlow() throws Exception {
        // 1. Add tour to cart
        AddToCartRequest cartReq = new AddToCartRequest(1L, null, 1);
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        // 2. Get cart
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalPrice", greaterThan(0.0)));

        // 3. Create booking with promo code
        BookingRequest bookingReq = new BookingRequest(1L, null, "WELCOME10", PaymentMethod.CARD);
        MvcResult bookingResult = mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.discountAmount", greaterThan(0.0)))
                .andReturn();

        Long bookingId = objectMapper.readTree(bookingResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. Process payment
        PaymentRequest payReq = new PaymentRequest(bookingId, PaymentMethod.CARD, "4276 3800 1234 5678", "IVAN PETROV", "12/28", "123");
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.qrCodeBase64", containsString("data:image/png;base64,")));

        // 5. Download voucher PDF
        mockMvc.perform(get("/api/v1/bookings/" + bookingId + "/voucher"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"voucher-")))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        // 6. Clean up cart
        mockMvc.perform(delete("/api/v1/cart"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("Веб-страницы корзины, чекаута и оплаты открываются с кодом 200")
    void webPages_return200() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/booking?tourId=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}

