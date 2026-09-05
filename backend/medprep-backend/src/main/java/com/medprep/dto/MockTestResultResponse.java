package com.medprep.dto;

import com.medprep.entity.PracticeSessionStatus;

import java.time.LocalDateTime;

public class MockTestResultResponse {

    private Long sessionId;

    private PracticeSessionStatus status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer correctAnswers;

    private Integer incorrectAnswers;

    private Integer unansweredQuestions;

    private Double score;

    private Double percentage;

    private Integer passingMarks;

    private boolean passed;

    private Boolean negativeMarking;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public MockTestResultResponse() {
    }

    public MockTestResultResponse(
            Long sessionId,
            PracticeSessionStatus status,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            Integer incorrectAnswers,
            Integer unansweredQuestions,
            Double score,
            Double percentage,
            Integer passingMarks,
            boolean passed,
            Boolean negativeMarking,
            LocalDateTime startedAt,
            LocalDateTime completedAt) {

        this.sessionId = sessionId;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.score = score;
        this.percentage = percentage;
        this.passingMarks = passingMarks;
        this.passed = passed;
        this.negativeMarking = negativeMarking;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public PracticeSessionStatus getStatus() {
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

    public Integer getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public Integer getUnansweredQuestions() {
        return unansweredQuestions;
    }

    public Double getScore() {
        return score;
    }

    public Double getPercentage() {
        return percentage;
    }

    public Integer getPassingMarks() {
        return passingMarks;
    }

    public boolean isPassed() {
        return passed;
    }

    public Boolean getNegativeMarking() {
        return negativeMarking;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}