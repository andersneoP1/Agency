package com.nativetravel.service.impl;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;
import com.nativetravel.entity.SupportMessage;
import com.nativetravel.entity.User;
import com.nativetravel.repository.SupportMessageRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.SupportChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Реализация сервиса чата поддержки с правилами сопоставления ключевых слов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupportChatServiceImpl implements SupportChatService {

    private final SupportMessageRepository supportMessageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SupportMessageDto processMessage(SupportMessageRequest request, String userEmail) {
        User user = null;
        if (userEmail != null && !userEmail.isBlank()) {
            user = userRepository.findByEmailIgnoreCase(userEmail).orElse(null);
        }

        // 1. Save USER message
        SupportMessage userMsg = SupportMessage.builder()
                .sessionId(request.sessionId())
                .user(user)
                .senderType("USER")
                .message(request.message().trim())
                .escalated(false)
                .build();
        supportMessageRepository.save(userMsg);
        log.debug("Saved user message in session {}", request.sessionId());

        // 2. Rule-based keyword engine (prioritized: operator -> payment -> discounts -> flights -> tours -> cancellation -> greeting)
        String text = request.message().toLowerCase(Locale.ROOT);
        boolean escalate = false;
        String botReply;

        if (containsAny(text, "оператор", "человек", "менеджер", "живой", "позови", "специалист", "консультант", "соедини", "свяжи")) {
            escalate = true;
            botReply = "Я переключил ваш диалог на живого специалиста службы заботы о клиентах Native Travel. Оператор подключится в течение 2 минут!";
        } else if (containsAny(text, "оплат", "карт", "сбп", "крипт", "деньг", "рубл", "usdt", "терминал")) {
            botReply = "Мы принимаем оплату банковскими картами, через СБП, криптовалютой (USDT) и наличными. Сразу после оплаты формируется электронный ваучер с QR-кодом.";
        } else if (containsAny(text, "промокод", "скидк", "бонус", "акци", "купон")) {
            botReply = "Для получения скидки 10% на первое бронирование используйте промокод WELCOME10 на шаге оплаты!";
        } else if (containsAny(text, "билет", "рейс", "авиа", "самолет", "перелет")) {
            botReply = "Авиабилеты регулярных и чартерных рейсов доступны в специальном разделе Авиабилеты (/flights).";
        } else if (containsAny(text, "тур", "турци", "египет", "дубай", "бали", "мальдив", "каталог", "поездк", "отдых")) {
            botReply = "В нашем каталоге представлено множество эксклюзивных туров по выгодным ценам! Вы можете ознакомиться с ними в разделе Туры (/tours).";
        } else if (containsAny(text, "возврат", "отмен", "перенос")) {
            botReply = "Условия отмены зависят от тарифа тура. Вы можете управлять бронированием в личном кабинете (/profile) или связаться с оператором, написав «оператор».";
        } else if (containsAny(text, "привет", "здравствуй", "добрый", "хай", "hello")) {
            botReply = "Здравствуйте! Рад приветствовать вас в Native Travel. Чем я могу вам помочь?";
        } else {
            botReply = "Спасибо за ваш вопрос! Я автоматический ассистент. Если вы хотите пообщаться с живым оператором, просто напишите «оператор». Также ответы на частые вопросы есть в разделе /faq.";
        }

        // 3. Save BOT message
        SupportMessage botMsg = SupportMessage.builder()
                .sessionId(request.sessionId())
                .user(null)
                .senderType("BOT")
                .message(botReply)
                .escalated(escalate)
                .build();
        SupportMessage savedBotMsg = supportMessageRepository.save(botMsg);
        log.info("Support bot replied in session {}. Escalated: {}", request.sessionId(), escalate);

        return toDto(savedBotMsg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageDto> getSessionHistory(String sessionId) {
        return supportMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SupportMessageDto escalateToOperator(String sessionId) {
        SupportMessage escalationMsg = SupportMessage.builder()
                .sessionId(sessionId)
                .senderType("BOT")
                .message("Ваше обращение эскалировано на старшего специалиста поддержки. Ожидайте ответа в чате.")
                .escalated(true)
                .build();
        SupportMessage saved = supportMessageRepository.save(escalationMsg);
        log.info("Session {} manually escalated to operator", sessionId);
        return toDto(saved);
    }

    @Override
    @Transactional
    public SupportMessageDto recordOperatorBusy(String sessionId) {
        SupportMessage busyMsg = SupportMessage.builder()
                .sessionId(sessionId)
                .senderType("BOT")
                .message("Все операторы в данный момент заняты, попробуйте позднее...")
                .escalated(true)
                .build();
        SupportMessage saved = supportMessageRepository.save(busyMsg);
        log.info("Session {} marked as operator busy", sessionId);
        return toDto(saved);
    }

    private boolean containsAny(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private SupportMessageDto toDto(SupportMessage msg) {
        return new SupportMessageDto(
                msg.getId(),
                msg.getSenderType(),
                msg.getMessage(),
                msg.getSessionId(),
                msg.isEscalated(),
                msg.getCreatedAt()
        );
    }
}
