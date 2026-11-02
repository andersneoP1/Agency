package com.nativetravel.service;

/**
 * Сервис отправки почтовых сообщений и уведомлений.
 */
public interface EmailService {

    /**
     * Отправляет одноразовый 6-значный код для сброса пароля.
     *
     * @param toEmail адрес электронной почты получателя
     * @param code 6-значный код подтверждения
     */
    void sendPasswordResetCode(String toEmail, String code);

    /**
     * Отправляет подтверждение бронирования с билетом и ваучером.
     *
     * @param toEmail адрес электронной почты получателя
     * @param bookingNumber номер бронирования
     * @param qrBytes байты изображения QR-кода
     * @param pdfBytes байты PDF-ваучера
     */
    void sendBookingConfirmation(String toEmail, String bookingNumber, byte[] qrBytes, byte[] pdfBytes);
}
