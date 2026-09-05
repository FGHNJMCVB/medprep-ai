package com.medprep.repository;

import com.medprep.entity.Attempt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttemptRepository
        extends JpaRepository<Attempt, Long> {

    List<Attempt> findByUserIdOrderByAttemptedAtDesc(
            Long userId
    );

    long countByUserId(
            Long userId
    );

    long countByUserIdAndCorrectTrue(
            Long userId
    );

    Optional<Attempt>
    findTopByUserIdAndQuestionIdOrderByAttemptedAtDesc(
            Long userId,
            Long questionId
    );

    Optional<Attempt>
    findTopByUserIdAndQuestionIdAndAttemptedAtBetweenOrderByAttemptedAtDesc(
            Long userId,
            Long questionId,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    );
}