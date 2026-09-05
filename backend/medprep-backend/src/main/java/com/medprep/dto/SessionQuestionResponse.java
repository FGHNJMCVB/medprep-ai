package com.medprep.dto;

import java.util.List;

public class SessionQuestionResponse {

    private Long sessionQuestionId;
    private Integer questionNumber;
    private Long questionId;
    private String questionText;
    private List<QuestionOptionResponse> options;

    public SessionQuestionResponse(
            Long sessionQuestionId,
            Integer questionNumber,
            Long questionId,
            String questionText,
            List<QuestionOptionResponse> options) {

        this.sessionQuestionId = sessionQuestionId;
        this.questionNumber = questionNumber;
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = options;
    }

    public Long getSessionQuestionId() {
        return sessionQuestionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<QuestionOptionResponse> getOptions() {
        return options;
    }
}