package com.medprep.repository;

import com.medprep.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionIdOrderByDisplayOrderAsc(
            Long questionId);

    List<QuestionOption> findByQuestionIdAndCorrectTrue(
            Long questionId);
}