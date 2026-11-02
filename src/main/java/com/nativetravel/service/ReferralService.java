package com.nativetravel.service;

import com.nativetravel.dto.ReferralDto;
import com.nativetravel.entity.User;

/**
 * Сервис реферальной программы и начисления бонусов за приглашенных друзей.
 */
public interface ReferralService {

    /**
     * Регистрация реферальной связи при регистрации нового пользователя по промокоду.
     *
     * @param referralCode реферальный код пригласившего
     * @param newUser новый зарегистрированный пользователь
     */
    void registerReferral(String referralCode, User newUser);

    /**
     * Проверка и начисление промокода рефереру при первой оплате приглашенного друга.
     *
     * @param buyer покупатель, совершивший оплату
     */
    void checkAndRewardReferrer(User buyer);

    /**
     * Получение информации о реферальной программе для личного кабинета.
     *
     * @param userEmail email пользователя
     * @return статистика и реферальная ссылка
     */
    ReferralDto getReferralInfo(String userEmail);
}
