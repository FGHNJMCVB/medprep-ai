package com.medprep.dto;

public class AdminQuestionTrendRequest {

    private Long subjectId;

    private Long topicId;

    private String conceptTag;

    private Integer historicalFrequency;

    private Integer recentFrequency;

    private Integer recurrenceYears;

    private Double clinicalWeight;

    private Double imageWeight;

    private Double integratedWeight;

    private Boolean highYield;

    private String sourceReference;

    public AdminQuestionTrendRequest() {
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getConceptTag() {
        return conceptTag;
    }

    public void setConceptTag(String conceptTag) {
        this.conceptTag = conceptTag;
    }

    public Integer getHistoricalFrequency() {
        return historicalFrequency;
    }

    public void setHistoricalFrequency(Integer historicalFrequency) {
        this.historicalFrequency = historicalFrequency;
    }

    public Integer getRecentFrequency() {
        return recentFrequency;
    }

    public void setRecentFrequency(Integer recentFrequency) {
        this.recentFrequency = recentFrequency;
    }

    public Integer getRecurrenceYears() {
        return recurrenceYears;
    }

    public void setRecurrenceYears(Integer recurrenceYears) {
        this.recurrenceYears = recurrenceYears;
    }

    public Double getClinicalWeight() {
        return clinicalWeight;
    }

    public void setClinicalWeight(Double clinicalWeight) {
        this.clinicalWeight = clinicalWeight;
    }

    public Double getImageWeight() {
        return imageWeight;
    }

    public void setImageWeight(Double imageWeight) {
        this.imageWeight = imageWeight;
    }

    public Double getIntegratedWeight() {
        return integratedWeight;
    }

    public void setIntegratedWeight(Double integratedWeight) {
        this.integratedWeight = integratedWeight;
    }

    public Boolean getHighYield() {
        return highYield;
    }

    public void setHighYield(Boolean highYield) {
        this.highYield = highYield;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }
}