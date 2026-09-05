package com.medprep.dto;

public class MockTestAnswerResponse {

    private Long sessionId;
    private Long sessionQuestionId;
    private Long questionId;
    private Long selectedOptionId;
    private boolean correct;
    private int answeredQuestions;
    private int correctAnswers;
    private int totalQuestions;

    public MockTestAnswerResponse() {
    }

    public MockTestAnswerResponse(
            Long sessionId,
            Long sessionQuestionId,
            Long questionId,
            Long selectedOptionId,
            boolean correct,
            int answeredQuestions,
            int correctAnswers,
            int totalQuestions) {

        this.sessionId = sessionId;
        this.sessionQuestionId = sessionQuestionId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.correct = correct;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getSessionQuestionId() {
        return sessionQuestionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }
}