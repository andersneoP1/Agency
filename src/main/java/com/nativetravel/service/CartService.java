package com.nativetravel.service;

import com.nativetravel.dto.AddToCartRequest;
import com.nativetravel.dto.CartItemDto;
import com.nativetravel.dto.CartResponse;

/**
 * Сервис управления корзиной покупок пользователя.
 */
public interface CartService {

    /**
     * Получение текущего состава корзины пользователя с расчетом общей стоимости.
     *
     * @param userEmail email пользователя
     * @return состояние корзины
     */
    CartResponse getCart(String userEmail);

    /**
     * Добавление тура или авиабилета в корзину.
     *
     * @param request запрос добавления
     * @param userEmail email пользователя
     * @return DTO добавленного элемента
     */
    CartItemDto addItem(AddToCartRequest request, String userEmail);

    /**
     * Удаление позиции из корзины.
     *
     * @param cartItemId ID элемента корзины
     * @param userEmail email пользователя
     */
    void removeItem(Long cartItemId, String userEmail);

    /**
     * Полная очистка корзины пользователя.
     *
     * @param userEmail email пользователя
     */
    void clearCart(String userEmail);
}

