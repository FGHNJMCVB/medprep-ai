package com.medprep.dto;

import com.medprep.entity.QuestionDifficulty;

public class QuestionGenerationRequest {

    private Long subjectId;

    private Long topicId;

    private Integer count;

    private QuestionDifficulty difficulty;

    private Boolean highYield;

    private Boolean clinicalCase;

    private Boolean imageBased;

    private String focus;

    public QuestionGenerationRequest() {
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(QuestionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Boolean getHighYield() {
        return highYield;
    }

    public void setHighYield(Boolean highYield) {
        this.highYield = highYield;
    }

    public Boolean getClinicalCase() {
        return clinicalCase;
    }

    public void setClinicalCase(Boolean clinicalCase) {
        this.clinicalCase = clinicalCase;
    }

    public Boolean getImageBased() {
        return imageBased;
    }

    public void setImageBased(Boolean imageBased) {
        this.imageBased = imageBased;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
}