package com.medprep.repository;

import com.medprep.entity.Question;
import com.medprep.entity.QuestionDifficulty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findByTopicIdAndActiveTrueOrderByIdAsc(
            Long topicId
    );

    List<Question> findBySubjectIdAndActiveTrueOrderByIdAsc(
            Long subjectId
    );

    List<Question> findByTopicIdAndDifficultyAndActiveTrueOrderByIdAsc(
            Long topicId,
            QuestionDifficulty difficulty
    );

    List<Question> findBySubjectIdAndDifficultyAndActiveTrueOrderByIdAsc(
            Long subjectId,
            QuestionDifficulty difficulty
    );

    List<Question> findByActiveTrue();

    long countByDifficultyAndActiveTrue(
            QuestionDifficulty difficulty
    );

    boolean existsByQuestionTextIgnoreCase(
            String questionText
    );
}