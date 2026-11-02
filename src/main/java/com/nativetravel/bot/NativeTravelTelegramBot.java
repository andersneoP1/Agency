package com.nativetravel.bot;

import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.repository.HotOfferSubscriptionRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
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
 * Обрабатывает команды /start, /help, /tours, /faq, /link <code>.
 */
@Component
@ConditionalOnProperty(prefix = "app.telegram", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class NativeTravelTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final HotOfferSubscriptionRepository subscriptionRepository;

    public NativeTravelTelegramBot(
            @Value("${app.telegram.bot-token:mock-token}") String botToken,
            @Value("${app.telegram.bot-username:NativeTravelSupportBot}") String botUsername,
            TourRepository tourRepository,
            UserRepository userRepository,
            HotOfferSubscriptionRepository subscriptionRepository) {
        super(botToken);
        this.botUsername = botUsername;
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        log.info("Initialized NativeTravelTelegramBot with username @{}", botUsername);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
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
        } else if (messageText.startsWith("/faq")) {
            sendResponse(chatId, getFaqMessage());
        } else if (messageText.startsWith("/link")) {
            handleLinkCommand(chatId, messageText);
        } else {
            sendResponse(chatId, "Команда не распознана. Введите /help для просмотра списка доступных команд.");
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

    private String getStartMessage() {
        return "👋 Добро пожаловать в официальный бот турагентства Native Travel!\n\n"
                + "Здесь вы можете узнавать о горящих турах, получать ответы на частые вопросы и привязать свой аккаунт.\n\n"
                + "Доступные команды:\n"
                + "✈️ /tours — горящие предложения недели\n"
                + "❓ /faq — ответы на частые вопросы\n"
                + "🔗 /link <code> — привязать аккаунт сайта по реферальному коду\n"
                + "ℹ️ /help — справка и контакты службы поддержки";
    }

    private String getHelpMessage() {
        return "ℹ️ Справка Native Travel Bot:\n\n"
                + "• /tours — топ горящих предложений\n"
                + "• /faq — информация о бронировании и оплате\n"
                + "• /link <код> — привязка Telegram к профилю на сайте\n\n"
                + "📞 Горячая линия поддержки: 8 (800) 555-35-35\n"
                + "🌐 Наш сайт: https://nativetravel.local";
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
        sb.append("Для полного бронирования перейдите на сайт.");
        return sb.toString();
    }

    private String getFaqMessage() {
        return "❓ Частые вопросы:\n\n"
                + "1. Как оплатить тур? Картой, СБП, USDT или наличными.\n"
                + "2. Где получить ваучер? Он сразу приходит на email и доступен в личном кабинете с QR-кодом.\n"
                + "3. Как получить скидку? Используйте промокод WELCOME10 на 10% скидки при первой покупке!";
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to chatId {}: {}", chatId, e.getMessage());
        }
    }
}
