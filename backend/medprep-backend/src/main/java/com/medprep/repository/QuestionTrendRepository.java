package com.medprep.repository;

import com.medprep.entity.QuestionTrend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionTrendRepository
        extends JpaRepository<QuestionTrend, Long> {

    List<QuestionTrend> findBySubjectIdOrderByTrendScoreDesc(
            Long subjectId
    );

    List<QuestionTrend> findByTopicIdOrderByTrendScoreDesc(
            Long topicId
    );

    List<QuestionTrend> findAllByOrderByTrendScoreDesc();

    Optional<QuestionTrend> findBySubjectIdAndTopicIdAndConceptTag(
            Long subjectId,
            Long topicId,
            String conceptTag
    );
}