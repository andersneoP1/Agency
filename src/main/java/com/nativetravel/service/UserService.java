package com.nativetravel.service;

import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.User;

/**
 * Сервис управления пользователями и их профилями.
 */
public interface UserService {

    /**
     * Поиск пользователя по email.
     *
     * @param email адрес электронной почты
     * @return найденная сущность пользователя
     */
    User findByEmail(String email);

    /**
     * Поиск пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденная сущность пользователя
     */
    User findById(Long id);

    /**
     * Получение DTO текущего аутентифицированного пользователя.
     *
     * @return данные текущего пользователя
     */
    UserDto getCurrentUserDto();

    /**
     * Получение сущности текущего аутентифицированного пользователя.
     *
     * @return сущность текущего пользователя
     */
    User getCurrentUserEntity();

    /**
     * Генерация уникального реферального кода.
     *
     * @return уникальная строка реферального кода
     */
    String generateUniqueReferralCode();
}
