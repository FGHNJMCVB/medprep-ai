package com.medprep.dto;

public class AdminQuestionOptionRequest {

    private String optionLabel;

    private String optionText;

    private Integer displayOrder;

    private Boolean correct;

    public AdminQuestionOptionRequest() {
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}