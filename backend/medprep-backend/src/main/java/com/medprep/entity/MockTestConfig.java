package com.medprep.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mock_test_configs")
public class MockTestConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer partQuestions;

    @Column(nullable = false)
    private Integer partDurationMinutes;

    @Column(nullable = false)
    private Integer passingMarks;

    @Column(nullable = false)
    private Boolean negativeMarking;

    public MockTestConfig() {
    }

    public MockTestConfig(
            String name,
            Integer totalQuestions,
            Integer partQuestions,
            Integer partDurationMinutes,
            Integer passingMarks,
            Boolean negativeMarking) {

        this.name = name;
        this.totalQuestions = totalQuestions;
        this.partQuestions = partQuestions;
        this.partDurationMinutes = partDurationMinutes;
        this.passingMarks = passingMarks;
        this.negativeMarking = negativeMarking;
    }

    public Long getId() {
        return id;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setPartQuestions(Integer partQuestions) {
        this.partQuestions = partQuestions;
    }

    public void setPartDurationMinutes(Integer partDurationMinutes) {
        this.partDurationMinutes = partDurationMinutes;
    }

    public void setPassingMarks(Integer passingMarks) {
        this.passingMarks = passingMarks;
    }

    public void setNegativeMarking(Boolean negativeMarking) {
        this.negativeMarking = negativeMarking;
    }
}