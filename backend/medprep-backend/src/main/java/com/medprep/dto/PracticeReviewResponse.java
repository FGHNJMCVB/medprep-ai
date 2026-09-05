package com.medprep.dto;

import java.util.List;

public class PracticeReviewResponse {

    private final Long sessionId;
    private final String status;

    private final Integer totalQuestions;
    private final Integer answeredQuestions;
    private final Integer correctAnswers;
    private final Integer wrongAnswers;
    private final Integer unansweredQuestions;

    private final Double accuracy;

    private final List<PracticeReviewQuestionResponse> questions;

    public PracticeReviewResponse(
            Long sessionId,
            String status,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            Integer wrongAnswers,
            Integer unansweredQuestions,
            Double accuracy,
            List<PracticeReviewQuestionResponse> questions) {

        this.sessionId = sessionId;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.accuracy = accuracy;
        this.questions = questions;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Integer getAnsweredQuestions() {
        return answeredQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public Integer getWrongAnswers() {
        return wrongAnswers;
    }

    public Integer getUnansweredQuestions() {
        return unansweredQuestions;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public List<PracticeReviewQuestionResponse> getQuestions() {
        return questions;
    }
}