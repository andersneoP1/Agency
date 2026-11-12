package com.nativetravel.controller.rest;

import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.SubscriptionDto;
import com.nativetravel.dto.SubscriptionRequest;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * REST контроллер подписок на горящие предложения и запуск рассылок.
 */
@Tag(name = "Subscriptions - Рассылка горящих предложений", description = "Управление подпиской на горящие предложения и триггер рассылки")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;

    @Operation(summary = "Подписаться на горящие туры (JSON)", description = "Регистрирует email или telegram ID в списке активных подписчиков")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Подписка успешно оформлена", content = @Content(schema = @Schema(implementation = SubscriptionDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный email или запрос", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/subscriptions/hot-offers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        SubscriptionDto dto = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Подписаться на горящие туры (Форма)", description = "Регистрирует email через HTML-форму или HTMX")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Подписка успешно оформлена"),
            @ApiResponse(responseCode = "400", description = "Некорректный email или запрос")
    })
    @PostMapping(value = "/subscriptions/hot-offers", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> subscribeForm(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long telegramChatId,
            @RequestHeader(value = "HX-Request", defaultValue = "false") boolean isHtmx,
            @RequestHeader(value = "Accept", defaultValue = "") String acceptHeader,
            Locale locale) {

        boolean wantsHtml = isHtmx || acceptHeader.contains(MediaType.TEXT_HTML_VALUE);

        if ((email == null || email.isBlank() || !email.contains("@")) && telegramChatId == null) {
            if (wantsHtml) {
                String errorMsg = messageSource.getMessage("footer.subscribe.invalid_email", null,
                        "Пожалуйста, введите корректный email", locale != null ? locale : Locale.getDefault());
                String html = """
                    <div class="alert alert-danger py-2 px-3 small mb-0 d-flex align-items-center gap-2 text-white border-0 shadow-sm" style="background: rgba(220, 53, 69, 0.9); border-radius: 6px;">
                        <i class="bi bi-exclamation-circle-fill text-warning"></i>
                        <span>%s</span>
                    </div>
                    """.formatted(errorMsg);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                        .body(html);
            }
            throw new BadRequestException("Необходимо указать корректный email или telegram chat id для подписки");
        }

        SubscriptionRequest request = new SubscriptionRequest(email != null ? email.trim() : null, telegramChatId);
        SubscriptionDto dto = subscriptionService.subscribe(request);

        if (wantsHtml) {
            String successMsg = messageSource.getMessage("footer.subscribe.success", null,
                    "Вы успешно подписаны на горящие туры!", locale != null ? locale : Locale.getDefault());
            String html = """
                <div class="alert alert-success py-2 px-3 small mb-0 d-flex align-items-center gap-2 text-white border-0 shadow-sm animate__animated animate__fadeIn" style="background: rgba(25, 135, 84, 0.9); border-radius: 6px;">
                    <i class="bi bi-check-circle-fill text-warning"></i>
                    <span>%s</span>
                </div>
                """.formatted(successMsg);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.valueOf("text/html;charset=UTF-8"))
                    .body(html);
        }

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

