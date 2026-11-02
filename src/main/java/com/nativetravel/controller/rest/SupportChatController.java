package com.nativetravel.controller.rest;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;
import com.nativetravel.service.SupportChatService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST контроллер службы поддержки клиентов (сообщения и история сессий).
 */
@Tag(name = "Support - Служба поддержки", description = "REST API онлайн-чата поддержки, истории сообщений и эскалации")
@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportChatController {

    private final SupportChatService supportChatService;

    @Operation(summary = "Отправить сообщение в чат поддержки", description = "Отправляет сообщение боту/оператору и возвращает сгенерированный ответ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сообщение успешно обработано", content = @Content(schema = @Schema(implementation = SupportMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/messages")
    public ResponseEntity<SupportMessageDto> sendMessage(
            @Valid @RequestBody SupportMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        SupportMessageDto response = supportChatService.processMessage(request, email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получить историю сообщений сессии", description = "Возвращает хронологический список сообщений для указанной сессии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "История получена"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/messages")
    public ResponseEntity<List<SupportMessageDto>> getSessionMessages(@RequestParam String sessionId) {
        return ResponseEntity.ok(supportChatService.getSessionHistory(sessionId));
    }

    @Operation(summary = "Запросить перевод на живого оператора", description = "Эскалирует диалог на оператора поддержки")
    @PostMapping("/escalate")
    public ResponseEntity<SupportMessageDto> escalate(@RequestParam String sessionId) {
        return ResponseEntity.ok(supportChatService.escalateToOperator(sessionId));
    }
}

