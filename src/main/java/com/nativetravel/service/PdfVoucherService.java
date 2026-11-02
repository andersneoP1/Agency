package com.nativetravel.service;

import com.nativetravel.entity.Booking;

/**
 * Сервис генерации PDF ваучеров путешествий с использованием OpenPDF.
 */
public interface PdfVoucherService {

    /**
     * Генерация официального PDF-ваучера бронирования с внедренным QR-кодом.
     *
     * @param booking сущность бронирования со всеми деталями
     * @return массив байтов сформированного PDF документа
     */
    byte[] generateBookingVoucherPdf(Booking booking);
}
