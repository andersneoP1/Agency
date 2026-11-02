package com.nativetravel.service;

/**
 * Сервис генерации QR-кодов для электронных ваучеров и билетов.
 */
public interface QrCodeService {

    /**
     * Генерация QR-кода в формате PNG байтов.
     *
     * @param content текст или URL для кодирования
     * @param width ширина в пикселях
     * @param height высота в пикселях
     * @return массив байтов PNG-изображения
     */
    byte[] generateQrCodePng(String content, int width, int height);

    /**
     * Генерация QR-кода в формате Base64 Data URI для прямого отображения в HTML.
     *
     * @param content текст или URL
     * @param width ширина
     * @param height высота
     * @return строка вида "data:image/png;base64,..."
     */
    String generateQrCodeBase64(String content, int width, int height);
}
