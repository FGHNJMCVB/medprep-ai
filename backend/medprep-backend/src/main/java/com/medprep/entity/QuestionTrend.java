package com.medprep.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "question_trends",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trend_subject_topic_concept",
                        columnNames = {
                                "subject_id",
                                "topic_id",
                                "concept_tag"
                        }
                )
        }
)
public class QuestionTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==========================================================
    // SUBJECT / TOPIC
    // ==========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    // ==========================================================
    // CONCEPT
    // ==========================================================

    @Column(name = "concept_tag", nullable = false, length = 150)
    private String conceptTag;

    // ==========================================================
    // TREND METRICS
    // ==========================================================

    @Column(nullable = false)
    private Integer historicalFrequency;

    @Column(nullable = false)
    private Integer recentFrequency;

    @Column(nullable = false)
    private Integer recurrenceYears;

    @Column(nullable = false)
    private Double clinicalWeight;

    @Column(nullable = false)
    private Double imageWeight;

    @Column(nullable = false)
    private Double integratedWeight;

    @Column(nullable = false)
    private Double trendScore;

    @Column(nullable = false)
    private Boolean highYield;

    // ==========================================================
    // SOURCE
    // ==========================================================

    @Column(length = 255)
    private String sourceReference;

    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    public QuestionTrend() {
    }

    public QuestionTrend(
            Subject subject,
            Topic topic,
            String conceptTag,
            Integer historicalFrequency,
            Integer recentFrequency,
            Integer recurrenceYears,
            Double clinicalWeight,
            Double imageWeight,
            Double integratedWeight,
            Double trendScore,
            Boolean highYield,
            String sourceReference) {

        this.subject = subject;
        this.topic = topic;
        this.conceptTag = conceptTag;
        this.historicalFrequency = historicalFrequency;
        this.recentFrequency = recentFrequency;
        this.recurrenceYears = recurrenceYears;
        this.clinicalWeight = clinicalWeight;
        this.imageWeight = imageWeight;
        this.integratedWeight = integratedWeight;
        this.trendScore = trendScore;
        this.highYield = highYield;
        this.sourceReference = sourceReference;
    }

    // ==========================================================
    // GETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getConceptTag() {
        return conceptTag;
    }

    public Integer getHistoricalFrequency() {
        return historicalFrequency;
    }

    public Integer getRecentFrequency() {
        return recentFrequency;
    }

    public Integer getRecurrenceYears() {
        return recurrenceYears;
    }

    public Double getClinicalWeight() {
        return clinicalWeight;
    }

    public Double getImageWeight() {
        return imageWeight;
    }

    public Double getIntegratedWeight() {
        return integratedWeight;
    }

    public Double getTrendScore() {
        return trendScore;
    }

    public Boolean getHighYield() {
        return highYield;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    // ==========================================================
    // SETTERS
    // ==========================================================

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setConceptTag(String conceptTag) {
        this.conceptTag = conceptTag;
    }

    public void setHistoricalFrequency(
            Integer historicalFrequency) {

        this.historicalFrequency = historicalFrequency;
    }

    public void setRecentFrequency(
            Integer recentFrequency) {

        this.recentFrequency = recentFrequency;
    }

    public void setRecurrenceYears(
            Integer recurrenceYears) {

        this.recurrenceYears = recurrenceYears;
    }

    public void setClinicalWeight(
            Double clinicalWeight) {

        this.clinicalWeight = clinicalWeight;
    }

    public void setImageWeight(
            Double imageWeight) {

        this.imageWeight = imageWeight;
    }

    public void setIntegratedWeight(
            Double integratedWeight) {

        this.integratedWeight = integratedWeight;
    }

    public void setTrendScore(
            Double trendScore) {

        this.trendScore = trendScore;
    }

    public void setHighYield(Boolean highYield) {
        this.highYield = highYield;
    }

    public void setSourceReference(
            String sourceReference) {

        this.sourceReference = sourceReference;
    }
}