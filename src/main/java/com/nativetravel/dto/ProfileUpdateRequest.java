package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос обновления профиля пользователя.
 */
@Schema(description = "Запрос обновления личных данных профиля")
public record ProfileUpdateRequest(
        @Schema(description = "Имя", example = "Иван")
        @NotBlank(message = "Имя обязательно для заполнения")
        @Size(max = 100, message = "Имя не должно превышать 100 символов")
        String firstName,

        @Schema(description = "Фамилия", example = "Петров")
        @NotBlank(message = "Фамилия обязательна для заполнения")
        @Size(max = 100, message = "Фамилия не должна превышать 100 символов")
        String lastName,

        @Schema(description = "Номер телефона", example = "+7 (999) 111-22-33")
        String phone
) {
}

