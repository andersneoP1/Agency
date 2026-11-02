package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO сообщения чата службы поддержки.
 */
@Schema(description = "Сообщение в чате поддержки")
public record SupportMessageDto(
        @Schema(description = "ID сообщения", example = "1")
        Long id,

        @Schema(description = "Тип отправителя: USER, BOT, AGENT", example = "BOT")
        String senderType,

        @Schema(description = "Текст сообщения", example = "Здравствуйте! Чем могу помочь?")
        String message,

        @Schema(description = "Идентификатор сессии браузера", example = "session-12345")
        String sessionId,

        @Schema(description = "Флаг эскалации на живого оператора", example = "false")
        boolean escalated,

        @Schema(description = "Время отправки")
        Instant createdAt
) {
}

