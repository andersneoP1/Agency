package com.nativetravel.repository;

import com.nativetravel.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий опросников предпочтений путешественников.
 */
@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * Получение последнего заполненного опроса пользователя с предзагрузкой рекомендованного тура.
     */
    @Query("SELECT s FROM Survey s LEFT JOIN FETCH s.recommendedTour t LEFT JOIN FETCH t.hotel WHERE s.user.id = :userId ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Survey> findLatestByUserId(@Param("userId") Long userId);
}
