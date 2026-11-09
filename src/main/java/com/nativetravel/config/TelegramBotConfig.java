package com.nativetravel.config;

import com.nativetravel.bot.NativeTravelTelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Spring configuration for Telegram Bot integration.
 * Initializes TelegramBotsApi and registers NativeTravelTelegramBot for long polling.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.telegram", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(NativeTravelTelegramBot bot) throws TelegramApiException {
        log.info("Initializing TelegramBotsApi with DefaultBotSession for @{}...", bot.getBotUsername());
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        log.info("Telegram bot @{} successfully registered with TelegramBotsApi and long polling started!", bot.getBotUsername());
        return botsApi;
    }
}

