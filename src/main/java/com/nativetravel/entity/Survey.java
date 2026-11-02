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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA сущность результатов опроса предпочтений путешественника.
 */
@Entity
@Table(name = "surveys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "preferred_budget", length = 50)
    private String preferredBudget;

    @Column(name = "travel_type", length = 50)
    private String travelType;

    @Column(name = "preferred_season", length = 50)
    private String preferredSeason;

    @Column(name = "climate_preference", length = 50)
    private String climatePreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_tour_id")
    private Tour recommendedTour;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

