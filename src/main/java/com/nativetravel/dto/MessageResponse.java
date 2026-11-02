package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Стандартный ответ сервера с текстовым сообщением.
 */
@Schema(description = "Ответ сервера с информационным сообщением")
public record MessageResponse(
        @Schema(description = "Текст сообщения", example = "Операция выполнена успешно")
        String message,

        @Schema(description = "Временная метка ответа")
        Instant timestamp
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message, Instant.now());
    }
}
