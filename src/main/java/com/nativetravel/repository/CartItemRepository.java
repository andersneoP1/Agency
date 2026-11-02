package com.nativetravel.repository;

import com.nativetravel.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий корзины покупок пользователя.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Получение всех элементов корзины пользователя с предзагрузкой туров и рейсов.
     */
    @Query("SELECT c FROM CartItem c " +
           "LEFT JOIN FETCH c.tour t " +
           "LEFT JOIN FETCH t.hotel " +
           "LEFT JOIN FETCH c.flight " +
           "WHERE c.user.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<CartItem> findByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * Поиск элемента корзины по пользователю и туру.
     */
    Optional<CartItem> findByUserIdAndTourId(Long userId, Long tourId);

    /**
     * Поиск элемента корзины по пользователю и рейсу.
     */
    Optional<CartItem> findByUserIdAndFlightId(Long userId, Long flightId);

    /**
     * Очистка всей корзины пользователя.
     */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
