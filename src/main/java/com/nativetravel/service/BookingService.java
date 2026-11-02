package com.nativetravel.service;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.BookingRequest;
import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис управления бронированиями, жизненным циклом заказов и проведением платежей.
 */
public interface BookingService {

    /**
     * Оформление предварительного бронирования со скидкой по промокоду.
     *
     * @param request параметры бронирования
     * @param userEmail email авторизованного пользователя
     * @return DTO созданного бронирования
     */
    BookingDto createBooking(BookingRequest request, String userEmail);

    /**
     * Проведение оплаты бронирования с генерацией QR-кода, PDF-ваучера и отправкой письма.
     *
     * @param request реквизиты и параметры платежа
     * @param userEmail email плательщика
     * @return результат оплаты и подтверждения
     */
    PaymentResponse processPayment(PaymentRequest request, String userEmail);

    /**
     * Получение бронирования по ID с проверкой прав доступа.
     *
     * @param id ID бронирования
     * @param userEmail email пользователя
     * @return DTO бронирования
     */
    BookingDto getBookingById(Long id, String userEmail);

    /**
     * Получение бронирования по номеру.
     *
     * @param bookingNumber номер бронирования (NT-YYYY-XXXXXX)
     * @param userEmail email пользователя
     * @return DTO бронирования
     */
    BookingDto getBookingByNumber(String bookingNumber, String userEmail);

    /**
     * Получение истории всех бронирований текущего пользователя.
     *
     * @param userEmail email пользователя
     * @param pageable параметры пагинации
     * @return страница бронирований
     */
    Page<BookingDto> getUserBookings(String userEmail, Pageable pageable);

    /**
     * Генерация и скачивание PDF ваучера бронирования.
     *
     * @param bookingId ID бронирования
     * @param userEmail email пользователя
     * @return байты файла PDF
     */
    byte[] downloadVoucherPdf(Long bookingId, String userEmail);

    /**
     * Отмена неоплаченного бронирования.
     *
     * @param bookingId ID бронирования
     * @param userEmail email пользователя
     */
    void cancelBooking(Long bookingId, String userEmail);
}

