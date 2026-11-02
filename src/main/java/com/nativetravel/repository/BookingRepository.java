package com.nativetravel.repository;

import com.nativetravel.entity.Booking;
import com.nativetravel.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий бронирований.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Поиск бронирования по уникальному номеру.
     */
    Optional<Booking> findByBookingNumber(String bookingNumber);

    /**
     * Поиск всех бронирований конкретного пользователя.
     */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Все бронирования для панели администратора.
     */
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Подсчет количества бронирований по статусу.
     */
    long countByStatus(BookingStatus status);

    /**
     * Получение бронирования с предзагрузкой всех связанных сущностей.
     */
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.tour t " +
           "LEFT JOIN FETCH t.hotel " +
           "LEFT JOIN FETCH b.flight " +
           "LEFT JOIN FETCH b.promoCode " +
           "LEFT JOIN FETCH b.qrCode " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    /**
     * Получение бронирования по номеру с предзагрузкой.
     */
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.tour t " +
           "LEFT JOIN FETCH t.hotel " +
           "LEFT JOIN FETCH b.flight " +
           "LEFT JOIN FETCH b.promoCode " +
           "LEFT JOIN FETCH b.qrCode " +
           "WHERE b.bookingNumber = :bookingNumber")
    Optional<Booking> findByBookingNumberWithDetails(@Param("bookingNumber") String bookingNumber);
}

