package com.nativetravel.controller.rest;

import com.nativetravel.dto.PaymentRequest;
import com.nativetravel.dto.PaymentResponse;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST контроллер платежных операций (заглушка СБП, карты, криптовалюта, наличные).
 */
@Tag(name = "Payments - Оплата", description = "Проведение оплаты бронирований, генерация QR-кода и подтверждений")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Провести оплату бронирования", description = "Эмулирует обработку платежа, переводит бронь в PAID, генерирует QR и PDF ваучер")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Оплата успешно проведена", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный статус бронирования или данные карты", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Бронирование не найдено", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация для оплаты");
        }
        PaymentResponse response = paymentService.processPayment(request, principal.getName());
        return ResponseEntity.ok(response);
    }
}

