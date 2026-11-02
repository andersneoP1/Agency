package com.nativetravel.controller.rest;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.BookingRequest;
import com.nativetravel.dto.MessageResponse;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.BookingService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST контроллер бронирований, истории заказов и ваучеров.
 */
@Tag(name = "Bookings - Бронирования", description = "Создание бронирований, просмотр истории заказов, скачивание ваучеров")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Создать новое бронирование", description = "Создает бронирование со статусом PENDING с применением скидки по промокоду")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Бронирование создано", content = @Content(schema = @Schema(implementation = BookingDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры или исчерпаны места", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody BookingRequest request,
            Principal principal
    ) {
        validateAuth(principal);
        BookingDto booking = bookingService.createBooking(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @Operation(summary = "Получить список бронирований пользователя", description = "Возвращает историю заказов с пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История бронирований успешно получена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Page<BookingDto>> getUserBookings(
            @PageableDefault(size = 10) Pageable pageable,
            Principal principal
    ) {
        validateAuth(principal);
        return ResponseEntity.ok(bookingService.getUserBookings(principal.getName(), pageable));
    }

    @Operation(summary = "Получить детальную информацию о бронировании", description = "Возвращает детали бронирования по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Бронирование найдено", content = @Content(schema = @Schema(implementation = BookingDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(
            @Parameter(description = "ID бронирования", example = "1") @PathVariable Long id,
            Principal principal
    ) {
        validateAuth(principal);
        return ResponseEntity.ok(bookingService.getBookingById(id, principal.getName()));
    }

    @Operation(summary = "Скачать PDF ваучер бронирования", description = "Генерирует и отдает файл туристического ваучера в формате PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF ваучер успешно сформирован"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}/voucher")
    public ResponseEntity<byte[]> downloadVoucher(
            @Parameter(description = "ID бронирования", example = "1") @PathVariable Long id,
            Principal principal
    ) {
        validateAuth(principal);
        byte[] pdf = bookingService.downloadVoucherPdf(id, principal.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"voucher-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Отменить бронирование", description = "Отменяет бронирование и освобождает места")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Бронирование успешно отменено", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<MessageResponse> cancelBooking(
            @Parameter(description = "ID бронирования", example = "1") @PathVariable Long id,
            Principal principal
    ) {
        validateAuth(principal);
        bookingService.cancelBooking(id, principal.getName());
        return ResponseEntity.ok(MessageResponse.of("Бронирование успешно отменено"));
    }

    private void validateAuth(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
    }
}
