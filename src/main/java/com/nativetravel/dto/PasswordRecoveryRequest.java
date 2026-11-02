package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на отправку кода восстановления пароля.
 */
@Schema(description = "Запрос на восстановление пароля по email")
public record PasswordRecoveryRequest(
        @Schema(description = "Email адрес пользователя", example = "alex@example.com")
        @NotBlank(message = "Email обязателен для заполнения")
        @Email(message = "Некорректный формат email адреса")
        String email
) {
}
