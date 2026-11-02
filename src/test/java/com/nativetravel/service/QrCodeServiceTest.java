package com.nativetravel.service;

import com.nativetravel.service.impl.QrCodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование QrCodeService")
class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeServiceImpl();

    @Test
    @DisplayName("Генерация QR-кода возвращает валидные PNG байты")
    void generateQrCodePng_returnsPngBytes() {
        byte[] png = qrCodeService.generateQrCodePng("https://nativetravel.local/test", 200, 200);

        assertThat(png).isNotEmpty();
        // PNG magic bytes: 0x89 0x50 0x4E 0x47
        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(png[1]).isEqualTo((byte) 'P');
        assertThat(png[2]).isEqualTo((byte) 'N');
        assertThat(png[3]).isEqualTo((byte) 'G');
    }

    @Test
    @DisplayName("Генерация QR-кода в формате Base64 возвращает Data URI")
    void generateQrCodeBase64_returnsDataUri() {
        String base64 = qrCodeService.generateQrCodeBase64("https://nativetravel.local/test", 200, 200);

        assertThat(base64).isNotBlank();
        assertThat(base64).startsWith("data:image/png;base64,");
    }
}
