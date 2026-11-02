package com.nativetravel.controller.rest;

import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.SubscriptionDto;
import com.nativetravel.dto.SubscriptionRequest;
import com.nativetravel.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер подписок на горящие предложения и запуск рассылок.
 */
@Tag(name = "Subscriptions - Рассылка горящих предложений", description = "Управление подпиской на горящие предложения и триггер рассылки")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Подписаться на горящие туры", description = "Регистрирует email или telegram ID в списке активных подписчиков")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Подписка успешно оформлена", content = @Content(schema = @Schema(implementation = SubscriptionDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный email или запрос", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/subscriptions/hot-offers")
    public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        SubscriptionDto dto = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Отписаться от рассылки", description = "Деактивирует подписку по указанному email")
    @PostMapping("/subscriptions/unsubscribe")
    public ResponseEntity<MessageResponse> unsubscribe(@RequestParam String email) {
        subscriptionService.unsubscribe(email);
        return ResponseEntity.ok(MessageResponse.of("Вы успешно отписаны от рассылки"));
    }

    @Operation(summary = "Запустить массовую рассылку горящих предложений", description = "Административный триггер для отправки рассылки горящих туров всем активным подписчикам")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/broadcast-hot-offers")
    public ResponseEntity<MessageResponse> broadcastHotOffers() {
        int count = subscriptionService.broadcastHotOffers();
        return ResponseEntity.ok(MessageResponse.of("Рассылка успешно отправлена. Получателей: " + count));
    }
}

