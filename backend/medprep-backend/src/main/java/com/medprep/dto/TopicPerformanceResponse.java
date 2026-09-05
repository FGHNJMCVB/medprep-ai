package com.medprep.dto;

public class TopicPerformanceResponse {

    private Long topicId;
    private String topicName;

    private Long subjectId;
    private String subjectName;

    private long attempted;
    private long correct;
    private long wrong;

    private double accuracy;

    public TopicPerformanceResponse() {
    }

    public TopicPerformanceResponse(
            Long topicId,
            String topicName,
            Long subjectId,
            String subjectName,
            long attempted,
            long correct,
            long wrong,
            double accuracy) {

        this.topicId = topicId;
        this.topicName = topicName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.attempted = attempted;
        this.correct = correct;
        this.wrong = wrong;
        this.accuracy = accuracy;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getTopicName() {
        return topicName;
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