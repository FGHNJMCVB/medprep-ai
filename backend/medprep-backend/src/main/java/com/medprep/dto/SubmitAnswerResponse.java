package com.medprep.dto;

public class SubmitAnswerResponse {

    private Long attemptId;
    private Boolean correct;

    private Long selectedOptionId;
    private String selectedOptionLabel;

    private Long correctOptionId;
    private String correctOptionLabel;

    private String explanation;

    public SubmitAnswerResponse() {
    }

    public SubmitAnswerResponse(
            Long attemptId,
            Boolean correct,
            Long selectedOptionId,
            String selectedOptionLabel,
            Long correctOptionId,
            String correctOptionLabel,
            String explanation) {

        this.attemptId = attemptId;
        this.correct = correct;
        this.selectedOptionId = selectedOptionId;
        this.selectedOptionLabel = selectedOptionLabel;
        this.correctOptionId = correctOptionId;
        this.correctOptionLabel = correctOptionLabel;
        this.explanation = explanation;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public String getSelectedOptionLabel() {
        return selectedOptionLabel;
    }

    public Long getCorrectOptionId() {
        return correctOptionId;
    }

    public String getCorrectOptionLabel() {
        return correctOptionLabel;
    }

    public String getExplanation() {
        return explanation;
    }
}