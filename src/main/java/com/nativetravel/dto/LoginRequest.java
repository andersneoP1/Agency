package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на авторизацию в системе.
 */
@Schema(description = "Учетные данные для входа в систему")
public record LoginRequest(
        @Schema(description = "Email адрес", example = "alex@example.com")
        @NotBlank(message = "Email обязателен для заполнения")
        @Email(message = "Некорректный формат email адреса")
        String email,

        @Schema(description = "Пароль", example = "Secret123!")
        @NotBlank(message = "Пароль обязателен для заполнения")
        String password
) {
}
