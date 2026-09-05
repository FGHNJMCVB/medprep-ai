package com.medprep.dto;

import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionType;

import java.util.List;

public class AdminQuestionRequest {

    private Long subjectId;

    private Long topicId;

    private String questionText;

    private String explanation;

    private QuestionDifficulty difficulty;

    private QuestionType questionType;

    private String conceptTag;

    private String sourceYear;

    private String sourceSession;

    private String sourceReference;

    private Boolean highYield;

    private Boolean clinicalCase;

    private Boolean imageBased;

    private List<AdminQuestionOptionRequest> options;

    public AdminQuestionRequest() {
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(QuestionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getConceptTag() {
        return conceptTag;
    }

    public void setConceptTag(String conceptTag) {
        this.conceptTag = conceptTag;
    }

    public String getSourceYear() {
        return sourceYear;
    }

    public void setSourceYear(String sourceYear) {
        this.sourceYear = sourceYear;
    }

    public String getSourceSession() {
        return sourceSession;
    }

    public void setSourceSession(String sourceSession) {
        this.sourceSession = sourceSession;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
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

    public List<AdminQuestionOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<AdminQuestionOptionRequest> options) {
        this.options = options;
    }
}