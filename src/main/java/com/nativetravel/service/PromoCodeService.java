package com.nativetravel.service;

import com.nativetravel.dto.PromoCodeApplyResponse;
import com.nativetravel.dto.PromoCodeValidateRequest;
import com.nativetravel.entity.PromoCode;

import java.math.BigDecimal;

/**
 * Сервис валидации, применения и учета промокодов.
 */
public interface PromoCodeService {

    /**
     * Валидация промокода и расчет суммы скидки.
     *
     * @param request запрос с кодом и суммой заказа
     * @return результат расчета скидки
     */
    PromoCodeApplyResponse validateAndCalculate(PromoCodeValidateRequest request);

    /**
     * Получение сущности валидного промокода для оформления заказа.
     *
     * @param code кодовое слово
     * @param orderAmount сумма заказа
     * @return сущность промокода
     */
    PromoCode getValidPromoCode(String code, BigDecimal orderAmount);

    /**
     * Инкремент счетчика использований промокода.
     *
     * @param promoCode сущность промокода
     */
    void incrementUses(PromoCode promoCode);
}

