package com.medprep.service;

import com.medprep.dto.QuestionOptionResponse;
import com.medprep.dto.QuestionResponse;
import com.medprep.entity.Question;
import com.medprep.repository.QuestionOptionRepository;
import com.medprep.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;

    public QuestionService(QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
    }

    public List<QuestionResponse> getQuestionsByTopic(Long topicId) {

        return questionRepository
                .findByTopicIdAndActiveTrueOrderByIdAsc(topicId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuestionResponse> getQuestionsBySubject(Long subjectId) {

        return questionRepository
                .findBySubjectIdAndActiveTrueOrderByIdAsc(subjectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private QuestionResponse toResponse(Question question) {

        List<QuestionOptionResponse> options = questionOptionRepository
                .findByQuestionIdOrderByDisplayOrderAsc(question.getId())
                .stream()
                .map(option -> new QuestionOptionResponse(
                        option.getId(),
                        option.getOptionLabel(),
                        option.getOptionText(),
                        option.getDisplayOrder()))
                .toList();

        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getExplanation(),
                question.getDifficulty(),
                question.getQuestionType(),
                question.getConceptTag(),
                question.getSourceYear(),
                question.getSourceSession(),
                question.getSourceReference(),
                question.getHighYield(),
                question.getClinicalCase(),
                question.getImageBased(),
                question.getSubject().getId(),
                question.getTopic().getId(),
                options);
    }
}