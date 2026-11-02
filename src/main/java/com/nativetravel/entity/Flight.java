package com.nativetravel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Сущность авиарейса.
 */
@Data
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", length = 50, nullable = false)
    private String flightNumber;

    @Column(name = "airline", length = 100, nullable = false)
    private String airline;

    @Column(name = "departure_city", length = 100, nullable = false)
    private String departureCity;

    @Column(name = "arrival_city", length = 100, nullable = false)
    private String arrivalCity;

    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats = 50;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Flight() {
    }

    @Builder
    public Flight(Long id, String flightNumber, String airline, String departureCity,
                  String arrivalCity, Instant departureTime, Instant arrivalTime,
                  BigDecimal price, int availableSeats, Instant createdAt) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

