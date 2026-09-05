package com.medprep.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "practice_session_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_session_question",
                        columnNames = {"session_id", "question_id"}
                )
        }
)
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private PracticeSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean answered;

    @Column(nullable = false)
    private Boolean correct;

    private LocalDateTime answeredAt;

    public SessionQuestion() {
    }

    public SessionQuestion(
            PracticeSession session,
            Question question,
            Integer displayOrder) {

        this.session = session;
        this.question = question;
        this.displayOrder = displayOrder;
        this.answered = false;
        this.correct = false;
    }

    public Long getId() {
        return id;
    }

    public PracticeSession getSession() {
        return session;
    }

    public Question getQuestion() {
        return question;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Boolean getAnswered() {
        return answered;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void markAnswered(boolean correct) {
        this.answered = true;
        this.correct = correct;
        this.answeredAt = LocalDateTime.now();
    }
}