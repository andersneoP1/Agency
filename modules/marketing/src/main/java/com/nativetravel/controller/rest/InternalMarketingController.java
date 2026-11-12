package com.nativetravel.controller.rest;

import com.nativetravel.distributed.InternalRequests.*;
import com.nativetravel.dto.*;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Hidden
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class InternalMarketingController {
    private final PromoCodeService promos;
    private final ReferralService referrals;
    private final PromoCodeRepository promoRepository;
    private final UserRepository users;

    @PostMapping("/promos/lookup")
    public PromoSnapshot lookup(@Valid @RequestBody Promo request) {
        var promo = promos.getValidPromoCode(request.code(), request.amount());
        return new PromoSnapshot(promo.getId(), promo.getCode(), promo.getDiscountPercent());
    }
    @PostMapping("/promos/validate")
    public PromoCodeApplyResponse validate(@Valid @RequestBody PromoCodeValidateRequest request) {
        return promos.validateAndCalculate(request);
    }
    @PostMapping("/promos/use")
    public void use(@Valid @RequestBody Id request) {
        promos.incrementUses(promoRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Promo not found")));
    }
    @PostMapping("/referrals/info")
    public ReferralDto info(@Valid @RequestBody EmailLookup request) {
        return referrals.getReferralInfo(request.email());
    }
    @PostMapping("/referrals/register")
    public void register(@Valid @RequestBody Referral request) {
        referrals.registerReferral(request.code(), users.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }
    @PostMapping("/referrals/reward")
    public void reward(@Valid @RequestBody Id request) {
        referrals.checkAndRewardReferrer(users.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }
}
