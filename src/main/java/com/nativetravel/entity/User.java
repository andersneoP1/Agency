package com.nativetravel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя системы.
 */
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "referral_code", length = 50, nullable = false, unique = true)
    private String referralCode;

    @Column(name = "telegram_chat_id", unique = true)
    private Long telegramChatId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public User() {
    }

    @Builder
    public User(Long id, String email, String passwordHash, String firstName, String lastName,
                String phone, String avatarUrl, String referralCode, Long telegramChatId,
                boolean enabled, Set<Role> roles, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.referralCode = referralCode;
        this.telegramChatId = telegramChatId;
        this.enabled = enabled;
        this.roles = roles != null ? roles : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
