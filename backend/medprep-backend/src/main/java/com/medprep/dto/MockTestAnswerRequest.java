package com.medprep.dto;

public class MockTestAnswerRequest {

    private Long selectedOptionId;

    private Integer timeTakenSeconds;

    public MockTestAnswerRequest() {
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId =
                selectedOptionId;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(
            Integer timeTakenSeconds) {

        this.timeTakenSeconds =
                timeTakenSeconds;
    }
}