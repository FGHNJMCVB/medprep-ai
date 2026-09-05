package com.medprep.dto;

public class QuestionTrendResponse {

    private Long id;

    private Long subjectId;
    private String subjectName;

    private Long topicId;
    private String topicName;

    private String conceptTag;

    private Integer historicalFrequency;
    private Integer recentFrequency;
    private Integer recurrenceYears;

    private Double clinicalWeight;
    private Double imageWeight;
    private Double integratedWeight;

    private Double trendScore;

    private Boolean highYield;

    private String sourceReference;

    public QuestionTrendResponse() {
    }

    public QuestionTrendResponse(
            Long id,
            Long subjectId,
            String subjectName,
            Long topicId,
            String topicName,
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

        this.id = id;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.topicId = topicId;
        this.topicName = topicName;
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

    public Long getId() {
        return id;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getTopicName() {
        return topicName;
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
}