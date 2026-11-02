package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Запрос на обновление access-токена (если передан в теле, иначе читается из HttpOnly Cookie).
 */
@Schema(description = "Запрос на обновление access-токена")
public record TokenRefreshRequest(
        @Schema(description = "Refresh токен (опционально, если не передан через Cookie)")
        String refreshToken
) {
}
