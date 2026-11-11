package com.nativetravel.service;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.BookingRequest;
import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PaymentResponse;
import com.nativetravel.entity.Booking;
import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.PaymentMethod;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.mapper.BookingMapper;
import com.nativetravel.repository.BookingRepository;
import com.nativetravel.repository.CartItemRepository;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование BookingService")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PromoCodeService promoCodeService;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private PdfVoucherService pdfVoucherService;

    @Mock
    private EmailService emailService;

    @Mock
    private ReferralService referralService;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User sampleUser;
    private Tour sampleTour;
    private Booking sampleBooking;
    private BookingDto sampleBookingDto;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("client@example.com").firstName("Иван").lastName("Иванов").build();
        sampleTour = Tour.builder().id(1L).title("Тур в Сочи").price(BigDecimal.valueOf(60000)).availableSlots(10).build();
        sampleBooking = Booking.builder()
                .id(100L)
                .bookingNumber("NT-2026-123456")
                .user(sampleUser)
                .tour(sampleTour)
                .originalPrice(BigDecimal.valueOf(60000))
                .discountAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.valueOf(60000))
                .status(BookingStatus.PENDING)
                .paymentMethod(PaymentMethod.CARD)
                .build();

        sampleBookingDto = new BookingDto(
                100L, "NT-2026-123456", null, null, BigDecimal.valueOf(60000), BigDecimal.ZERO,
                BigDecimal.valueOf(60000), BookingStatus.PENDING, PaymentMethod.CARD, null, null
        );
    }

    @Test
    @DisplayName("Создание бронирования сохраняет заявку со статусом PENDING")
    void createBooking_validRequest_createsPendingBooking() {
        BookingRequest request = new BookingRequest(1L, null, null, PaymentMethod.CARD);

        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(sampleTour));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);
        when(bookingMapper.toDto(sampleBooking)).thenReturn(sampleBookingDto);

        BookingDto result = bookingService.createBooking(request, "client@example.com");

        assertThat(result).isNotNull();
        assertThat(result.bookingNumber()).isEqualTo("NT-2026-123456");
        assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("Проведение оплаты переводит статус в PAID, генерирует QR и PDF ваучер")
    void processPayment_validPayment_changesStatusToPaid() {
        PaymentRequest request = new PaymentRequest(100L, PaymentMethod.CARD, "4276 3800 1234 5678", "IVAN IVANOV", "12/28", "123");

        when(bookingRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(sampleBooking));
        when(userRepository.findByEmail("client@example.com")).thenReturn(Optional.of(sampleUser));
        when(qrCodeService.generateQrCodeBase64(anyString(), anyInt(), anyInt())).thenReturn("data:image/png;base64,mockQr");
        when(qrCodeService.generateQrCodePng(anyString(), anyInt(), anyInt())).thenReturn(new byte[]{1, 2, 3});
        when(pdfVoucherService.generateBookingVoucherPdf(any(Booking.class))).thenReturn(new byte[]{4, 5, 6});
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = bookingService.processPayment(request, "client@example.com");

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(BookingStatus.PAID);
        assertThat(response.qrCodeBase64()).isEqualTo("data:image/png;base64,mockQr");

        verify(emailService).sendBookingConfirmation(anyString(), anyString(), any(), any());
        verify(tourRepository).save(sampleTour);
        assertThat(sampleTour.getAvailableSlots()).isEqualTo(9);
    }
}

