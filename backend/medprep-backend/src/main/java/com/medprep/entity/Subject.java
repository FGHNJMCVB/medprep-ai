package com.medprep.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Integer weightageMarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectSection section;

    public Subject() {
    }

    public Subject(String name,
                   Integer displayOrder,
                   Integer weightageMarks,
                   SubjectSection section) {
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

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getWeightageMarks() {
        return weightageMarks;
    }

    public void setWeightageMarks(Integer weightageMarks) {
        this.weightageMarks = weightageMarks;
    }

    public SubjectSection getSection() {
        return section;
    }

    public void setSection(SubjectSection section) {
        this.section = section;
    }
}