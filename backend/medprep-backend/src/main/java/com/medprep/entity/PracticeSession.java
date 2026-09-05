package com.medprep.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_sessions")
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeSessionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeSessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer answeredQuestions;

    @Column(nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    public PracticeSession() {
    }

    public PracticeSession(
            User user,
            PracticeSessionType type,
            PracticeSessionStatus status,
            Subject subject,
            Topic topic,
            Integer totalQuestions,
            Integer answeredQuestions,
            Integer correctAnswers,
            LocalDateTime startedAt) {

        this.user = user;
        this.type = type;
        this.status = status;
        this.subject = subject;
        this.topic = topic;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.correctAnswers = correctAnswers;
        this.startedAt = startedAt;
    }

    // ==========================================================
    // GETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public PracticeSessionType getType() {
        return type;
    }

    public PracticeSessionStatus getStatus() {
        return status;
    }

    public Subject getSubject() {
        return subject;
    }

    public Topic getTopic() {
        return topic;
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

    // ==========================================================
    // SETTERS
    // ==========================================================

    public void setTotalQuestions(
            Integer totalQuestions) {

        this.totalQuestions = totalQuestions;
    }

    public void setStatus(
            PracticeSessionStatus status) {

        this.status = status;
    }

    public void setAnsweredQuestions(
            Integer answeredQuestions) {

        this.answeredQuestions = answeredQuestions;
    }

    public void setCorrectAnswers(
            Integer correctAnswers) {

        this.correctAnswers = correctAnswers;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }
}