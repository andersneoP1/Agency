package com.nativetravel.repository;

import com.nativetravel.entity.Role;
import com.nativetravel.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления ролями пользователей.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Поиск роли по её перечислению.
     *
     * @param name имя роли
     * @return Optional с найденной ролью
     */
    Optional<Role> findByName(RoleType name);
}
