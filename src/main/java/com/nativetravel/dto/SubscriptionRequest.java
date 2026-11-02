package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

/**
 * Запрос подписки на рассылку горящих предложений.
 */
@Schema(description = "Запрос подписки на рассылку горящих предложений")
public record SubscriptionRequest(
        @Schema(description = "Email адрес подписчика", example = "traveler@example.com")
        @Email(message = "Некорректный формат email адреса")
        String email,

        @Schema(description = "Telegram Chat ID (если подписка через Telegram)", example = "123456789")
        Long telegramChatId
) {
}
