package com.nativetravel.service;

import com.nativetravel.dto.SubscriptionDto;
import com.nativetravel.dto.SubscriptionRequest;
import com.nativetravel.entity.HotOfferSubscription;
import com.nativetravel.entity.Tour;
import com.nativetravel.repository.HotOfferSubscriptionRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование SubscriptionService (Phase 6)")
class SubscriptionServiceTest {

    @Mock
    private HotOfferSubscriptionRepository subscriptionRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    @DisplayName("Оформление новой email-подписки сохраняет запись")
    void subscribe_newEmail_createsSubscription() {
        SubscriptionRequest req = new SubscriptionRequest("traveler@example.com", null);

        when(subscriptionRepository.findByEmailIgnoreCase("traveler@example.com")).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(HotOfferSubscription.class))).thenAnswer(invocation -> {
            HotOfferSubscription sub = invocation.getArgument(0);
            sub.setId(10L);
            sub.setCreatedAt(Instant.now());
            return sub;
        });

        SubscriptionDto result = subscriptionService.subscribe(req);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("traveler@example.com");
        assertThat(result.active()).isTrue();
        verify(subscriptionRepository).save(any(HotOfferSubscription.class));
    }

    @Test
    @DisplayName("Повторная подписка на неактивный email реактивирует статус")
    void subscribe_inactiveEmail_reactivates() {
        SubscriptionRequest req = new SubscriptionRequest("traveler@example.com", null);
        HotOfferSubscription existing = HotOfferSubscription.builder()
                .id(5L)
                .email("traveler@example.com")
                .active(false)
                .build();

        when(subscriptionRepository.findByEmailIgnoreCase("traveler@example.com")).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(HotOfferSubscription.class))).thenReturn(existing);

        SubscriptionDto result = subscriptionService.subscribe(req);

        assertThat(result).isNotNull();
        assertThat(existing.isActive()).isTrue();
        verify(subscriptionRepository).save(existing);
    }

    @Test
    @DisplayName("Рассылка горящих предложений отправляет email всем активным подписчикам")
    void broadcastHotOffers_sendsEmailsToActiveSubscribers() {
        HotOfferSubscription s1 = HotOfferSubscription.builder().id(1L).email("sub1@example.com").active(true).build();
        HotOfferSubscription s2 = HotOfferSubscription.builder().id(2L).email("sub2@example.com").active(true).build();
        when(subscriptionRepository.findByActiveTrue()).thenReturn(List.of(s1, s2));

        Tour tour = Tour.builder().title("Тур в Дубай").country("ОАЭ").city("Дубай").price(BigDecimal.valueOf(80000)).durationDays(7).build();
        when(tourRepository.findByHotOfferTrue()).thenReturn(List.of(tour));

        int sent = subscriptionService.broadcastHotOffers();

        assertThat(sent).isEqualTo(2);
        verify(emailService).sendHotOffersNewsletter(eq("sub1@example.com"), contains("Тур в Дубай"));
        verify(emailService).sendHotOffersNewsletter(eq("sub2@example.com"), contains("Тур в Дубай"));
    }
}
