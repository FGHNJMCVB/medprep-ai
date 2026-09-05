package com.medprep.dto;

public class QuestionOptionResponse {

    private Long id;
    private String optionLabel;
    private String optionText;
    private Integer displayOrder;

    public QuestionOptionResponse() {
    }

    public QuestionOptionResponse(Long id,
                                  String optionLabel,
                                  String optionText,
                                  Integer displayOrder) {
        this.id = id;
        this.optionLabel = optionLabel;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public String getOptionText() {
        return optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}