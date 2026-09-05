package com.medprep.service;

import com.medprep.dto.AdminQuestionTrendRequest;
import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;
import com.medprep.repository.QuestionTrendRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.medprep.dto.AdminQuestionTrendBulkRequest;

@Service
public class AdminQuestionTrendService {

    private final QuestionTrendRepository questionTrendRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final QuestionTrendService questionTrendService;

    public AdminQuestionTrendService(
            QuestionTrendRepository questionTrendRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionTrendService questionTrendService) {

        this.questionTrendRepository = questionTrendRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.questionTrendService = questionTrendService;
    }

    // ==========================================================
    // CREATE / UPDATE TREND
    // ==========================================================

    @Transactional
    public Long saveTrend(AdminQuestionTrendRequest request) {

        validateRequest(request);

        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found: "
                                + request.getSubjectId()));

        Topic topic = topicRepository
                .findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found: "
                                + request.getTopicId()));

        if (!topic.getSubject()
                .getId()
                .equals(subject.getId())) {

            throw new IllegalArgumentException(
                    "Topic does not belong to the selected subject");
        }

        String conceptTag = request.getConceptTag()
                .trim();

        double trendScore = questionTrendService.calculateTrendScore(
                request.getHistoricalFrequency(),
                request.getRecentFrequency(),
                request.getRecurrenceYears(),
                request.getClinicalWeight(),
                request.getImageWeight(),
                request.getIntegratedWeight());

        boolean highYield = request.getHighYield() != null
                ? request.getHighYield()
                : questionTrendService.isHighYield(
                        trendScore);

        QuestionTrend trend = questionTrendRepository
                .findBySubjectIdAndTopicIdAndConceptTag(
                        subject.getId(),
                        topic.getId(),
                        conceptTag)
                .orElse(null);

        if (trend == null) {

            trend = new QuestionTrend(
                    subject,
                    topic,
                    conceptTag,
                    request.getHistoricalFrequency(),
                    request.getRecentFrequency(),
                    request.getRecurrenceYears(),
                    request.getClinicalWeight(),
                    request.getImageWeight(),
                    request.getIntegratedWeight(),
                    trendScore,
                    highYield,
                    request.getSourceReference());

        } else {

            trend.setHistoricalFrequency(
                    request.getHistoricalFrequency());

            trend.setRecentFrequency(
                    request.getRecentFrequency());

            trend.setRecurrenceYears(
                    request.getRecurrenceYears());

            trend.setClinicalWeight(
                    request.getClinicalWeight());

            trend.setImageWeight(
                    request.getImageWeight());

            trend.setIntegratedWeight(
                    request.getIntegratedWeight());

            trend.setTrendScore(
                    trendScore);

            trend.setHighYield(
                    highYield);

            trend.setSourceReference(
                    request.getSourceReference());
        }

        QuestionTrend saved = questionTrendRepository.save(
                trend);

        return saved.getId();
    }

    // ==========================================================
    // CREATE / UPDATE TRENDS IN BULK
    // ==========================================================

    @Transactional
    public int saveTrendsInBulk(
            AdminQuestionTrendBulkRequest request) {

        if (request == null ||
                request.getTrends() == null ||
                request.getTrends().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one trend is required");
        }

        int processedCount = 0;

        for (int i = 0; i < request.getTrends().size(); i++) {

            try {

                saveTrend(
                        request.getTrends().get(i));

                processedCount++;

            } catch (Exception exception) {

                throw new IllegalArgumentException(
                        "Failed to import trend at index "
                                + i
                                + ": "
                                + exception.getMessage(),
                        exception);
            }
        }

        return processedCount;
    }

    // ==========================================================
    // VALIDATION
    // ==========================================================

    private void validateRequest(
            AdminQuestionTrendRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Trend request is required");
        }

        if (request.getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject ID is required");
        }

        if (request.getTopicId() == null) {

            throw new IllegalArgumentException(
                    "Topic ID is required");
        }

        if (request.getConceptTag() == null ||
                request.getConceptTag().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Concept tag is required");
        }

        if (request.getHistoricalFrequency() == null ||
                request.getHistoricalFrequency() < 0) {

            throw new IllegalArgumentException(
                    "Historical frequency must be zero or greater");
        }

        if (request.getRecentFrequency() == null ||
                request.getRecentFrequency() < 0) {

            throw new IllegalArgumentException(
                    "Recent frequency must be zero or greater");
        }

        if (request.getRecurrenceYears() == null ||
                request.getRecurrenceYears() < 0) {

            throw new IllegalArgumentException(
                    "Recurrence years must be zero or greater");
        }

        validateWeight(
                request.getClinicalWeight(),
                "Clinical weight");

        validateWeight(
                request.getImageWeight(),
                "Image weight");

        validateWeight(
                request.getIntegratedWeight(),
                "Integrated weight");
    }

    private void validateWeight(
            Double value,
            String fieldName) {

        if (value == null ||
                value < 0.0 ||
                value > 1.0) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must be between 0.0 and 1.0");
        }
    }
}