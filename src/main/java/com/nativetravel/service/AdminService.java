package com.nativetravel.service;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.DashboardStatsDto;
import com.nativetravel.dto.HotelCreateRequest;
import com.nativetravel.dto.HotelDto;
import com.nativetravel.dto.PromoCodeCreateRequest;
import com.nativetravel.dto.PromoCodeDto;
import com.nativetravel.dto.TourCreateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Сервис административной панели: аналитика, CRUD туров, отелей, промокодов, пользователей и бронирований.
 */
public interface AdminService {

    /**
     * Сводная статистика и данные для графиков Chart.js.
     */
    DashboardStatsDto getDashboardStats();

    /**
     * Список всех туров для администратора.
     */
    Page<TourDto> getAllTours(Pageable pageable);

    /**
     * Создание нового тура.
     */
    TourDto createTour(TourCreateRequest request);

    /**
     * Редактирование существующего тура.
     */
    TourDto updateTour(Long id, TourCreateRequest request);

    /**
     * Удаление тура по ID.
     */
    void deleteTour(Long id);

    /**
     * Создание нового отеля.
     */
    HotelDto createHotel(HotelCreateRequest request);

    /**
     * Редактирование отеля.
     */
    HotelDto updateHotel(Long id, HotelCreateRequest request);

    /**
     * Удаление отеля.
     */
    void deleteHotel(Long id);

    /**
     * Получение списка всех отелей.
     */
    List<HotelDto> getAllHotels();

    /**
     * Список всех пользователей.
     */
    Page<UserDto> getAllUsers(Pageable pageable);

    /**
     * Переключение блокировки/активности пользователя.
     */
    void toggleUserStatus(Long userId);

    /**
     * Список всех промокодов.
     */
    Page<PromoCodeDto> getAllPromoCodes(Pageable pageable);

    /**
     * Создание нового промокода.
     */
    PromoCodeDto createPromoCode(PromoCodeCreateRequest request);

    /**
     * Переключение активности промокода.
     */
    void togglePromoCode(Long id);

    /**
     * Список всех бронирований клиентов.
     */
    Page<BookingDto> getAllBookings(Pageable pageable);

    /**
     * Ручное изменение статуса бронирования.
     */
    void updateBookingStatus(Long bookingId, BookingStatus newStatus);
}

