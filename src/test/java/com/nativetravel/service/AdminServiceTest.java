package com.nativetravel.service;

import com.nativetravel.dto.DashboardStatsDto;
import com.nativetravel.dto.HotelCreateRequest;
import com.nativetravel.dto.HotelDto;
import com.nativetravel.dto.PromoCodeCreateRequest;
import com.nativetravel.dto.PromoCodeDto;
import com.nativetravel.dto.TourCreateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.Booking;
import com.nativetravel.entity.BookingStatus;
import com.nativetravel.entity.Hotel;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
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
import com.nativetravel.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование AdminService")
class AdminServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private HotelMapper hotelMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PromoCodeMapper promoCodeMapper;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("Получение дашборда рассчитывает выручку и распределение статусов")
    void getDashboardStats_calculatesMetrics() {
        Booking b1 = Booking.builder().totalPrice(BigDecimal.valueOf(50000)).status(BookingStatus.PAID).createdAt(Instant.now()).build();
        Booking b2 = Booking.builder().totalPrice(BigDecimal.valueOf(30000)).status(BookingStatus.PENDING).createdAt(Instant.now()).build();

        when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));
        when(userRepository.count()).thenReturn(10L);
        when(tourRepository.count()).thenReturn(5L);
        when(reviewRepository.countByApprovedFalse()).thenReturn(2L);

        DashboardStatsDto stats = adminService.getDashboardStats();

        assertThat(stats).isNotNull();
        assertThat(stats.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(stats.totalBookings()).isEqualTo(2);
        assertThat(stats.paidBookings()).isEqualTo(1);
        assertThat(stats.totalUsers()).isEqualTo(10);
        assertThat(stats.totalTours()).isEqualTo(5);
        assertThat(stats.pendingReviews()).isEqualTo(2);
    }

    @Test
    @DisplayName("Создание тура успешно сохраняет сущность")
    void createTour_savesTour() {
        TourCreateRequest req = new TourCreateRequest("Новый тур", "Описание", "ОАЭ", "Дубай", null, BigDecimal.valueOf(80000), LocalDate.now(), LocalDate.now().plusDays(7), 7, "Все включено", null, false, 10);
        Tour saved = Tour.builder().id(100L).title("Новый тур").price(BigDecimal.valueOf(80000)).build();
        TourDto dto = new TourDto(100L, "Новый тур", "Описание", "ОАЭ", "Дубай", null, BigDecimal.valueOf(80000), LocalDate.now(), LocalDate.now().plusDays(7), 7, "Все включено", null, false, 10);

        when(tourRepository.save(any(Tour.class))).thenReturn(saved);
        when(tourMapper.toDto(saved)).thenReturn(dto);

        TourDto result = adminService.createTour(req);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    @DisplayName("Создание отеля сохраняет сущность в базу")
    void createHotel_savesHotel() {
        HotelCreateRequest req = new HotelCreateRequest("Hilton", "Турция", "Анталья", "Beach St", 5, "Отличный отель", "Пляж, Спа", null);
        Hotel saved = Hotel.builder().id(10L).name("Hilton").stars(5).build();
        HotelDto dto = new HotelDto(10L, "Hilton", "Турция", "Анталья", "Beach St", 5, "Отличный отель", "Пляж, Спа", null);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(saved);
        when(hotelMapper.toDto(saved)).thenReturn(dto);

        HotelDto result = adminService.createHotel(req);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        verify(hotelRepository).save(any(Hotel.class));
    }

    @Test
    @DisplayName("Переключение статуса пользователя меняет enabled на противоположный")
    void toggleUserStatus_togglesEnabled() {
        User user = User.builder().id(1L).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.toggleUserStatus(1L);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }
}
