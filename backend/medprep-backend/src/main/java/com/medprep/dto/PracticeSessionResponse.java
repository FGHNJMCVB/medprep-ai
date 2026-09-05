package com.medprep.dto;

import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;

import java.time.LocalDateTime;
import java.util.List;

public class PracticeSessionResponse {

    private Long sessionId;

    private PracticeSessionType type;

    private PracticeSessionStatus status;

    private Long subjectId;

    private Long topicId;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private Integer correctAnswers;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private List<SessionQuestionResponse> questions;

    public PracticeSessionResponse() {
    }

    public PracticeSessionResponse(
            Long sessionId,
            PracticeSessionType type,
            PracticeSessionStatus status,
            Long subjectId,
            Long topicId,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            List<SessionQuestionResponse> questions) {

        this.sessionId = sessionId;
        this.type = type;
        this.status = status;
        this.subjectId = subjectId;
        this.topicId = topicId;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.questions = questions;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public PracticeSessionType getType() {
        return type;
    }

    public PracticeSessionStatus getStatus() {
        return status;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public List<SessionQuestionResponse> getQuestions() {
        return questions;
    }
}