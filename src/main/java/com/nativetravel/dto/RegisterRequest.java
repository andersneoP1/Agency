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
        @jakarta.validation.constraints.Pattern(regexp = "(?:\\+7[ ()-]*[346789](?:[ ()-]*[0-9]){9}|\\+375(?:[ ()-]*[0-9]){9}|\\+380[ ()-]*(?:[ ()-]*[0-9]){9}|\\+373[ ()-]*(?:[ ()-]*[0-9]){8}|\\+374[ ()-]*(?:[ ()-]*[0-9]){8}|\\+994[ ()-]*(?:[ ()-]*[0-9]){9}|\\+995[ ()-]*(?:[ ()-]*[0-9]){9}|\\+996[ ()-]*(?:[ ()-]*[0-9]){9}|\\+992[ ()-]*(?:[ ()-]*[0-9]){9}|\\+993[ ()-]*(?:[ ()-]*[0-9]){8}|\\+998[ ()-]*(?:[ ()-]*[0-9]){9})?", message = "Invalid international phone number")
        String phone,

        @Schema(description = "Реферальный код пригласившего пользователя (опционально)", example = "REF-ABC1234")
        String referralCode
) {
    public RegisterRequest {
        if (phone != null) {
            phone = phone.strip();
        }
    }
}
