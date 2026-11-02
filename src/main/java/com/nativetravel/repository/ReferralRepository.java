package com.nativetravel.repository;

import com.nativetravel.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий реферальных связей.
 */
@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {

    /**
     * Поиск всех приглашенных пользователей конкретного реферера.
     */
    @Query("SELECT r FROM Referral r JOIN FETCH r.referred WHERE r.referrer.id = :referrerId ORDER BY r.createdAt DESC")
    List<Referral> findByReferrerIdWithReferred(@Param("referrerId") Long referrerId);

    /**
     * Поиск реферальной записи по приглашенному пользователю.
     */
    Optional<Referral> findByReferredId(Long referredId);

    /**
     * Подсчет общего количества приглашенных друзей.
     */
    long countByReferrerId(Long referrerId);
}
