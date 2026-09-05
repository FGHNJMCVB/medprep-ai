package com.medprep.dto;

import com.medprep.entity.SubjectSection;

public class SubjectResponse {

    private Long id;
    private String name;
    private Integer displayOrder;
    private Integer weightageMarks;
    private SubjectSection section;

    public SubjectResponse() {
    }

    public SubjectResponse(Long id,
                            String name,
                            Integer displayOrder,
                            Integer weightageMarks,
                            SubjectSection section) {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
        this.weightageMarks = weightageMarks;
        this.section = section;
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

    public Integer getWeightageMarks() {
        return weightageMarks;
    }

    public SubjectSection getSection() {
        return section;
    }
}