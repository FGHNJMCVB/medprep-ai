package com.medprep.dto;

public class PracticeSessionResultResponse {

    private Long sessionId;

    private String status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer correctAnswers;

    private Integer wrongAnswers;

    private Integer unansweredQuestions;

    private Double accuracy;

    public PracticeSessionResultResponse() {
    }

    public PracticeSessionResultResponse(
            Long sessionId,
            String status,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            Integer wrongAnswers,
            Integer unansweredQuestions,
            Double accuracy) {

        this.sessionId = sessionId;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.accuracy = accuracy;
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
}