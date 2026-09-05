package com.medprep.dto;

import java.util.List;

public class AdminBulkQuestionRequest {

    private List<AdminQuestionRequest> questions;

    public AdminBulkQuestionRequest() {
    }

    public List<AdminQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AdminQuestionRequest> questions) {
        this.questions = questions;
    }
}