package com.medprep.service;

import com.medprep.entity.QuestionTrend;
import com.medprep.repository.QuestionTrendRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionTrendService {

    private final QuestionTrendRepository questionTrendRepository;

    public QuestionTrendService(
            QuestionTrendRepository questionTrendRepository) {

        this.questionTrendRepository =
                questionTrendRepository;
    }

    // ==========================================================
    // GET ALL TRENDS
    // ==========================================================

    @Transactional(readOnly = true)
    public List<QuestionTrend> getAllTrends() {

        return questionTrendRepository
                .findAllByOrderByTrendScoreDesc();
    }

    // ==========================================================
    // GET SUBJECT TRENDS
    // ==========================================================

    @Transactional(readOnly = true)
    public List<QuestionTrend> getSubjectTrends(
            Long subjectId) {

        return questionTrendRepository
                .findBySubjectIdOrderByTrendScoreDesc(
                        subjectId
                );
    }

    // ==========================================================
    // GET TOPIC TRENDS
    // ==========================================================

    @Transactional(readOnly = true)
    public List<QuestionTrend> getTopicTrends(
            Long topicId) {

        return questionTrendRepository
                .findByTopicIdOrderByTrendScoreDesc(
                        topicId
                );
    }

    // ==========================================================
    // CALCULATE TREND SCORE
    // ==========================================================

    public double calculateTrendScore(
            int historicalFrequency,
            int recentFrequency,
            int recurrenceYears,
            double clinicalWeight,
            double imageWeight,
            double integratedWeight) {

        /*
         * These weights are our application's ranking model.
         *
         * Historical frequency:
         *       25%
         *
         * Recent frequency:
         *       25%
         *
         * Recurrence across years:
         *       20%
         *
         * Clinical relevance:
         *       15%
         *
         * Image relevance:
         *       5%
         *
         * Integrated / multi-subject relevance:
         *       10%
         */

        double historicalScore =
                normalizeFrequency(
                        historicalFrequency
                );

        double recentScore =
                normalizeFrequency(
                        recentFrequency
                );

        double recurrenceScore =
                normalizeFrequency(
                        recurrenceYears
                );

        double score =
                (historicalScore * 0.25)
              + (recentScore * 0.25)
              + (recurrenceScore * 0.20)
              + (clinicalWeight * 0.15)
              + (imageWeight * 0.05)
              + (integratedWeight * 0.10);

        return round(score);
    }

    // ==========================================================
    // HIGH-YIELD DECISION
    // ==========================================================

    public boolean isHighYield(
            double trendScore) {

        return trendScore >= 0.75;
    }

    // ==========================================================
    // NORMALIZE FREQUENCY
    // ==========================================================

    private double normalizeFrequency(
            int frequency) {

        if(frequency <= 0) {
            return 0.0;
        }

        /*
         * We cap the frequency score at 20.
         *
         * This prevents a concept appearing many times
         * from completely dominating every other metric.
         */

        double score =
                (double) frequency / 20.0;

        if(score > 1.0) {
            score = 1.0;
        }

        return score;
    }

    // ==========================================================
    // ROUND SCORE
    // ==========================================================

    private double round(
            double value) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}