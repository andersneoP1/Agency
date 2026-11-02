package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;

/**
 * DTO представления данных пользователя.
 */
@Schema(description = "Профиль пользователя")
public record UserDto(
        @Schema(description = "Идентификатор пользователя", example = "1")
        Long id,

        @Schema(description = "Email адрес", example = "alex@example.com")
        String email,

        @Schema(description = "Имя", example = "Алексей")
        String firstName,

        @Schema(description = "Фамилия", example = "Смирнов")
        String lastName,

        @Schema(description = "Номер телефона", example = "+79991234567")
        String phone,

        @Schema(description = "URL аватара", example = "/uploads/avatar.png")
        String avatarUrl,

        @Schema(description = "Персональный реферальный код", example = "REF-A1B2C3D4")
        String referralCode,

        @Schema(description = "Набор ролей пользователя", example = "[\"ROLE_USER\"]")
        Set<String> roles,

        @Schema(description = "Статус активности аккаунта", example = "true")
        boolean enabled,

        @Schema(description = "Дата и время регистрации")
        Instant createdAt
) {
}
