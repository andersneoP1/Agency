package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ответ при успешной аутентификации.
 */
@Schema(description = "Ответ сервера с JWT-токеном доступа и информацией о пользователе")
public record AuthResponse(
        @Schema(description = "Access JWT токен для авторизации запросов", example = "eyJhbGciOiJIUzUxMiJ9...")
        String accessToken,

        @Schema(description = "Тип токена", example = "Bearer")
        String tokenType,

        @Schema(description = "Время жизни access-токена в секундах", example = "900")
        long expiresIn,

        @Schema(description = "Данные авторизованного пользователя")
        UserDto user
) {
    public static AuthResponse of(String accessToken, long expiresIn, UserDto user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, user);
    }
}
