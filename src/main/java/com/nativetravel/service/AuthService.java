package com.nativetravel.service;

import com.nativetravel.dto.AuthResponse;
import com.nativetravel.dto.LoginRequest;
import com.nativetravel.dto.PasswordRecoveryRequest;
import com.nativetravel.dto.PasswordResetConfirmRequest;
import com.nativetravel.dto.RegisterRequest;

/**
 * Сервис аутентификации, регистрации и восстановления доступа.
 */
public interface AuthService {

    /**
     * Регистрация нового пользователя.
     *
     * @param request данные регистрации
     * @return AuthResponse с JWT access-токеном и данными пользователя
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Аутентификация пользователя по email и паролю.
     *
     * @param request данные для входа
     * @return AuthResponse с JWT access-токеном и данными пользователя
     */
    AuthResponse login(LoginRequest request);

    /**
     * Обновление access-токена по валидному refresh-токену.
     *
     * @param refreshToken refresh JWT-токен
     * @return AuthResponse с новым access-токеном
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Запрос на восстановление пароля (отправка 6-значного кода на email).
     *
     * @param request запрос с email адресом
     */
    void requestPasswordRecovery(PasswordRecoveryRequest request);

    /**
     * Подтверждение смены пароля по коду.
     *
     * @param request запрос с email, 6-значным кодом и новым паролем
     */
    void confirmPasswordReset(PasswordResetConfirmRequest request);

    /**
     * Генерация refresh-токена для пользователя.
     *
     * @param email адрес электронной почты
     * @return строка JWT refresh-токена
     */
    String generateRefreshToken(String email);
}
