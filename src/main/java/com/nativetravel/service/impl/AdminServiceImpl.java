package com.nativetravel.service.impl;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.DashboardStatsDto;
import com.nativetravel.dto.HotelCreateRequest;
import com.nativetravel.dto.HotelDto;
import com.nativetravel.dto.PromoCodeCreateRequest;
import com.nativetravel.dto.PromoCodeDto;
import com.nativetravel.dto.TourCreateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.Booking;
import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.Hotel;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.BookingMapper;
import com.nativetravel.mapper.HotelMapper;
import com.nativetravel.mapper.PromoCodeMapper;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.BookingRepository;
import com.nativetravel.repository.HotelRepository;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.repository.ReviewRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса административной панели.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ReviewRepository reviewRepository;

    private final TourMapper tourMapper;
    private final HotelMapper hotelMapper;
    private final UserMapper userMapper;
    private final PromoCodeMapper promoCodeMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        List<Booking> allBookings = bookingRepository.findAll();

        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PAID).count();
        long pendingCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long cancelledCount = allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put("PAID", paidCount);
        statusDistribution.put("PENDING", pendingCount);
        statusDistribution.put("CANCELLED", cancelledCount);

        // Monthly revenue map
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            monthlyRevenue.put(ym.format(monthFmt), BigDecimal.ZERO);
        }

        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.PAID && b.getCreatedAt() != null) {
                YearMonth ym = YearMonth.from(b.getCreatedAt().atZone(ZoneId.systemDefault()));
                String key = ym.format(monthFmt);
                if (monthlyRevenue.containsKey(key)) {
                    monthlyRevenue.put(key, monthlyRevenue.get(key).add(b.getTotalPrice()));
                }
            }
        }

        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();
        long pendingReviews = reviewRepository.countByApprovedFalse();

        return new DashboardStatsDto(
                totalRevenue,
                allBookings.size(),
                paidCount,
                totalUsers,
                totalTours,
                pendingReviews,
                statusDistribution,
                monthlyRevenue
        );
    }

    @Override
    @Transactional
    public TourDto createTour(TourCreateRequest request) {
        Hotel hotel = null;
        if (request.hotelId() != null) {
            hotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Отель с id " + request.hotelId() + " не найден"));
        }

        Tour tour = Tour.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .country(request.country().trim())
                .city(request.city().trim())
                .hotel(hotel)
                .price(request.price())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .durationDays(request.durationDays())
                .includedServices(request.includedServices())
                .imageUrl(request.imageUrl())
                .hotOffer(request.hotOffer())
                .availableSlots(request.availableSlots())
                .build();

        Tour saved = tourRepository.save(tour);
        log.info("Admin created tour #{}", saved.getId());
        return tourMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TourDto updateTour(Long id, TourCreateRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Тур с id " + id + " не найден"));

        if (request.hotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Отель не найден"));
            tour.setHotel(hotel);
        }

        tour.setTitle(request.title().trim());
        tour.setDescription(request.description().trim());
        tour.setCountry(request.country().trim());
        tour.setCity(request.city().trim());
        tour.setPrice(request.price());
        tour.setStartDate(request.startDate());
        tour.setEndDate(request.endDate());
        tour.setDurationDays(request.durationDays());
        tour.setIncludedServices(request.includedServices());
        tour.setImageUrl(request.imageUrl());
        tour.setHotOffer(request.hotOffer());
        tour.setAvailableSlots(request.availableSlots());

        Tour updated = tourRepository.save(tour);
        log.info("Admin updated tour #{}", id);
        return tourMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            throw new ResourceNotFoundException("Тур не найден");
        }
        tourRepository.deleteById(id);
        log.info("Admin deleted tour #{}", id);
    }

    @Override
    @Transactional
    public HotelDto createHotel(HotelCreateRequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.name().trim())
                .country(request.country().trim())
                .city(request.city().trim())
                .address(request.address())
                .stars(request.stars())
                .description(request.description())
                .amenities(request.amenities())
                .imageUrl(request.imageUrl())
                .build();

        Hotel saved = hotelRepository.save(hotel);
        log.info("Admin created hotel #{}", saved.getId());
        return hotelMapper.toDto(saved);
    }

    @Override
    @Transactional
    public HotelDto updateHotel(Long id, HotelCreateRequest request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отель с id " + id + " не найден"));

        hotel.setName(request.name().trim());
        hotel.setCountry(request.country().trim());
        hotel.setCity(request.city().trim());
        hotel.setAddress(request.address());
        hotel.setStars(request.stars());
        hotel.setDescription(request.description());
        hotel.setAmenities(request.amenities());
        hotel.setImageUrl(request.imageUrl());

        Hotel updated = hotelRepository.save(hotel);
        log.info("Admin updated hotel #{}", id);
        return hotelMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Отель не найден");
        }
        hotelRepository.deleteById(id);
        log.info("Admin deleted hotel #{}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        log.info("Admin toggled user #{} status to enabled={}", userId, user.isEnabled());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromoCodeDto> getAllPromoCodes(Pageable pageable) {
        return promoCodeRepository.findAll(pageable)
                .map(promoCodeMapper::toDto);
    }

    @Override
    @Transactional
    public PromoCodeDto createPromoCode(PromoCodeCreateRequest request) {
        if (promoCodeRepository.findByCodeIgnoreCase(request.code().trim()).isPresent()) {
            throw new BadRequestException("Промокод с таким именем уже существует");
        }

        PromoCode promo = PromoCode.builder()
                .code(request.code().trim().toUpperCase())
                .discountPercent(request.discountPercent())
                .minOrderAmount(request.minOrderAmount())
                .maxUses(request.maxUses())
                .currentUses(0)
                .validUntil(Instant.now().plus(request.validDays(), ChronoUnit.DAYS))
                .active(true)
                .build();

        PromoCode saved = promoCodeRepository.save(promo);
        log.info("Admin created promo code: {}", saved.getCode());
        return promoCodeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void togglePromoCode(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Промокод не найден"));
        promo.setActive(!Boolean.TRUE.equals(promo.getActive()));
        promoCodeRepository.save(promo);
        log.info("Admin toggled promo code #{} active={}", id, promo.getActive());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDto> getAllBookings(Pageable pageable) {
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(bookingMapper::toDto);
    }

    @Override
    @Transactional
    public void updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование не найдено"));
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        log.info("Admin updated booking #{} status to {}", booking.getBookingNumber(), newStatus);
    }
}
