package com.nativetravel.service.impl;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.BookingRequest;
import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PaymentResponse;
import com.nativetravel.entity.Booking;
import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.Flight;
import com.nativetravel.entity.PaymentMethod;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.entity.QRCode;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.mapper.BookingMapper;
import com.nativetravel.repository.BookingRepository;
import com.nativetravel.repository.CartItemRepository;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.BookingService;
import com.nativetravel.service.EmailService;
import com.nativetravel.service.PdfVoucherService;
import com.nativetravel.service.PromoCodeService;
import com.nativetravel.service.QrCodeService;
import com.nativetravel.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Year;

/**
 * Реализация сервиса управления бронированиями и проведением платежей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final FlightRepository flightRepository;
    private final CartItemRepository cartItemRepository;
    private final PromoCodeService promoCodeService;
    private final QrCodeService qrCodeService;
    private final PdfVoucherService pdfVoucherService;
    private final EmailService emailService;
    private final ReferralService referralService;
    private final BookingMapper bookingMapper;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public BookingDto createBooking(BookingRequest request, String userEmail) {
        if (request.tourId() == null && request.flightId() == null) {
            throw new BadRequestException("Для оформления бронирования необходимо выбрать тур или авиабилет");
        }

        User user = getUser(userEmail);
        Tour tour = null;
        Flight flight = null;
        BigDecimal originalPrice = BigDecimal.ZERO;

        if (request.tourId() != null) {
            tour = tourRepository.findById(request.tourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Тур с id " + request.tourId() + " не найден"));
            if (tour.getAvailableSlots() <= 0) {
                throw new BadRequestException("К сожалению, на тур '" + tour.getTitle() + "' закончились свободные места");
            }
            originalPrice = originalPrice.add(tour.getPrice());
        }

        if (request.flightId() != null) {
            flight = flightRepository.findById(request.flightId())
                    .orElseThrow(() -> new ResourceNotFoundException("Рейс с id " + request.flightId() + " не найден"));
            if (flight.getAvailableSeats() <= 0) {
                throw new BadRequestException("К сожалению, на рейс '" + flight.getFlightNumber() + "' закончились свободные места");
            }
            originalPrice = originalPrice.add(flight.getPrice());
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        PromoCode promo = null;

        if (request.promoCode() != null && !request.promoCode().trim().isEmpty()) {
            promo = promoCodeService.getValidPromoCode(request.promoCode().trim(), originalPrice);
            BigDecimal discountMultiplier = BigDecimal.valueOf(promo.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discountAmount = originalPrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalPrice = originalPrice.subtract(discountAmount);
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        String bookingNumber = generateBookingNumber();

        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .user(user)
                .tour(tour)
                .flight(flight)
                .promoCode(promo)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .paymentMethod(request.paymentMethod() != null ? request.paymentMethod() : PaymentMethod.CARD)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Created booking #{} for user {} (amount: {} RUB, discount: {} RUB)",
                bookingNumber, userEmail, totalPrice, discountAmount);

        return bookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String userEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с id " + request.bookingId() + " не найдено"));

        User user = getUser(userEmail);
        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedException("Вы не можете оплатить чужое бронирование");
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            String qrBase64 = booking.getQrCode() != null ? booking.getQrCode().getQrData() : "";
            return new PaymentResponse(
                    booking.getId(),
                    booking.getBookingNumber(),
                    booking.getStatus(),
                    booking.getPaymentMethod(),
                    booking.getTotalPrice(),
                    qrBase64,
                    "Бронирование уже было успешно оплачено ранее"
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Нельзя оплатить отмененное бронирование");
        }

        // Apply payment method
        booking.setPaymentMethod(request.paymentMethod());
        booking.setStatus(BookingStatus.PAID);

        // Decrement availability
        if (booking.getTour() != null && booking.getTour().getAvailableSlots() > 0) {
            booking.getTour().setAvailableSlots(booking.getTour().getAvailableSlots() - 1);
            tourRepository.save(booking.getTour());
        }
        if (booking.getFlight() != null && booking.getFlight().getAvailableSeats() > 0) {
            booking.getFlight().setAvailableSeats(booking.getFlight().getAvailableSeats() - 1);
            flightRepository.save(booking.getFlight());
        }

        // Increment promo code if used
        if (booking.getPromoCode() != null) {
            promoCodeService.incrementUses(booking.getPromoCode());
        }

        // Reward referrer if user was invited by friend
        referralService.checkAndRewardReferrer(user);

        // Generate QR code and link to booking
        String qrContent = "https://nativetravel.local/verify-booking?number=" + booking.getBookingNumber()
                + "&amount=" + booking.getTotalPrice() + "&user=" + user.getEmail();
        String qrBase64 = qrCodeService.generateQrCodeBase64(qrContent, 300, 300);
        byte[] qrPngBytes = qrCodeService.generateQrCodePng(qrContent, 300, 300);

        QRCode qrCode = QRCode.builder()
                .booking(booking)
                .qrData(qrBase64)
                .build();
        booking.setQrCode(qrCode);

        Booking saved = bookingRepository.save(booking);

        // Generate PDF voucher
        byte[] pdfVoucher = pdfVoucherService.generateBookingVoucherPdf(saved);

        // Send confirmation email with QR and PDF voucher
        emailService.sendBookingConfirmation(user.getEmail(), saved.getBookingNumber(), qrPngBytes, pdfVoucher);

        // Remove item from cart if user had it there
        try {
            if (saved.getTour() != null) {
                cartItemRepository.findByUserIdAndTourId(user.getId(), saved.getTour().getId())
                        .ifPresent(cartItemRepository::delete);
            }
            if (saved.getFlight() != null) {
                cartItemRepository.findByUserIdAndFlightId(user.getId(), saved.getFlight().getId())
                        .ifPresent(cartItemRepository::delete);
            }
        } catch (Exception e) {
            log.warn("Non-critical error clearing cart after booking payment: {}", e.getMessage());
        }

        log.info("Booking #{} successfully PAID via {} for user {}", saved.getBookingNumber(), request.paymentMethod(), userEmail);

        return new PaymentResponse(
                saved.getId(),
                saved.getBookingNumber(),
                saved.getStatus(),
                saved.getPaymentMethod(),
                saved.getTotalPrice(),
                qrBase64,
                "Оплата успешно завершена! Ваш ваучер и QR-код отправлены на email " + user.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long id, String userEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с id " + id + " не найдено"));
        validateAccess(booking, userEmail);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingByNumber(String bookingNumber, String userEmail) {
        Booking booking = bookingRepository.findByBookingNumberWithDetails(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование #" + bookingNumber + " не найдено"));
        validateAccess(booking, userEmail);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getUserBookings(String userEmail, Pageable pageable) {
        User user = getUser(userEmail);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadVoucherPdf(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование не найдено"));
        validateAccess(booking, userEmail);
        return pdfVoucherService.generateBookingVoucherPdf(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование не найдено"));
        validateAccess(booking, userEmail);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            // Restore inventory slots
            if (booking.getTour() != null) {
                booking.getTour().setAvailableSlots(booking.getTour().getAvailableSlots() + 1);
                tourRepository.save(booking.getTour());
            }
            if (booking.getFlight() != null) {
                booking.getFlight().setAvailableSeats(booking.getFlight().getAvailableSeats() + 1);
                flightRepository.save(booking.getFlight());
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking #{} was CANCELLED by user {}", booking.getBookingNumber(), userEmail);
    }

    private void validateAccess(Booking booking, String userEmail) {
        User user = getUser(userEmail);
        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedException("Доступ к данному бронированию запрещен");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + email + " не найден"));
    }

    private String generateBookingNumber() {
        int year = Year.now().getValue();
        int seq = 100000 + random.nextInt(900000);
        return "NT-" + year + "-" + seq;
    }
}
