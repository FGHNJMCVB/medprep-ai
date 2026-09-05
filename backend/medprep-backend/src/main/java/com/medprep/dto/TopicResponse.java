package com.medprep.dto;

public class TopicResponse {

    private Long id;
    private String name;
    private Integer displayOrder;
    private Long subjectId;

    public TopicResponse() {
    }

    public TopicResponse(Long id,
                          String name,
                          Integer displayOrder,
                          Long subjectId) {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
        this.subjectId = subjectId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Long getSubjectId() {
        return subjectId;
    }
}