package com.nativetravel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Сущность избранного тура пользователя.
 */
@Data
@Entity
@Table(
        name = "favorite_tours",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_favorite", columnNames = {"user_id", "tour_id"})
)
public class FavoriteTour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public FavoriteTour() {
    }

    @Builder
    public FavoriteTour(Long id, User user, Tour tour, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.tour = tour;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
