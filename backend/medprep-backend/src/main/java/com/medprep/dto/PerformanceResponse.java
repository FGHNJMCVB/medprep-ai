package com.medprep.dto;

import java.util.List;

public class PerformanceResponse {

    private long totalQuestions;
    private long correctAnswers;
    private long wrongAnswers;

    private double accuracy;
    private double averageTimeSeconds;

    private List<SubjectPerformanceResponse> subjectPerformance;
    private List<TopicPerformanceResponse> topicPerformance;
    private List<TopicPerformanceResponse> weakTopics;

    public PerformanceResponse() {
    }

    public PerformanceResponse(
            long totalQuestions,
            long correctAnswers,
            long wrongAnswers,
            double accuracy,
            double averageTimeSeconds,
            List<SubjectPerformanceResponse> subjectPerformance,
            List<TopicPerformanceResponse> topicPerformance,
            List<TopicPerformanceResponse> weakTopics) {

        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.accuracy = accuracy;
        this.averageTimeSeconds = averageTimeSeconds;
        this.subjectPerformance = subjectPerformance;
        this.topicPerformance = topicPerformance;
        this.weakTopics = weakTopics;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public long getWrongAnswers() {
        return wrongAnswers;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getAverageTimeSeconds() {
        return averageTimeSeconds;
    }

    public List<SubjectPerformanceResponse> getSubjectPerformance() {
        return subjectPerformance;
    }

    public List<TopicPerformanceResponse> getTopicPerformance() {
        return topicPerformance;
    }

    public List<TopicPerformanceResponse> getWeakTopics() {
        return weakTopics;
    }
}