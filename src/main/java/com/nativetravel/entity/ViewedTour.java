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
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Сущность истории просмотров туров пользователем.
 */
@Data
@Entity
@Table(name = "viewed_tours")
public class ViewedTour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "viewed_at", nullable = false)
    private Instant viewedAt;

    public ViewedTour() {
    }

    @Builder
    public ViewedTour(Long id, User user, Tour tour, Instant viewedAt) {
        this.id = id;
        this.user = user;
        this.tour = tour;
        this.viewedAt = viewedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.viewedAt = Instant.now();
    }
}
