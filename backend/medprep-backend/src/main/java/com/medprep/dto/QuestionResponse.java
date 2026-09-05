package com.medprep.dto;

import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionType;

import java.util.List;

public class QuestionResponse {

    private Long id;

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

    private Long subjectId;

    private Long topicId;

    private List<QuestionOptionResponse> options;

    public QuestionResponse() {
    }

    public QuestionResponse(
            Long id,
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
            Long subjectId,
            Long topicId,
            List<QuestionOptionResponse> options) {

        this.id = id;
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
        this.subjectId = subjectId;
        this.topicId = topicId;
        this.options = options;
    }

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

    public Long getSubjectId() {
        return subjectId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public List<QuestionOptionResponse> getOptions() {
        return options;
    }
}