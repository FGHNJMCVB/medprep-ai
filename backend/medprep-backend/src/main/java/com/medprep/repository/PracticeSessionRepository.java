package com.medprep.repository;

import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticeSessionRepository
        extends JpaRepository<PracticeSession, Long> {

    List<PracticeSession> findByUserIdOrderByStartedAtDesc(
            Long userId
    );

    List<PracticeSession> findByUserIdAndStatusOrderByStartedAtDesc(
            Long userId,
            PracticeSessionStatus status
    );
    List<PracticeSession> findByUserIdAndTypeAndStatusOrderByCompletedAtDesc(
        Long userId,
        PracticeSessionType type,
        PracticeSessionStatus status
);
}