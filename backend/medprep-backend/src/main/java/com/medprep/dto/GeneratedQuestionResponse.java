package com.medprep.dto;

import java.util.List;

public class GeneratedQuestionResponse {

    private List<AdminQuestionRequest> questions;

    public GeneratedQuestionResponse() {
    }

    public List<AdminQuestionRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(
            List<AdminQuestionRequest> questions) {

        this.questions = questions;
    }
}