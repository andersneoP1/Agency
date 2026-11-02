package com.nativetravel.repository;

import com.nativetravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления учетными записями пользователей.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по email.
     *
     * @param email адрес электронной почты
     * @return Optional с найденным пользователем
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Проверка существования пользователя с данным email.
     *
     * @param email адрес электронной почты
     * @return true, если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Поиск пользователя по реферальному коду.
     *
     * @param referralCode реферальный код
     * @return Optional с найденным пользователем
     */
    Optional<User> findByReferralCode(String referralCode);

    /**
     * Проверка существования пользователя с данным реферальным кодом.
     *
     * @param referralCode реферальный код
     * @return true, если код уже занят
     */
    boolean existsByReferralCode(String referralCode);

    /**
     * Поиск пользователя по идентификатору чата Telegram.
     *
     * @param telegramChatId ID чата Telegram
     * @return Optional с найденным пользователем
     */
    Optional<User> findByTelegramChatId(Long telegramChatId);
}
