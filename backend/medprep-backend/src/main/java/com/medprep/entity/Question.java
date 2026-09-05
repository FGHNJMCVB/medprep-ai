package com.medprep.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================================
    // QUESTION CONTENT
    // ==========================================================

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    // ==========================================================
    // QUESTION METADATA
    // ==========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(length = 100)
    private String conceptTag;

    @Column(length = 100)
    private String sourceYear;

    @Column(length = 100)
    private String sourceSession;

    @Column(length = 255)
    private String sourceReference;

    @Column(nullable = false)
    private Boolean highYield;

    @Column(nullable = false)
    private Boolean clinicalCase;

    @Column(nullable = false)
    private Boolean imageBased;

    @Column(nullable = false)
    private Boolean active;

    // ==========================================================
    // RELATIONSHIPS
    // ==========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuestionOption> options = new ArrayList<>();

    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    public Question() {
    }

    public Question(
            String questionText,
            String explanation,
            QuestionDifficulty difficulty,
            Boolean active,
            Subject subject,
            Topic topic) {

        this.questionText = questionText;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.questionType = QuestionType.ORIGINAL;
        this.conceptTag = null;
        this.sourceYear = null;
        this.sourceSession = null;
        this.sourceReference = null;
        this.highYield = false;
        this.clinicalCase = false;
        this.imageBased = false;
        this.active = active;
        this.subject = subject;
        this.topic = topic;
    }

    public Question(
            String questionText,
            String explanation,
            QuestionDifficulty difficulty,
            QuestionType questionType,
            String conceptTag,
            String sourceYear,
            String sourceSession,
            String sourceReference,
            Boolean highYield,
            Boolean clinicalCase,
            Boolean imageBased,
            Boolean active,
            Subject subject,
            Topic topic) {

        this.questionText = questionText;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.questionType = questionType;
        this.conceptTag = conceptTag;
        this.sourceYear = sourceYear;
        this.sourceSession = sourceSession;
        this.sourceReference = sourceReference;
        this.highYield = highYield;
        this.clinicalCase = clinicalCase;
        this.imageBased = imageBased;
        this.active = active;
        this.subject = subject;
        this.topic = topic;
    }

    // ==========================================================
    // GETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public String getConceptTag() {
        return conceptTag;
    }

    public String getSourceYear() {
        return sourceYear;
    }

    public String getSourceSession() {
        return sourceSession;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public Boolean getHighYield() {
        return highYield;
    }

    public Boolean getClinicalCase() {
        return clinicalCase;
    }

    public Boolean getImageBased() {
        return imageBased;
    }

    public Boolean getActive() {
        return active;
    }

    public Subject getSubject() {
        return subject;
    }

    public Topic getTopic() {
        return topic;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    // ==========================================================
    // SETTERS
    // ==========================================================

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setDifficulty(QuestionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public void setConceptTag(String conceptTag) {
        this.conceptTag = conceptTag;
    }

    public void setSourceYear(String sourceYear) {
        this.sourceYear = sourceYear;
    }

    public void setSourceSession(String sourceSession) {
        this.sourceSession = sourceSession;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public void setHighYield(Boolean highYield) {
        this.highYield = highYield;
    }

    public void setClinicalCase(Boolean clinicalCase) {
        this.clinicalCase = clinicalCase;
    }

    public void setImageBased(Boolean imageBased) {
        this.imageBased = imageBased;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
}