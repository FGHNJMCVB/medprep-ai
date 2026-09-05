package com.medprep.dto;

public class MockTestResponse {

    private Long configId;
    private String name;

    private Integer totalQuestions;
    private Integer partQuestions;
    private Integer partDurationMinutes;

    private Integer passingMarks;

    private Boolean negativeMarking;

    public MockTestResponse() {
    }

    public MockTestResponse(
            Long configId,
            String name,
            Integer totalQuestions,
            Integer partQuestions,
            Integer partDurationMinutes,
            Integer passingMarks,
            Boolean negativeMarking) {

        this.configId = configId;
        this.name = name;
        this.totalQuestions = totalQuestions;
        this.partQuestions = partQuestions;
        this.partDurationMinutes = partDurationMinutes;
        this.passingMarks = passingMarks;
        this.negativeMarking = negativeMarking;
    }

    public Long getConfigId() {
        return configId;
    }

    public String getName() {
        return name;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Integer getPartQuestions() {
        return partQuestions;
    }

    public Integer getPartDurationMinutes() {
        return partDurationMinutes;
    }

    public Integer getPassingMarks() {
        return passingMarks;
    }

    public Boolean getNegativeMarking() {
        return negativeMarking;
    }
}