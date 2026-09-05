package com.medprep.dto;

public class PracticeHistoryResponse {

    private final Long sessionId;
    private final Long subjectId;
    private final Long topicId;

    private final Integer totalQuestions;
    private final Integer answeredQuestions;
    private final Integer correctAnswers;
    private final Integer wrongAnswers;
    private final Integer unansweredQuestions;

    private final Double accuracy;

    private final String startedAt;
    private final String completedAt;

    public PracticeHistoryResponse(
            Long sessionId,
            Long subjectId,
            Long topicId,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            Integer wrongAnswers,
            Integer unansweredQuestions,
            Double accuracy,
            String startedAt,
            String completedAt) {

        this.sessionId = sessionId;
        this.subjectId = subjectId;
        this.topicId = topicId;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unansweredQuestions = unansweredQuestions;
        this.accuracy = accuracy;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public Long getTopicId() {
        return topicId;
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

    public String getStartedAt() {
        return startedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}