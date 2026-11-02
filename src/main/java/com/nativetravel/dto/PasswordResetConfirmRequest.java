package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на подтверждение сброса пароля с кодом и новым паролем.
 */
@Schema(description = "Запрос на установку нового пароля по коду подтверждения")
public record PasswordResetConfirmRequest(
        @Schema(description = "Email адрес пользователя", example = "alex@example.com")
        @NotBlank(message = "Email обязателен для заполнения")
        @Email(message = "Некорректный формат email адреса")
        String email,

        @Schema(description = "6-значный код подтверждения из письма", example = "123456")
        @NotBlank(message = "Код подтверждения обязателен")
        @Pattern(regexp = "^[0-9]{6}$", message = "Код подтверждения должен состоять ровно из 6 цифр")
        String code,

        @Schema(description = "Новый пароль (минимум 8 символов, заглавная, строчная буква и цифра)", example = "NewSecret123!")
        @NotBlank(message = "Новый пароль обязателен")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
                message = "Пароль должен содержать как минимум одну цифру, одну строчную и одну заглавную букву"
        )
        String newPassword
) {
}
