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
 * Сущность временного кода подтверждения для восстановления пароля.
 */
@Data
@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PasswordResetCode() {
    }

    @Builder
    public PasswordResetCode(Long id, User user, String code, Instant expiresAt, boolean used, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.code = code;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Проверяет, истек ли срок действия кода.
     *
     * @return true, если код просрочен
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }
}
