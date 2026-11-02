package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO подписки на рассылку горящих предложений.
 */
@Schema(description = "DTO подписки на горящие туры")
public record SubscriptionDto(
        @Schema(description = "ID подписки", example = "1")
        Long id,

        @Schema(description = "Email адрес", example = "traveler@example.com")
        String email,

        @Schema(description = "Telegram Chat ID", example = "123456789")
        Long telegramChatId,

        @Schema(description = "Статус активности", example = "true")
        boolean active,

        @Schema(description = "Дата оформления подписки")
        Instant createdAt
) {
}

