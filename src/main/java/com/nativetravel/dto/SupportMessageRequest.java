package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос отправки сообщения в чат поддержки.
 */
@Schema(description = "Запрос отправки сообщения в чат")
public record SupportMessageRequest(
        @Schema(description = "Идентификатор сессии клиента", example = "sess-abc-123")
        @NotBlank(message = "ID сессии обязателен")
        String sessionId,

        @Schema(description = "Текст сообщения", example = "Подскажите, как оплатить тур?")
        @NotBlank(message = "Сообщение не может быть пустым")
        @Size(max = 2000, message = "Сообщение не должно превышать 2000 символов")
        String message
) {
}

