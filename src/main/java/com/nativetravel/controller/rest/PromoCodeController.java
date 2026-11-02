package com.nativetravel.controller.rest;

import com.nativetravel.dto.PromoCodeApplyResponse;
import com.nativetravel.dto.PromoCodeValidateRequest;
import com.nativetravel.service.PromoCodeService;
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

/**
 * REST контроллер валидации промокодов и расчета скидок.
 */
@Tag(name = "PromoCodes - Промокоды", description = "Проверка действия промокодов и расчет скидки для бронирования")
@RestController
@RequestMapping("/api/v1/promocodes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @Operation(summary = "Проверить промокод и рассчитать скидку", description = "Проверяет срок действия, лимит использований и минимальную сумму заказа")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Промокод валиден", content = @Content(schema = @Schema(implementation = PromoCodeApplyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Промокод недействителен, истек или сумма недостаточна", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Промокод не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/validate")
    public ResponseEntity<PromoCodeApplyResponse> validatePromoCode(
            @Valid @RequestBody PromoCodeValidateRequest request
    ) {
        return ResponseEntity.ok(promoCodeService.validateAndCalculate(request));
    }
}

