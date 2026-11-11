package com.nativetravel.bot;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;
import com.nativetravel.entity.HotOfferSubscription;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.repository.HotOfferSubscriptionRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.SupportChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

/**
 * Telegram-бот турагентства Native Travel.
 * Обрабатывает команды /start, /help, /tours, /faq, /link &lt;code&gt;, /subscribe, /unsubscribe
 * и входящие вопросы службы поддержки через SupportChatService.
 */
@Component
@ConditionalOnProperty(prefix = "app.telegram", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class NativeTravelTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final HotOfferSubscriptionRepository subscriptionRepository;
    private final SupportChatService supportChatService;

    public NativeTravelTelegramBot(
            @Value("${app.telegram.bot-token:mock-token}") String botToken,
            @Value("${app.telegram.bot-username:NativeTravelSuppport_bot}") String botUsername,
            TourRepository tourRepository,
            UserRepository userRepository,
            HotOfferSubscriptionRepository subscriptionRepository,
            SupportChatService supportChatService) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.supportChatService = supportChatService;
        log.info("Initialized NativeTravelTelegramBot with username @{}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        log.debug("Received Telegram message from chatId {}: {}", chatId, messageText);

        if (messageText.startsWith("/start")) {
            sendResponse(chatId, getStartMessage());
        } else if (messageText.startsWith("/help")) {
            sendResponse(chatId, getHelpMessage());
        } else if (messageText.startsWith("/tours")) {
            sendResponse(chatId, getHotToursMessage());
        } else if (messageText.equalsIgnoreCase("FAQ") || messageText.equalsIgnoreCase("/faq")) {
            sendResponse(chatId, getFaqMessage());
        } else if (messageText.startsWith("/subscribe")) {
            handleSubscribeCommand(chatId);
        } else if (messageText.startsWith("/unsubscribe")) {
            handleUnsubscribeCommand(chatId);
        } else if (messageText.startsWith("/link")) {
            handleLinkCommand(chatId, messageText);
        } else {
            handleSupportMessage(chatId, messageText);
        }
    }

    private void handleLinkCommand(long chatId, String messageText) {
        String[] parts = messageText.split("\\s+");
        if (parts.length < 2) {
            sendResponse(chatId, "⚠️ Пожалуйста, укажите ваш реферальный код из личного кабинета.\nПример: /link NT-IVAN123");
            return;
        }

        String referralCode = parts[1].trim();
        Optional<User> userOpt = userRepository.findByReferralCode(referralCode);
        if (userOpt.isEmpty()) {
            sendResponse(chatId, "❌ Пользователь с кодом " + referralCode + " не найден в системе Native Travel.");
            return;
        }

        User user = userOpt.get();
        user.setTelegramChatId(chatId);
        userRepository.save(user);

        sendResponse(chatId, "✅ Ваш Telegram-аккаунт успешно привязан к профилю " + user.getEmail() + "!\nТеперь вы будете получать важные уведомления о бронированиях.");
    }

    private void handleSubscribeCommand(long chatId) {
        Optional<HotOfferSubscription> existing = subscriptionRepository.findByTelegramChatId(chatId);
        if (existing.isPresent() && existing.get().isActive()) {
            sendResponse(chatId, "🔔 Вы уже подписаны на уведомления о горящих турах!");
            return;
        }
        HotOfferSubscription sub = existing.orElseGet(HotOfferSubscription::new);
        sub.setTelegramChatId(chatId);
        sub.setActive(true);
        subscriptionRepository.save(sub);
        sendResponse(chatId, "🎉 Вы успешно подписались на горящие предложения Native Travel в Telegram!\nМы будем присылать самые выгодные туры.");
    }

    private void handleUnsubscribeCommand(long chatId) {
        Optional<HotOfferSubscription> existing = subscriptionRepository.findByTelegramChatId(chatId);
        if (existing.isPresent() && existing.get().isActive()) {
            HotOfferSubscription sub = existing.get();
            sub.setActive(false);
            subscriptionRepository.save(sub);
            sendResponse(chatId, "🔕 Вы отписались от уведомлений о горящих турах.");
        } else {
            sendResponse(chatId, "Вы не были подписаны на уведомления.");
        }
    }

    private void handleSupportMessage(long chatId, String messageText) {
        try {
            Optional<User> userOpt = userRepository.findByTelegramChatId(chatId);
            String email = userOpt.map(User::getEmail).orElse(null);
            SupportMessageRequest req = new SupportMessageRequest("tg-" + chatId, messageText);
            SupportMessageDto reply = supportChatService.processMessage(req, email);
            sendResponse(chatId, reply.message());
        } catch (Exception e) {
            log.warn("Failed to process conversational support for tg chat {}: {}", chatId, e.getMessage());
            sendResponse(chatId, "Я на связи! Если у вас вопрос по бронированию, турам или оплате — напишите его подробнее, или используйте /help.");
        }
    }

    private String getStartMessage() {
        return "👋 Добро пожаловать в официальный бот турагентства Native Travel (@" + botUsername + ")!\n\n"
                + "Здесь вы можете узнавать о горящих турах, получать консультацию поддержки и привязать свой аккаунт.\n\n"
                + "Доступные команды:\n"
                + "✈️ /tours — топ горящих предложений недели\n"
                + "❓ FAQ — ответы на частые вопросы\n"
                + "🔔 /subscribe — подписаться на горящие туры\n"
                + "🔗 /link <код> — привязать аккаунт сайта (реф. код из профиля)\n"
                + "ℹ️ /help — справка и контакты службы поддержки\n\n"
                + "💬 Вы также можете просто написать свой вопрос в этот чат, и бот поддержки ответит вам!";
    }

    private String getHelpMessage() {
        return "ℹ️ Справка Native Travel Bot (@" + botUsername + "):\n\n"
                + "• /tours — топ горящих предложений\n"
                + "• FAQ — информация о бронировании и оплате\n"
                + "• /subscribe — подписаться на горящие туры\n"
                + "• /link <код> — привязка Telegram к профилю на сайте\n\n"
                + "📞 Горячая линия поддержки: 8 (800) 555-35-35\n"
                + "🌐 Наш сайт: http://localhost:8080\n\n"
                + "💬 Чтобы задать вопрос — просто напишите его в этот чат!";
    }

    private String getHotToursMessage() {
        List<Tour> hotTours = tourRepository.findByHotOfferTrue();
        if (hotTours.isEmpty()) {
            return "На данный момент все горящие туры забронированы. Проверьте каталог позже!";
        }

        StringBuilder sb = new StringBuilder("🔥 ТОП горящих предложений недели:\n\n");
        int count = 0;
        for (Tour tour : hotTours) {
            if (count++ >= 3) break;
            sb.append("🏝️ ").append(tour.getTitle()).append("\n")
                    .append("📍 ").append(tour.getCountry()).append(", ").append(tour.getCity()).append("\n")
                    .append("💰 Цена: от ").append(tour.getPrice()).append(" ₽ (").append(tour.getDurationDays()).append(" дн.)\n\n");
        }
        sb.append("Для бронирования перейдите на наш сайт!");
        return sb.toString();
    }

    private String getFaqMessage() {
        return "❓ Частые вопросы:\n\n"
                + "1. Как оплатить тур? Картой, СБП, USDT или наличными.\n"
                + "2. Где получить ваучер? Он сразу приходит на email и доступен в личном кабинете с QR-кодом.\n"
                + "3. Как получить скидку? Используйте промокод WELCOME10 на 10% скидки при первой покупке!";
    }

    public boolean sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId {}: {}", chatId, e.getMessage());
            return false;
        }
    }

    private void sendResponse(long chatId, String text) {
        sendTextMessage(chatId, text);
    }
}
