package com.nativetravel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO статистики для дашборда администратора.
 */
@Schema(description = "Статистика дашборда администратора")
public record DashboardStatsDto(
        @Schema(description = "Общая выручка от оплаченных заказов", example = "1250000.00")
        BigDecimal totalRevenue,

        @Schema(description = "Всего оформлено бронирований", example = "48")
        long totalBookings,

        @Schema(description = "Оплаченных бронирований", example = "35")
        long paidBookings,

        @Schema(description = "Всего зарегистрированных пользователей", example = "120")
        long totalUsers,

        @Schema(description = "Активных туров в каталоге", example = "10")
        long totalTours,

        @Schema(description = "Отзывов на модерации", example = "2")
        long pendingReviews,

        @Schema(description = "Распределение статусов бронирований: PAID, PENDING, CANCELLED")
        Map<String, Long> statusDistribution,

        @Schema(description = "Выручка по месяцам для графика Chart.js")
        Map<String, BigDecimal> monthlyRevenue
) {
}
