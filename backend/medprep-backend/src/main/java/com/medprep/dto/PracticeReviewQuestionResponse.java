package com.medprep.dto;

import java.util.List;

public class PracticeReviewQuestionResponse {

    private final Long sessionQuestionId;
    private final Integer displayOrder;
    private final Long questionId;
    private final String questionText;

    private final String explanation;

    private final Long selectedOptionId;
    private final String selectedOptionLabel;
    private final String selectedOptionText;

    private final Long correctOptionId;
    private final String correctOptionLabel;
    private final String correctOptionText;

    private final Boolean correct;
    private final Integer timeTakenSeconds;

    private final List<QuestionOptionResponse> options;

    public PracticeReviewQuestionResponse(
            Long sessionQuestionId,
            Integer displayOrder,
            Long questionId,
            String questionText,
            String explanation,
            Long selectedOptionId,
            String selectedOptionLabel,
            String selectedOptionText,
            Long correctOptionId,
            String correctOptionLabel,
            String correctOptionText,
            Boolean correct,
            Integer timeTakenSeconds,
            List<QuestionOptionResponse> options) {

        this.sessionQuestionId = sessionQuestionId;
        this.displayOrder = displayOrder;
        this.questionId = questionId;
        this.questionText = questionText;
        this.explanation = explanation;
        this.selectedOptionId = selectedOptionId;
        this.selectedOptionLabel = selectedOptionLabel;
        this.selectedOptionText = selectedOptionText;
        this.correctOptionId = correctOptionId;
        this.correctOptionLabel = correctOptionLabel;
        this.correctOptionText = correctOptionText;
        this.correct = correct;
        this.timeTakenSeconds = timeTakenSeconds;
        this.options = options;
    }

    public Long getSessionQuestionId() {
        return sessionQuestionId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public String getSelectedOptionLabel() {
        return selectedOptionLabel;
    }

    public String getSelectedOptionText() {
        return selectedOptionText;
    }

    public Long getCorrectOptionId() {
        return correctOptionId;
    }

    public String getCorrectOptionLabel() {
        return correctOptionLabel;
    }

    public String getCorrectOptionText() {
        return correctOptionText;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public List<QuestionOptionResponse> getOptions() {
        return options;
    }
}