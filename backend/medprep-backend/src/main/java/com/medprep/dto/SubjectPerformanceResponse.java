package com.medprep.dto;

public class SubjectPerformanceResponse {

    private Long subjectId;
    private String subjectName;

    private long attempted;
    private long correct;
    private long wrong;

    private double accuracy;

    public SubjectPerformanceResponse() {
    }

    public SubjectPerformanceResponse(
            Long subjectId,
            String subjectName,
            long attempted,
            long correct,
            long wrong,
            double accuracy) {

        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.attempted = attempted;
        this.correct = correct;
        this.wrong = wrong;
        this.accuracy = accuracy;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public long getAttempted() {
        return attempted;
    }

    public long getCorrect() {
        return correct;
    }

    public long getWrong() {
        return wrong;
    }

    public double getAccuracy() {
        return accuracy;
    }
}