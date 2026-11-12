package com.nativetravel.service.impl;

import com.nativetravel.dto.SubscriptionDto;
import com.nativetravel.dto.SubscriptionRequest;
import com.nativetravel.entity.HotOfferSubscription;
import com.nativetravel.entity.Tour;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.repository.HotOfferSubscriptionRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.service.EmailService;
import com.nativetravel.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса подписок на горящие предложения и email/telegram рассылки.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final HotOfferSubscriptionRepository subscriptionRepository;
    private final TourRepository tourRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public SubscriptionDto subscribe(SubscriptionRequest request) {
        String email = request.email() != null ? request.email().trim().toLowerCase() : null;
        Long chatId = request.telegramChatId();

        if ((email == null || email.isBlank()) && chatId == null) {
            throw new BadRequestException("Необходимо указать email или telegram chat id для подписки");
        }

        if (email != null && !email.isBlank()) {
            Optional<HotOfferSubscription> existing = subscriptionRepository.findByEmailIgnoreCase(email);
            HotOfferSubscription target;
            if (existing.isPresent()) {
                target = existing.get();
                if (!target.isActive()) {
                    target.setActive(true);
                    target = subscriptionRepository.save(target);
                    log.info("Reactivated hot offer subscription for email: {}", email);
                }
            } else {
                HotOfferSubscription newSub = HotOfferSubscription.builder()
                        .email(email)
                        .telegramChatId(chatId)
                        .active(true)
                        .build();
                target = subscriptionRepository.save(newSub);
                log.info("Created new hot offer subscription for email: {}", email);
            }

            sendSubscriptionWelcomeEmail(email);
            return toDto(target);
        }

        // Subscription via telegramChatId only
        Optional<HotOfferSubscription> existingTg = subscriptionRepository.findByTelegramChatId(chatId);
        if (existingTg.isPresent()) {
            HotOfferSubscription sub = existingTg.get();
            if (!sub.isActive()) {
                sub.setActive(true);
                subscriptionRepository.save(sub);
            }
            return toDto(sub);
        }

        HotOfferSubscription newSub = HotOfferSubscription.builder()
                .telegramChatId(chatId)
                .active(true)
                .build();
        HotOfferSubscription saved = subscriptionRepository.save(newSub);
        log.info("Created new hot offer subscription for Telegram chat: {}", chatId);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void unsubscribe(String email) {
        if (email != null && !email.isBlank()) {
            subscriptionRepository.findByEmailIgnoreCase(email.trim()).ifPresent(sub -> {
                sub.setActive(false);
                subscriptionRepository.save(sub);
                log.info("Unsubscribed email: {}", email);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int broadcastHotOffers() {
        List<HotOfferSubscription> activeSubs = subscriptionRepository.findByActiveTrue();
        if (activeSubs.isEmpty()) {
            log.info("No active subscribers for hot offers broadcast");
            return 0;
        }

        List<Tour> hotTours = tourRepository.findByHotOfferTrue();
        if (hotTours.isEmpty()) {
            log.info("No hot tours available to broadcast");
            return 0;
        }

        StringBuilder content = new StringBuilder();
        for (Tour tour : hotTours) {
            content.append("• ").append(tour.getTitle())
                    .append(" (").append(tour.getCountry()).append(", ").append(tour.getCity()).append(")")
                    .append(" — от ").append(tour.getPrice()).append(" ₽ (длительность: ")
                    .append(tour.getDurationDays()).append(" дн.)\n");
        }

        String broadcastBody = content.toString();
        int sentCount = 0;

        for (HotOfferSubscription sub : activeSubs) {
            if (sub.getEmail() != null && !sub.getEmail().isBlank()) {
                emailService.sendHotOffersNewsletter(sub.getEmail(), broadcastBody);
                sentCount++;
            }
        }

        log.info("Successfully broadcast hot offers to {} subscribers", sentCount);
        return sentCount;
    }

    private void sendSubscriptionWelcomeEmail(String email) {
        try {
            List<Tour> hotTours = tourRepository.findByHotOfferTrue();
            StringBuilder content = new StringBuilder();
            if (hotTours != null && !hotTours.isEmpty()) {
                for (Tour tour : hotTours) {
                    content.append("• ").append(tour.getTitle())
                            .append(" (").append(tour.getCountry()).append(", ").append(tour.getCity()).append(")")
                            .append(" — от ").append(tour.getPrice()).append(" ₽ (длительность: ")
                            .append(tour.getDurationDays()).append(" дн.)\n");
                }
            } else {
                content.append("В ближайшее время мы пришлем вам самые горячие скидки на новые направления!\n");
            }
            emailService.sendHotOffersNewsletter(email, content.toString());
        } catch (Exception e) {
            log.error("Failed to send welcome hot offers newsletter to {}: {}", email, e.getMessage());
        }
    }

    private SubscriptionDto toDto(HotOfferSubscription sub) {
        return new SubscriptionDto(
                sub.getId(),
                sub.getEmail(),
                sub.getTelegramChatId(),
                sub.isActive(),
                sub.getCreatedAt()
        );
    }
}

