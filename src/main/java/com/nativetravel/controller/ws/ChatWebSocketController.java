package com.nativetravel.controller.ws;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;
import com.nativetravel.service.SupportChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket контроллер для интерактивного обмена сообщениями в чате поддержки (STOMP).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SupportChatService supportChatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Обработка входящего сообщения пользователя через WebSocket.
     * Отправляет ответ бота в персональный топик сессии: /topic/messages/{sessionId}
     */
    @MessageMapping("/chat.send")
    public void handleIncomingMessage(@Payload SupportMessageRequest request, Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        log.debug("WebSocket incoming message from session: {}, user: {}", request.sessionId(), username);

        SupportMessageDto botResponse = supportChatService.processMessage(request, username);

        // Отправка ответа в персональный канал сессии
        messagingTemplate.convertAndSend("/topic/messages/" + request.sessionId(), botResponse);

        // Если произошла эскалация, также уведомляем операторов в общий канал
        if (botResponse.escalated()) {
            messagingTemplate.convertAndSend("/topic/support/escalations", botResponse);
        }
    }
}

