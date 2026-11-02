package com.nativetravel.repository;

import com.nativetravel.entity.PasswordResetCode;
import com.nativetravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления кодами подтверждения сброса пароля.
 */
@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /**
     * Поиск активного неиспользованного кода для конкретного пользователя.
     *
     * @param user пользователь
     * @param code 6-значный код
     * @return Optional с объектом кода
     */
    Optional<PasswordResetCode> findByUserAndCodeAndUsedFalse(User user, String code);

    /**
     * Поиск последнего созданного кода для пользователя.
     *
     * @param user пользователь
     * @return Optional с последним кодом
     */
    Optional<PasswordResetCode> findFirstByUserOrderByCreatedAtDesc(User user);
}
