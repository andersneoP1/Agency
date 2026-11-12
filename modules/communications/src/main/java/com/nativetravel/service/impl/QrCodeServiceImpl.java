package com.nativetravel.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nativetravel.service.QrCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Реализация сервиса генерации QR-кодов с использованием библиотеки ZXing.
 */
@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public byte[] generateQrCodePng(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate QR code for content: {}", content, e);
            throw new RuntimeException("Ошибка генерации QR-кода", e);
        }
    }

    @Override
    public String generateQrCodeBase64(String content, int width, int height) {
        byte[] pngData = generateQrCodePng(content, width, height);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
    }
}

