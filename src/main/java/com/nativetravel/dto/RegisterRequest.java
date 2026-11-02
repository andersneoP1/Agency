package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Запрос на регистрацию нового пользователя.
 */
@Schema(description = "Данные для регистрации нового пользователя")
public record RegisterRequest(
        @Schema(description = "Имя пользователя", example = "Алексей")
        @NotBlank(message = "Имя обязательно для заполнения")
        @Size(max = 100, message = "Имя не должно превышать 100 символов")
        String firstName,

        @Schema(description = "Фамилия пользователя", example = "Смирнов")
        @NotBlank(message = "Фамилия обязательна для заполнения")
        @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
        String lastName,

        @Schema(description = "Email адрес", example = "alex@example.com")
        @NotBlank(message = "Email обязателен для заполнения")
        @Email(message = "Некорректный формат email адреса")
        String email,

        @Schema(description = "Пароль (минимум 8 символов, заглавная, строчная буква и цифра)", example = "Secret123!")
        @NotBlank(message = "Пароль обязателен для заполнения")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
                message = "Пароль должен содержать как минимум одну цифру, одну строчную и одну заглавную букву"
        )
        String password,

        @Schema(description = "Номер телефона", example = "+79991234567")
        @Size(max = 50, message = "Номер телефона не должен превышать 50 символов")
        String phone,

        @Schema(description = "Реферальный код пригласившего пользователя (опционально)", example = "REF-ABC1234")
        String referralCode
) {
}
