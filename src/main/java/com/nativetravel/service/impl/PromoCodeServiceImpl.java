package com.nativetravel.service.impl;

import com.nativetravel.dto.PromoCodeApplyResponse;
import com.nativetravel.dto.PromoCodeValidateRequest;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Реализация сервиса управления промокодами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    @Override
    @Transactional(readOnly = true)
    public PromoCodeApplyResponse validateAndCalculate(PromoCodeValidateRequest request) {
        PromoCode promo = getValidPromoCode(request.code(), request.orderAmount());

        BigDecimal discountMultiplier = BigDecimal.valueOf(promo.getDiscountPercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal discountAmount = request.orderAmount().multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = request.orderAmount().subtract(discountAmount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return new PromoCodeApplyResponse(
                promo.getCode(),
                promo.getDiscountPercent(),
                discountAmount,
                finalAmount,
                "Промокод " + promo.getCode() + " успешно применен! Скидка " + promo.getDiscountPercent() + "%"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PromoCode getValidPromoCode(String code, BigDecimal orderAmount) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Промокод не может быть пустым");
        }

        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Промокод '" + code + "' не найден"));

        if (!Boolean.TRUE.equals(promo.getActive())) {
            throw new BadRequestException("Срок действия промокода '" + code + "' завершен или он деактивирован");
        }

        if (promo.getValidUntil() != null && Instant.now().isAfter(promo.getValidUntil())) {
            throw new BadRequestException("Срок действия промокода '" + code + "' истек");
        }

        if (promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new BadRequestException("Лимит использований промокода '" + code + "' исчерпан");
        }

        if (orderAmount != null && orderAmount.compareTo(promo.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Минимальная сумма заказа для применения промокода '" + code + "' составляет " 
                    + promo.getMinOrderAmount() + " ₽");
        }

        return promo;
    }

    @Override
    @Transactional
    public void incrementUses(PromoCode promoCode) {
        if (promoCode != null) {
            promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
            promoCodeRepository.save(promoCode);
            log.info("Incremented usage for promo code: {}, current: {}/{}",
                    promoCode.getCode(), promoCode.getCurrentUses(), promoCode.getMaxUses());
        }
    }
}
