package com.medprep.dto;

import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;

import java.time.LocalDateTime;

public class MockTestHistoryResponse {

    private Long sessionId;

    private PracticeSessionStatus status;

    private PracticeSessionType type;

    private Long subjectId;

    private String subjectName;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer correctAnswers;

    private Integer incorrectAnswers;

    private Integer unansweredQuestions;

    private Double score;

    private Double percentage;

    private boolean passed;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public MockTestHistoryResponse() {
    }

    public MockTestHistoryResponse(
            Long sessionId,
            PracticeSessionStatus status,
            PracticeSessionType type,
            Long subjectId,
            String subjectName,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            Integer incorrectAnswers,
            Integer unansweredQuestions,
            Double score,
            Double percentage,
            boolean passed,
            LocalDateTime startedAt,
            LocalDateTime completedAt) {

        this.sessionId = sessionId;
        this.status = status;
        this.type = type;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.score = score;
        this.percentage = percentage;
        this.passed = passed;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public PracticeSessionStatus getStatus() {
        return status;
    }

    public PracticeSessionType getType() {
        return type;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
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

    public boolean isPassed() {
        return passed;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
