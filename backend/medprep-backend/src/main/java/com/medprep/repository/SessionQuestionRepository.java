package com.medprep.repository;

import com.medprep.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionQuestionRepository
        extends JpaRepository<SessionQuestion, Long> {

    List<SessionQuestion> findBySessionIdOrderByDisplayOrderAsc(
            Long sessionId
    );

    Optional<SessionQuestion> findBySessionIdAndQuestionId(
            Long sessionId,
            Long questionId
    );

    long countBySessionIdAndAnsweredTrue(Long sessionId);

    long countBySessionIdAndCorrectTrue(Long sessionId);
}