package com.nativetravel.repository;

import com.nativetravel.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий управления промокодами.
 */
@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    /**
     * Поиск промокода по его кодовому слову (без учета регистра).
     */
    Optional<PromoCode> findByCodeIgnoreCase(String code);

    /**
     * Поиск активного промокода.
     */
    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(String code);
}
