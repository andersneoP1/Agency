package com.nativetravel.controller.rest;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.DashboardStatsDto;
import com.nativetravel.dto.HotelCreateRequest;
import com.nativetravel.dto.HotelDto;
import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.PromoCodeCreateRequest;
import com.nativetravel.dto.PromoCodeDto;
import com.nativetravel.dto.ReviewDto;
import com.nativetravel.dto.TourCreateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.BookingStatus;
import com.nativetravel.service.AdminService;
import com.nativetravel.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST контроллер панели администратора (требуется роль ROLE_ADMIN).
 */
@Tag(name = "Admin - Панель управления", description = "Управление турами, отелями, пользователями, отзывами, промокодами и бронированиями")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ReviewService reviewService;

    @Operation(summary = "Получить аналитику дашборда", description = "Возвращает сводные метрики и данные для графиков выручки")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аналитика получена", content = @Content(schema = @Schema(implementation = DashboardStatsDto.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (не администратор)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // --- TOURS CRUD ---
    @Operation(summary = "Создать новый тур")
    @PostMapping("/tours")
    public ResponseEntity<TourDto> createTour(@Valid @RequestBody TourCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTour(request));
    }

    @Operation(summary = "Редактировать тур")
    @PutMapping("/tours/{id}")
    public ResponseEntity<TourDto> updateTour(@PathVariable Long id, @Valid @RequestBody TourCreateRequest request) {
        return ResponseEntity.ok(adminService.updateTour(id, request));
    }

    @Operation(summary = "Удалить тур")
    @DeleteMapping("/tours/{id}")
    public ResponseEntity<MessageResponse> deleteTour(@PathVariable Long id) {
        adminService.deleteTour(id);
        return ResponseEntity.ok(MessageResponse.of("Тур успешно удален"));
    }

    // --- HOTELS CRUD ---
    @Operation(summary = "Список всех отелей")
    @GetMapping("/hotels")
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(adminService.getAllHotels());
    }

    @Operation(summary = "Создать отель")
    @PostMapping("/hotels")
    public ResponseEntity<HotelDto> createHotel(@Valid @RequestBody HotelCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createHotel(request));
    }

    @Operation(summary = "Редактировать отель")
    @PutMapping("/hotels/{id}")
    public ResponseEntity<HotelDto> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelCreateRequest request) {
        return ResponseEntity.ok(adminService.updateHotel(id, request));
    }

    @Operation(summary = "Удалить отель")
    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<MessageResponse> deleteHotel(@PathVariable Long id) {
        adminService.deleteHotel(id);
        return ResponseEntity.ok(MessageResponse.of("Отель успешно удален"));
    }

    // --- USERS MANAGEMENT ---
    @Operation(summary = "Список всех пользователей")
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @Operation(summary = "Переключить статус блокировки пользователя")
    @PostMapping("/users/{id}/toggle-status")
    public ResponseEntity<MessageResponse> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(MessageResponse.of("Статус пользователя изменен"));
    }

    // --- REVIEWS MODERATION ---
    @Operation(summary = "Список отзывов на модерации")
    @GetMapping("/reviews/pending")
    public ResponseEntity<Page<ReviewDto>> getPendingReviews(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getPendingReviews(pageable));
    }

    @Operation(summary = "Одобрить отзыв")
    @PostMapping("/reviews/{id}/approve")
    public ResponseEntity<MessageResponse> approveReview(@PathVariable Long id) {
        reviewService.approveReview(id);
        return ResponseEntity.ok(MessageResponse.of("Отзыв одобрен и опубликован"));
    }

    @Operation(summary = "Отклонить/удалить отзыв")
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<MessageResponse> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(MessageResponse.of("Отзыв удален"));
    }

    // --- PROMO CODES ---
    @Operation(summary = "Список промокодов")
    @GetMapping("/promocodes")
    public ResponseEntity<Page<PromoCodeDto>> getAllPromoCodes(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllPromoCodes(pageable));
    }

    @Operation(summary = "Создать промокод")
    @PostMapping("/promocodes")
    public ResponseEntity<PromoCodeDto> createPromoCode(@Valid @RequestBody PromoCodeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createPromoCode(request));
    }

    @Operation(summary = "Переключить активность промокода")
    @PostMapping("/promocodes/{id}/toggle")
    public ResponseEntity<MessageResponse> togglePromoCode(@PathVariable Long id) {
        adminService.togglePromoCode(id);
        return ResponseEntity.ok(MessageResponse.of("Активность промокода изменена"));
    }

    // --- BOOKINGS ---
    @Operation(summary = "Список всех клиентских бронирований")
    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingDto>> getAllBookings(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllBookings(pageable));
    }

    @Operation(summary = "Изменить статус бронирования")
    @PostMapping("/bookings/{id}/status")
    public ResponseEntity<MessageResponse> updateBookingStatus(
            @PathVariable Long id,
            @Parameter(description = "Новый статус: PAID, PENDING, CANCELLED", example = "PAID")
            @RequestParam BookingStatus status
    ) {
        adminService.updateBookingStatus(id, status);
        return ResponseEntity.ok(MessageResponse.of("Статус бронирования обновлен на " + status));
    }
}
