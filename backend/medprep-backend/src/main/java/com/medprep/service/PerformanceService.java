package com.medprep.service;

import com.medprep.dto.PerformanceResponse;
import com.medprep.dto.SubjectPerformanceResponse;
import com.medprep.dto.TopicPerformanceResponse;
import com.medprep.entity.Attempt;
import com.medprep.repository.AttemptRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceService {

    private final AttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public PerformanceService(
            AttemptRepository attemptRepository,
            UserRepository userRepository) {

        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PerformanceResponse getPerformance(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        List<Attempt> attempts =
                attemptRepository
                        .findByUserIdOrderByAttemptedAtDesc(userId);

        long totalQuestions = attempts.size();

        long correctAnswers = attempts.stream()
                .filter(Attempt::getCorrect)
                .count();

        long wrongAnswers =
                totalQuestions - correctAnswers;

        double accuracy = 0.0;

        if(totalQuestions > 0) {
            accuracy =
                    ((double) correctAnswers / totalQuestions) * 100.0;
        }

        double averageTimeSeconds = 0.0;

        if(!attempts.isEmpty()) {

            long totalTime = attempts.stream()
                    .mapToLong(Attempt::getTimeTakenSeconds)
                    .sum();

            averageTimeSeconds =
                    (double) totalTime / attempts.size();
        }

        List<SubjectPerformanceResponse> subjectPerformance =
                buildSubjectPerformance(attempts);

        List<TopicPerformanceResponse> topicPerformance =
                buildTopicPerformance(attempts);

        List<TopicPerformanceResponse> weakTopics =
                topicPerformance.stream()
                        .filter(topic ->
                                topic.getAccuracy() < 70.0)
                        .sorted(
                                Comparator.comparingDouble(
                                        TopicPerformanceResponse::getAccuracy
                                )
                        )
                        .limit(5)
                        .toList();

        return new PerformanceResponse(
                totalQuestions,
                correctAnswers,
                wrongAnswers,
                round(accuracy),
                round(averageTimeSeconds),
                subjectPerformance,
                topicPerformance,
                weakTopics
        );
    }

    private List<SubjectPerformanceResponse> buildSubjectPerformance(
            List<Attempt> attempts) {

        Map<Long, SubjectStats> statsMap =
                new LinkedHashMap<>();

        for(Attempt attempt : attempts) {

            Long subjectId =
                    attempt.getQuestion()
                            .getSubject()
                            .getId();

            String subjectName =
                    attempt.getQuestion()
                            .getSubject()
                            .getName();

            SubjectStats stats =
                    statsMap.computeIfAbsent(
                            subjectId,
                            id -> new SubjectStats(
                                    subjectId,
                                    subjectName
                            )
                    );

            stats.attempted++;

            if(Boolean.TRUE.equals(attempt.getCorrect())) {
                stats.correct++;
            }
        }

        List<SubjectPerformanceResponse> result =
                new ArrayList<>();

        for(SubjectStats stats : statsMap.values()) {

            long wrong =
                    stats.attempted - stats.correct;

            double accuracy = 0.0;

            if(stats.attempted > 0) {

                accuracy =
                        ((double) stats.correct
                                / stats.attempted) * 100.0;
            }

            result.add(
                    new SubjectPerformanceResponse(
                            stats.subjectId,
                            stats.subjectName,
                            stats.attempted,
                            stats.correct,
                            wrong,
                            round(accuracy)
                    )
            );
        }

        return result;
    }

    private List<TopicPerformanceResponse> buildTopicPerformance(
            List<Attempt> attempts) {

        Map<Long, TopicStats> statsMap =
                new LinkedHashMap<>();

        for(Attempt attempt : attempts) {

            Long topicId =
                    attempt.getQuestion()
                            .getTopic()
                            .getId();

            String topicName =
                    attempt.getQuestion()
                            .getTopic()
                            .getName();

            Long subjectId =
                    attempt.getQuestion()
                            .getSubject()
                            .getId();

            String subjectName =
                    attempt.getQuestion()
                            .getSubject()
                            .getName();

            TopicStats stats =
                    statsMap.computeIfAbsent(
                            topicId,
                            id -> new TopicStats(
                                    topicId,
                                    topicName,
                                    subjectId,
                                    subjectName
                            )
                    );

            stats.attempted++;

            if(Boolean.TRUE.equals(attempt.getCorrect())) {
                stats.correct++;
            }
        }

        List<TopicPerformanceResponse> result =
                new ArrayList<>();

        for(TopicStats stats : statsMap.values()) {

            long wrong =
                    stats.attempted - stats.correct;

            double accuracy = 0.0;

            if(stats.attempted > 0) {

                accuracy =
                        ((double) stats.correct
                                / stats.attempted) * 100.0;
            }

            result.add(
                    new TopicPerformanceResponse(
                            stats.topicId,
                            stats.topicName,
                            stats.subjectId,
                            stats.subjectName,
                            stats.attempted,
                            stats.correct,
                            wrong,
                            round(accuracy)
                    )
            );
        }

        return result;
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    // ==========================================================
    // INTERNAL STATS CLASSES
    // ==========================================================

    private static class SubjectStats {

        private final Long subjectId;
        private final String subjectName;

        private long attempted;
        private long correct;

        private SubjectStats(
                Long subjectId,
                String subjectName) {

            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }
    }

    private static class TopicStats {

        private final Long topicId;
        private final String topicName;

        private final Long subjectId;
        private final String subjectName;

        private long attempted;
        private long correct;

        private TopicStats(
                Long topicId,
                String topicName,
                Long subjectId,
                String subjectName) {

            this.topicId = topicId;
            this.topicName = topicName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }
    }
}