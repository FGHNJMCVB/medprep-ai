package com.medprep.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.medprep.dto.QuestionGenerationRequest;

import com.medprep.entity.Question;
import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;

import com.medprep.repository.QuestionRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionBankGenerationService {

    // ==========================================================
    // QUESTION BANK TARGET
    // ==========================================================

    private static final int TARGET_PER_TOPIC = 150;

    private static final int EASY_TARGET = 30;

    private static final int MEDIUM_TARGET = 90;

    private static final int HARD_TARGET = 30;

    /*
     * Never ask Gemini for more than 10 questions
     * in a single generation request.
     */
    private static final int MAX_BATCH_SIZE = 10;

    /*
     * Stop if repeated generation attempts produce
     * no database progress.
     */
    private static final int MAX_STALLED_ATTEMPTS = 2;

    private final AiQuestionGenerationService aiQuestionGenerationService;

    private final QuestionRepository questionRepository;

    private final TopicRepository topicRepository;

    private final ObjectMapper objectMapper;

    public QuestionBankGenerationService(
            AiQuestionGenerationService aiQuestionGenerationService,
            QuestionRepository questionRepository,
            TopicRepository topicRepository,
            ObjectMapper objectMapper) {

        this.aiQuestionGenerationService = aiQuestionGenerationService;

        this.questionRepository = questionRepository;

        this.topicRepository = topicRepository;

        this.objectMapper = objectMapper;
    }
    // ==========================================================
    // GET COMPLETE QUESTION BANK STATUS
    // ==========================================================

    public List<Map<String, Object>> getAllTopicProgress() {

        List<Topic> topics = new ArrayList<>(
                topicRepository.findAll());

        topics.sort(
                Comparator
                        .comparing(
                                (Topic topic) -> topic.getSubject() != null
                                        ? safeDisplayOrder(
                                                topic.getSubject())
                                        : Integer.MAX_VALUE)
                        .thenComparing(
                                topic -> safeDisplayOrder(topic))
                        .thenComparing(
                                topic -> topic.getId() == null
                                        ? Long.MAX_VALUE
                                        : topic.getId()));

        List<Map<String, Object>> progress = new ArrayList<>();

        for (Topic topic : topics) {

            if (topic == null ||
                    topic.getId() == null) {

                continue;
            }

            progress.add(
                    buildTopicProgress(topic));
        }

        return progress;
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS FOR ONE TOPIC
    // ==========================================================

    public Map<String, Object> generateMissingForTopic(
            Long topicId) {

        Topic topic = findTopic(topicId);

        /*
         * Capture the state before generation.
         */
        TopicQuestionCounts initialCounts = loadQuestionCounts(topic);

        // ------------------------------------------------------
        // EASY
        // ------------------------------------------------------

        generateMissingDifficulty(
                topic,
                QuestionDifficulty.EASY);

        // ------------------------------------------------------
        // MEDIUM
        // ------------------------------------------------------

        generateMissingDifficulty(
                topic,
                QuestionDifficulty.MEDIUM);

        // ------------------------------------------------------
        // HARD
        // ------------------------------------------------------

        generateMissingDifficulty(
                topic,
                QuestionDifficulty.HARD);

        /*
         * Load the final state once.
         */
        TopicQuestionCounts finalCounts = loadQuestionCounts(topic);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "topicId",
                topic.getId());

        result.put(
                "topicName",
                topic.getName());

        result.put(
                "subjectId",
                topic.getSubject() != null
                        ? topic.getSubject().getId()
                        : null);

        result.put(
                "subjectName",
                topic.getSubject() != null
                        ? topic.getSubject().getName()
                        : null);

        result.put(
                "initialTotalQuestions",
                initialCounts.total);

        result.put(
                "initialEasyQuestions",
                initialCounts.easy);

        result.put(
                "initialMediumQuestions",
                initialCounts.medium);

        result.put(
                "initialHardQuestions",
                initialCounts.hard);

        result.put(
                "generatedQuestions",
                Math.max(
                        0,
                        finalCounts.total -
                                initialCounts.total));

        result.put(
                "success",
                true);

        result.put(
                "progress",
                buildTopicProgress(
                        topic,
                        finalCounts));

        return result;
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS FOR MULTIPLE TOPICS
    // ==========================================================

    public Map<String, Object> generateMissingForTopics(
            int maxTopics) {

        if (maxTopics <= 0) {

            throw new IllegalArgumentException(
                    "maxTopics must be greater than zero");
        }

        List<Topic> topics = new ArrayList<>(
                topicRepository.findAll());

        topics.sort(
                Comparator
                        .comparing(
                                (Topic topic) -> topic.getSubject() != null
                                        ? safeDisplayOrder(
                                                topic.getSubject())
                                        : Integer.MAX_VALUE)
                        .thenComparing(
                                topic -> safeDisplayOrder(topic))
                        .thenComparing(
                                topic -> topic.getId() == null
                                        ? Long.MAX_VALUE
                                        : topic.getId()));

        List<Map<String, Object>> topicResults = new ArrayList<>();

        int processedTopics = 0;

        int generatedQuestions = 0;

        int successfulTopics = 0;

        int failedTopics = 0;

        for (Topic topic : topics) {

            if (topic == null ||
                    topic.getId() == null) {

                continue;
            }

            if (processedTopics >= maxTopics) {

                break;
            }

            /*
             * Check current progress before generating.
             *
             * This also ensures already-complete topics are skipped.
             */
            Map<String, Object> currentProgress = buildTopicProgress(topic);

            boolean complete = Boolean.TRUE.equals(
                    currentProgress.get(
                            "complete"));

            if (complete) {

                continue;
            }

            /*
             * Isolate every topic.
             *
             * A failure in one topic must not prevent
             * subsequent topics from being processed.
             */
            try {

                Map<String, Object> result = generateMissingForTopic(
                        topic.getId());

                topicResults.add(
                        result);

                generatedQuestions += integerValue(
                        result.get(
                                "generatedQuestions"));

                successfulTopics++;

            } catch (Exception generationError) {

                Map<String, Object> failure = new LinkedHashMap<>();

                failure.put(
                        "topicId",
                        topic.getId());

                failure.put(
                        "topicName",
                        topic.getName());

                failure.put(
                        "subjectId",
                        topic.getSubject() != null
                                ? topic.getSubject().getId()
                                : null);

                failure.put(
                        "subjectName",
                        topic.getSubject() != null
                                ? topic.getSubject().getName()
                                : null);

                String errorMessage = generationError.getMessage();

                if (errorMessage == null ||
                        errorMessage.trim().isEmpty()) {

                    errorMessage = generationError
                            .getClass()
                            .getSimpleName();
                }

                failure.put(
                        "success",
                        false);

                failure.put(
                        "error",
                        errorMessage);

                topicResults.add(
                        failure);

                failedTopics++;
            }

            /*
             * Count the topic as processed whether it succeeded
             * or failed.
             */
            processedTopics++;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put(
                "requestedTopicLimit",
                maxTopics);

        response.put(
                "processedTopics",
                processedTopics);

        response.put(
                "successfulTopics",
                successfulTopics);

        response.put(
                "failedTopics",
                failedTopics);

        response.put(
                "generatedQuestions",
                generatedQuestions);

        response.put(
                "topics",
                topicResults);

        return response;
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS FOR ONE DIFFICULTY
    // ==========================================================

    private void generateMissingDifficulty(
            Topic topic,
            QuestionDifficulty difficulty) {

        int stalledAttempts = 0;

        while (true) {

            /*
             * ONE database query.
             *
             * From this single list we calculate:
             * total
             * EASY
             * MEDIUM
             * HARD
             */
            TopicQuestionCounts currentCounts = loadQuestionCounts(topic);

            int currentCount = currentCounts.getCount(
                    difficulty);

            int target = getTarget(
                    difficulty);

            int remaining = target - currentCount;

            // --------------------------------------------------
            // Target already reached.
            // --------------------------------------------------

            if (remaining <= 0) {

                return;
            }

            int batchSize = Math.min(
                    remaining,
                    MAX_BATCH_SIZE);

            /*
             * Generate only the missing amount, never more
             * than MAX_BATCH_SIZE.
             */
            List<Long> generatedIds = generateBatch(
                    topic,
                    difficulty,
                    batchSize);

            /*
             * Load current state once after generation.
             *
             * This is required because the generation service
             * may reject exact duplicates and therefore save
             * fewer questions than requested.
             */
            TopicQuestionCounts updatedCounts = loadQuestionCounts(topic);

            int updatedCount = updatedCounts.getCount(
                    difficulty);

            if (generatedIds == null ||
                    generatedIds.isEmpty() ||
                    updatedCount <= currentCount) {

                stalledAttempts++;

            } else {

                stalledAttempts = 0;
            }

            /*
             * Avoid infinite Gemini calls if a topic repeatedly
             * produces duplicates or otherwise makes no progress.
             */
            if (stalledAttempts >= MAX_STALLED_ATTEMPTS) {

                throw new IllegalStateException(
                        "Unable to fill "
                                + topic.getName()
                                + " with enough "
                                + difficulty
                                + " questions. "
                                + "Generation stopped after "
                                + MAX_STALLED_ATTEMPTS
                                + " attempts without database progress.");
            }
        }
    }

    // ==========================================================
    // GENERATE ONE SAFE BATCH
    // ==========================================================

    private List<Long> generateBatch(
            Topic topic,
            QuestionDifficulty difficulty,
            int batchSize) {

        Long subjectId = topic.getSubject() != null
                ? topic.getSubject().getId()
                : null;

        if (subjectId == null) {

            throw new IllegalStateException(
                    "Topic "
                            + topic.getId()
                            + " has no associated subject");
        }

        /*
         * Build the existing QuestionGenerationRequest
         * without changing its public structure.
         */
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put(
                "subjectId",
                subjectId);

        payload.put(
                "topicId",
                topic.getId());

        payload.put(
                "count",
                batchSize);

        payload.put(
                "difficulty",
                difficulty.name());

        payload.put(
                "highYield",
                true);

        payload.put(
                "clinicalCase",
                true);

        /*
         * Keep false until a real image-question asset
         * pipeline exists.
         */
        payload.put(
                "imageBased",
                false);

        payload.put(
                "focus",
                "FMGE question bank generation. "
                        + "Generate questions strictly for: "
                        + topic.getSubject().getName()
                        + " > "
                        + topic.getName()
                        + ". "
                        + "Do not broaden beyond this topic.");

        QuestionGenerationRequest request;

        try {

            request = objectMapper.convertValue(
                    payload,
                    QuestionGenerationRequest.class);

        } catch (IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "Unable to build question generation "
                            + "request for "
                            + topic.getSubject().getName()
                            + " > "
                            + topic.getName(),
                    exception);
        }

        return aiQuestionGenerationService
                .generateAndSaveQuestions(
                        request);
    }

    // ==========================================================
    // BUILD TOPIC PROGRESS
    // ==========================================================

    private Map<String, Object> buildTopicProgress(
            Topic topic) {

        /*
         * ONE database query for this topic.
         */
        TopicQuestionCounts counts = loadQuestionCounts(topic);

        return buildTopicProgress(
                topic,
                counts);
    }

    // ==========================================================
    // BUILD TOPIC PROGRESS FROM EXISTING COUNTS
    // ==========================================================

    private Map<String, Object> buildTopicProgress(
            Topic topic,
            TopicQuestionCounts counts) {

        int total = counts.total;

        int easy = counts.easy;

        int medium = counts.medium;

        int hard = counts.hard;

        int missing = Math.max(
                0,
                TARGET_PER_TOPIC - total);

        boolean complete = easy >= EASY_TARGET &&
                medium >= MEDIUM_TARGET &&
                hard >= HARD_TARGET;

        Map<String, Object> progress = new LinkedHashMap<>();

        progress.put(
                "topicId",
                topic.getId());

        progress.put(
                "topicName",
                topic.getName());

        progress.put(
                "subjectId",
                topic.getSubject() != null
                        ? topic.getSubject().getId()
                        : null);

        progress.put(
                "subjectName",
                topic.getSubject() != null
                        ? topic.getSubject().getName()
                        : null);

        progress.put(
                "totalQuestions",
                total);

        progress.put(
                "targetQuestions",
                TARGET_PER_TOPIC);

        progress.put(
                "missingQuestions",
                missing);

        progress.put(
                "easyQuestions",
                easy);

        progress.put(
                "mediumQuestions",
                medium);

        progress.put(
                "hardQuestions",
                hard);

        progress.put(
                "easyTarget",
                EASY_TARGET);

        progress.put(
                "mediumTarget",
                MEDIUM_TARGET);

        progress.put(
                "hardTarget",
                HARD_TARGET);

        progress.put(
                "easyMissing",
                Math.max(
                        0,
                        EASY_TARGET - easy));

        progress.put(
                "mediumMissing",
                Math.max(
                        0,
                        MEDIUM_TARGET - medium));

        progress.put(
                "hardMissing",
                Math.max(
                        0,
                        HARD_TARGET - hard));

        progress.put(
                "complete",
                complete);

        return progress;
    }

    // ==========================================================
    // LOAD QUESTION COUNTS
    // ==========================================================

    private TopicQuestionCounts loadQuestionCounts(
            Topic topic) {

        /*
         * Exactly ONE query for the topic.
         */
        List<Question> questions = questionRepository
                .findByTopicIdAndActiveTrueOrderByIdAsc(
                        topic.getId());

        TopicQuestionCounts counts = new TopicQuestionCounts();

        if (questions == null ||
                questions.isEmpty()) {

            return counts;
        }

        for (Question question : questions) {

            if (question == null) {

                continue;
            }

            counts.total++;

            QuestionDifficulty difficulty = question.getDifficulty();

            if (difficulty == QuestionDifficulty.EASY) {

                counts.easy++;

            } else if (difficulty == QuestionDifficulty.MEDIUM) {

                counts.medium++;

            } else if (difficulty == QuestionDifficulty.HARD) {

                counts.hard++;
            }
        }

        return counts;
    }

    // ==========================================================
    // FIND TOPIC
    // ==========================================================

    private Topic findTopic(
            Long topicId) {

        if (topicId == null) {

            throw new IllegalArgumentException(
                    "Topic ID is required");
        }

        return topicRepository
                .findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found: "
                                + topicId));
    }

    // ==========================================================
    // TARGET FOR DIFFICULTY
    // ==========================================================

    private int getTarget(
            QuestionDifficulty difficulty) {

        if (difficulty == QuestionDifficulty.EASY) {

            return EASY_TARGET;
        }

        if (difficulty == QuestionDifficulty.MEDIUM) {

            return MEDIUM_TARGET;
        }

        if (difficulty == QuestionDifficulty.HARD) {

            return HARD_TARGET;
        }

        throw new IllegalArgumentException(
                "Unsupported question difficulty: "
                        + difficulty);
    }

    // ==========================================================
    // SAFE DISPLAY ORDER
    // ==========================================================

    private int safeDisplayOrder(
            Subject subject) {

        if (subject == null ||
                subject.getDisplayOrder() == null) {

            return Integer.MAX_VALUE;
        }

        return subject.getDisplayOrder();
    }

    private int safeDisplayOrder(
            Topic topic) {

        if (topic == null ||
                topic.getDisplayOrder() == null) {

            return Integer.MAX_VALUE;
        }

        return topic.getDisplayOrder();
    }

    // ==========================================================
    // INTEGER VALUE
    // ==========================================================

    private int integerValue(
            Object value) {

        if (value instanceof Number number) {

            return number.intValue();
        }

        return 0;
    }

    // ==========================================================
    // TOPIC QUESTION COUNTS
    // ==========================================================

    private static class TopicQuestionCounts {

        private int total;

        private int easy;

        private int medium;

        private int hard;

        private int getCount(
                QuestionDifficulty difficulty) {

            if (difficulty == QuestionDifficulty.EASY) {

                return easy;
            }

            if (difficulty == QuestionDifficulty.MEDIUM) {

                return medium;
            }

            if (difficulty == QuestionDifficulty.HARD) {

                return hard;
            }

            return 0;
        }
    }
}