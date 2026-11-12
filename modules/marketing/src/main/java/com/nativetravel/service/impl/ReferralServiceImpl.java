package com.nativetravel.service.impl;

import com.nativetravel.dto.ReferralDto;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.entity.Referral;
import com.nativetravel.entity.User;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.repository.ReferralRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса реферальной программы.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralServiceImpl implements ReferralService {

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String baseUrl;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void registerReferral(String referralCode, User newUser) {
        if (referralCode == null || referralCode.trim().isEmpty() || newUser == null) {
            return;
        }

        Optional<User> referrerOpt = userRepository.findByReferralCode(referralCode.trim().toUpperCase());
        if (referrerOpt.isEmpty()) {
            log.info("Referral code {} not found, skipping referral binding", referralCode);
            return;
        }

        User referrer = referrerOpt.get();
        if (referrer.getId().equals(newUser.getId())) {
            log.warn("User {} tried to refer themselves, ignored", newUser.getEmail());
            return;
        }

        if (referralRepository.findByReferredId(newUser.getId()).isPresent()) {
            return;
        }

        Referral referral = Referral.builder()
                .referrer(referrer)
                .referred(newUser)
                .rewardGranted(false)
                .build();

        referralRepository.save(referral);
        log.info("Registered referral: referrer {} -> referred {}", referrer.getEmail(), newUser.getEmail());
    }

    @Override
    @Transactional
    public void checkAndRewardReferrer(User buyer) {
        if (buyer == null) return;

        Optional<Referral> referralOpt = referralRepository.findByReferredId(buyer.getId());
        if (referralOpt.isEmpty()) {
            return;
        }

        Referral referral = referralOpt.get();
        if (Boolean.TRUE.equals(referral.getRewardGranted())) {
            return; // Already rewarded
        }

        // Grant reward: Create 10% promo code for the referrer
        String promoCodeStr = "REF-" + (100000 + random.nextInt(900000));
        PromoCode promoCode = PromoCode.builder()
                .code(promoCodeStr)
                .discountPercent(10)
                .minOrderAmount(BigDecimal.valueOf(5000))
                .maxUses(1)
                .currentUses(0)
                .validUntil(Instant.now().plus(90, ChronoUnit.DAYS))
                .active(true)
                .build();

        promoCodeRepository.save(promoCode);

        referral.setRewardGranted(true);
        referralRepository.save(referral);

        log.info("Rewarded referrer {} with 10% promo code {} after first purchase of friend {}",
                referral.getReferrer().getEmail(), promoCodeStr, buyer.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralDto getReferralInfo(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        List<Referral> referrals = referralRepository.findByReferrerIdWithReferred(user.getId());

        List<String> friendNames = referrals.stream()
                .map(r -> r.getReferred().getFullName())
                .toList();

        int rewardsCount = (int) referrals.stream()
                .filter(r -> Boolean.TRUE.equals(r.getRewardGranted()))
                .count();

        String link = baseUrl + "/register?ref=" + user.getReferralCode();

        return new ReferralDto(
                user.getReferralCode(),
                link,
                referrals.size(),
                friendNames,
                rewardsCount
        );
    }
}

