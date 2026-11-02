package com.nativetravel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

/**
 * Сущность системной роли пользователя.
 */
@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private RoleType name;

    public Role() {
    }

    public Role(Long id, RoleType name) {
        this.id = id;
        this.name = name;
    }

    @Builder
    public Role(RoleType name) {
        this.name = name;
    }
}
