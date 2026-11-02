package com.nativetravel.service;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;
import com.nativetravel.entity.SupportMessage;
import com.nativetravel.repository.SupportMessageRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.SupportChatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование SupportChatService (Phase 6)")
class SupportChatServiceTest {

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportChatServiceImpl chatService;

    @Test
    @DisplayName("Бот распознает вопрос о турах и предлагает перейти в каталог")
    void processMessage_tourKeyword_repliesWithCatalogInfo() {
        SupportMessageRequest request = new SupportMessageRequest("sess-1", "Подскажите, какие туры в Турцию есть?");

        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage msg = invocation.getArgument(0);
            msg.setId(10L);
            msg.setCreatedAt(Instant.now());
            return msg;
        });

        SupportMessageDto response = chatService.processMessage(request, null);

        assertThat(response).isNotNull();
        assertThat(response.senderType()).isEqualTo("BOT");
        assertThat(response.message()).contains("/tours");
        assertThat(response.escalated()).isFalse();
        verify(supportMessageRepository, times(2)).save(any(SupportMessage.class));
    }

    @Test
    @DisplayName("Бот распознает запрос живого человека и эскалирует на оператора")
    void processMessage_operatorKeyword_escalates() {
        SupportMessageRequest request = new SupportMessageRequest("sess-2", "Мне нужен живой оператор для консультации");

        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage msg = invocation.getArgument(0);
            msg.setId(11L);
            msg.setCreatedAt(Instant.now());
            return msg;
        });

        SupportMessageDto response = chatService.processMessage(request, null);

        assertThat(response).isNotNull();
        assertThat(response.escalated()).isTrue();
        assertThat(response.message()).contains("специалиста");
    }

    @Test
    @DisplayName("Получение истории сообщений сессии возвращает хронологический список")
    void getSessionHistory_returnsOrderedMessages() {
        SupportMessage m1 = SupportMessage.builder().id(1L).sessionId("sess-3").senderType("USER").message("Привет").createdAt(Instant.now()).build();
        SupportMessage m2 = SupportMessage.builder().id(2L).sessionId("sess-3").senderType("BOT").message("Здравствуйте!").createdAt(Instant.now()).build();

        when(supportMessageRepository.findBySessionIdOrderByCreatedAtAsc("sess-3")).thenReturn(List.of(m1, m2));

        List<SupportMessageDto> history = chatService.getSessionHistory("sess-3");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).message()).isEqualTo("Привет");
        assertThat(history.get(1).senderType()).isEqualTo("BOT");
    }

    @Test
    @DisplayName("Ручная эскалация обращается к репозиторию и возвращает системное сообщение")
    void escalateToOperator_savesEscalationMessage() {
        when(supportMessageRepository.save(any(SupportMessage.class))).thenAnswer(invocation -> {
            SupportMessage msg = invocation.getArgument(0);
            msg.setId(12L);
            msg.setCreatedAt(Instant.now());
            return msg;
        });

        SupportMessageDto result = chatService.escalateToOperator("sess-4");

        assertThat(result).isNotNull();
        assertThat(result.escalated()).isTrue();
        assertThat(result.message()).contains("эскалировано");
    }
}

