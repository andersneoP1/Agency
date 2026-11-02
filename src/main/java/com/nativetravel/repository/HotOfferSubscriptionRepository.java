package com.nativetravel.repository;

import com.nativetravel.entity.HotOfferSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления подписками на горящие туры.
 */
@Repository
public interface HotOfferSubscriptionRepository extends JpaRepository<HotOfferSubscription, Long> {

    Optional<HotOfferSubscription> findByEmailIgnoreCase(String email);

    Optional<HotOfferSubscription> findByTelegramChatId(Long telegramChatId);

    List<HotOfferSubscription> findByActiveTrue();

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTelegramChatId(Long telegramChatId);
}

