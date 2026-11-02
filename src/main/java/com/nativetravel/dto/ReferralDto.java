package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO представления реферальной программы пользователя.
 */
@Schema(description = "Информация о реферальной программе пользователя")
public record ReferralDto(
        @Schema(description = "Реферальный промокод пользователя", example = "NT-IVAN123")
        String referralCode,

        @Schema(description = "Персональная ссылка для приглашения друзей", example = "https://nativetravel.local/register?ref=NT-IVAN123")
        String referralLink,

        @Schema(description = "Количество успешно приглашенных друзей", example = "3")
        int totalInvited,

        @Schema(description = "Список имен приглашенных друзей")
        List<String> invitedFriends,

        @Schema(description = "Количество полученных бонусных промокодов", example = "1")
        int earnedRewardsCount
) {
}
