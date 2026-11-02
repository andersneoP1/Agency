package com.nativetravel.repository;

import com.nativetravel.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для хранения и поиска сообщений службы поддержки.
 */
@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<SupportMessage> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<SupportMessage> findByEscalatedTrueOrderByCreatedAtDesc();
}
