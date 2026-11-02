package com.nativetravel.repository;

import com.nativetravel.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий QR-кодов бронирований.
 */
@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    /**
     * Поиск QR-кода по идентификатору бронирования.
     */
    Optional<QRCode> findByBookingId(Long bookingId);
}
