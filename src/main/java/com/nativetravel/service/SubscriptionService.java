package com.nativetravel.service;

import com.nativetravel.dto.SubscriptionDto;
import com.nativetravel.dto.SubscriptionRequest;

/**
 * Сервис подписки на горящие предложения и массовой рассылки.
 */
public interface SubscriptionService {

    /**
     * Оформить подписку на рассылку горящих предложений.
     *
     * @param request запрос с email или telegram chat id
     * @return DTO созданной или активной подписки
     */
    SubscriptionDto subscribe(SubscriptionRequest request);

    /**
     * Отписаться от рассылки по email.
     *
     * @param email адрес электронной почты
     */
    void unsubscribe(String email);

    /**
     * Запустить рассылку горящих туров всем активным подписчикам (admin trigger).
     *
     * @return количество отправленных уведомлений
     */
    int broadcastHotOffers();
}
