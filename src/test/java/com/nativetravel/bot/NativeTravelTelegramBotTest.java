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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NativeTravelTelegramBotTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotOfferSubscriptionRepository subscriptionRepository;

    @Mock
    private SupportChatService supportChatService;

    private NativeTravelTelegramBot bot;

    @BeforeEach
    void setUp() {
        bot = spy(new NativeTravelTelegramBot(
                "9255986103:AAExBRvhrYePSat2xM6E9RJXIIaBoWZurtE",
                "NativeTravelSuppport_bot",
                tourRepository,
                userRepository,
                subscriptionRepository,
                supportChatService
        ));
        lenient().doReturn(true).when(bot).sendTextMessage(anyLong(), anyString());
    }

    private Update createUpdate(long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);
        update.setMessage(message);
        return update;
    }

    @Test
    @DisplayName("getBotUsername and getBotToken return configured values")
    void shouldReturnBotUsernameAndToken() {
        assertThat(bot.getBotUsername()).isEqualTo("NativeTravelSuppport_bot");
        assertThat(bot.getBotToken()).isEqualTo("9255986103:AAExBRvhrYePSat2xM6E9RJXIIaBoWZurtE");
    }

    @Test
    @DisplayName("Process /start command")
    void shouldHandleStartCommand() {
        bot.onUpdateReceived(createUpdate(12345L, "/start"));
        verify(bot).sendTextMessage(eq(12345L), contains("Добро пожаловать"));
    }

    @Test
    @DisplayName("Process /help command")
    void shouldHandleHelpCommand() {
        bot.onUpdateReceived(createUpdate(12345L, "/help"));
        verify(bot).sendTextMessage(eq(12345L), contains("Справка"));
    }

    @Test
    @DisplayName("Process /tours command")
    void shouldHandleToursCommand() {
        Tour tour = Tour.builder()
                .id(1L)
                .title("Тур в Дубай")
                .country("ОАЭ")
                .city("Дубай")
                .price(BigDecimal.valueOf(85000))
                .durationDays(7)
                .hotOffer(true)
                .build();
        when(tourRepository.findByHotOfferTrue()).thenReturn(List.of(tour));

        bot.onUpdateReceived(createUpdate(12345L, "/tours"));
        verify(bot).sendTextMessage(eq(12345L), contains("Тур в Дубай"));
    }

    @Test
    @DisplayName("Process /link command with valid referral code")
    void shouldHandleLinkCommandSuccess() {
        User user = User.builder()
                .id(1L)
                .email("ivan@nativetravel.local")
                .referralCode("NT-IVAN123")
                .build();
        when(userRepository.findByReferralCode("NT-IVAN123")).thenReturn(Optional.of(user));

        bot.onUpdateReceived(createUpdate(999L, "/link NT-IVAN123"));

        assertThat(user.getTelegramChatId()).isEqualTo(999L);
        verify(userRepository).save(user);
        verify(bot).sendTextMessage(eq(999L), contains("успешно привязан"));
    }

    @Test
    @DisplayName("Process /subscribe command")
    void shouldHandleSubscribeCommand() {
        when(subscriptionRepository.findByTelegramChatId(999L)).thenReturn(Optional.empty());

        bot.onUpdateReceived(createUpdate(999L, "/subscribe"));

        verify(subscriptionRepository).save(any(HotOfferSubscription.class));
        verify(bot).sendTextMessage(eq(999L), contains("успешно подписались"));
    }

    @Test
    @DisplayName("Process conversational support message through SupportChatService")
    void shouldHandleSupportMessage() {
        when(userRepository.findByTelegramChatId(777L)).thenReturn(Optional.empty());
        SupportMessageDto dto = new SupportMessageDto(1L, "BOT", "Здравствуйте! Чем я могу помочь?", "sess-777", false, Instant.now());
        when(supportChatService.processMessage(any(SupportMessageRequest.class), isNull())).thenReturn(dto);

        bot.onUpdateReceived(createUpdate(777L, "Привет, как оплатить?"));

        verify(supportChatService).processMessage(argThat(r -> r.message().equals("Привет, как оплатить?")), isNull());
        verify(bot).sendTextMessage(eq(777L), eq("Здравствуйте! Чем я могу помочь?"));
    }
}
