package com.medprep.service;

import com.medprep.dto.QuestionGenerationRequest;
import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class QuestionBankExpansionService {

    private static final int TARGET_EASY = 60;
    private static final int TARGET_MEDIUM = 180;
    private static final int TARGET_HARD = 60;

    private static final int MAX_AI_BATCH_SIZE = 10;

    private final QuestionRepository questionRepository;

    private final SubjectRepository subjectRepository;

    private final TopicRepository topicRepository;

    private final QuestionTrendService questionTrendService;

    private final AiQuestionGenerationService aiQuestionGenerationService;

    public QuestionBankExpansionService(
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionTrendService questionTrendService,
            AiQuestionGenerationService aiQuestionGenerationService) {

        this.questionRepository = questionRepository;

        this.subjectRepository = subjectRepository;

        this.topicRepository = topicRepository;

        this.questionTrendService = questionTrendService;

        this.aiQuestionGenerationService = aiQuestionGenerationService;
    }

    // ==========================================================
    // EXPAND QUESTION BANK
    // ==========================================================

    public ExpansionResult expandQuestionBank(
            int maxQuestionsThisRun) {

        if (maxQuestionsThisRun <= 0) {

            throw new IllegalArgumentException(
                    "Maximum questions for this run must be greater than zero");
        }

        int remainingBudget = maxQuestionsThisRun;

        int easyCreated = 0;

        int mediumCreated = 0;

        int hardCreated = 0;

        while (remainingBudget > 0) {

            DifficultyNeed need = findLargestDifficultyNeed();

            if (need == null) {
                break;
            }

            int batchSize = Math.min(
                    MAX_AI_BATCH_SIZE,
                    Math.min(
                            need.remaining,
                            remainingBudget));

            List<Long> questionIds = generateBatch(
                    need.difficulty,
                    batchSize);

            int accepted = questionIds.size();

            if (accepted <= 0) {

                throw new IllegalStateException(
                        "No questions were generated for difficulty "
                                + need.difficulty);
            }

            switch (need.difficulty) {

                case EASY:

                    easyCreated += accepted;

                    break;

                case MEDIUM:

                    mediumCreated += accepted;

                    break;

                case HARD:

                    hardCreated += accepted;

                    break;

                default:

                    throw new IllegalStateException(
                            "Unsupported difficulty: "
                                    + need.difficulty);
            }

            remainingBudget -= accepted;
        }

        return buildResult(
                easyCreated,
                mediumCreated,
                hardCreated);
    }

    // ==========================================================
    // GENERATE ONE DIFFICULTY BATCH
    // ==========================================================

    private List<Long> generateBatch(
            QuestionDifficulty difficulty,
            int count) {

        TopicSelection selection = selectTopic(difficulty);

        QuestionGenerationRequest request = new QuestionGenerationRequest();

        request.setSubjectId(
                selection.subject.getId());

        request.setTopicId(
                selection.topic.getId());

        request.setCount(
                count);

        request.setDifficulty(
                difficulty);

        request.setHighYield(
                selection.highYield);

        request.setClinicalCase(
                true);

        request.setImageBased(
                false);

        StringBuilder focus = new StringBuilder();

        focus.append(
                "Generate original FMGE-aligned medical "
                        + "multiple-choice questions for "
                        + selection.subject.getName()
                        + " - "
                        + selection.topic.getName()
                        + ". ");

        focus.append(
                "All questions must be "
                        + difficulty.name()
                        + " difficulty. ");

        focus.append(
                "Generate exactly "
                        + count
                        + " questions. ");

        focus.append(
                "Do not repeat question wording, facts, "
                        + "or question patterns within the batch. ");

        focus.append(
                "Every question must contain exactly four "
                        + "options and exactly one correct answer. ");

        focus.append(
                "Questions must be original and must not claim "
                        + "to be official past examination questions. ");

        if (selection.trend != null) {

            focus.append(
                    "Prioritize this recurring concept: "
                            + selection.trend.getConceptTag()
                            + ". ");

            focus.append(
                    "Use the trend data only as a topic-priority "
                            + "signal, not as proof that an exact "
                            + "question appeared in an examination. ");
        }

        request.setFocus(
                focus.toString());

        return aiQuestionGenerationService
                .generateAndSaveQuestions(
                        request);
    }

    // ==========================================================
    // DETERMINE LARGEST DEFICIT
    // ==========================================================

    private DifficultyNeed findLargestDifficultyNeed() {

        int easyCurrent = getCurrentCount(
                QuestionDifficulty.EASY);

        int mediumCurrent = getCurrentCount(
                QuestionDifficulty.MEDIUM);

        int hardCurrent = getCurrentCount(
                QuestionDifficulty.HARD);

        int easyNeed = Math.max(
                0,
                TARGET_EASY - easyCurrent);

        int mediumNeed = Math.max(
                0,
                TARGET_MEDIUM - mediumCurrent);

        int hardNeed = Math.max(
                0,
                TARGET_HARD - hardCurrent);

        DifficultyNeed largest = null;

        double largestRatio = -1.0;

        // ----------------------------------------------------------
        // EASY deficit ratio
        // ----------------------------------------------------------

        if (easyNeed > 0) {

            double ratio = (double) easyNeed
                    / TARGET_EASY;

            if (ratio > largestRatio) {

                largestRatio = ratio;

                largest = new DifficultyNeed(
                        QuestionDifficulty.EASY,
                        easyNeed);
            }
        }

        // ----------------------------------------------------------
        // MEDIUM deficit ratio
        // ----------------------------------------------------------

        if (mediumNeed > 0) {

            double ratio = (double) mediumNeed
                    / TARGET_MEDIUM;

            if (ratio > largestRatio) {

                largestRatio = ratio;

                largest = new DifficultyNeed(
                        QuestionDifficulty.MEDIUM,
                        mediumNeed);
            }
        }

        // ----------------------------------------------------------
        // HARD deficit ratio
        // ----------------------------------------------------------

        if (hardNeed > 0) {

            double ratio = (double) hardNeed
                    / TARGET_HARD;

            if (ratio > largestRatio) {

                largest = new DifficultyNeed(
                        QuestionDifficulty.HARD,
                        hardNeed);
            }
        }

        return largest;
    }

    // ==========================================================
    // CURRENT DIFFICULTY COUNT
    // ==========================================================

    private int getCurrentCount(
            QuestionDifficulty difficulty) {

        return (int) questionRepository
                .countByDifficultyAndActiveTrue(
                        difficulty);
    }

    // ==========================================================
    // SELECT SUBJECT / TOPIC
    // ==========================================================

    private TopicSelection selectTopic(
            QuestionDifficulty difficulty) {

        List<Subject> subjects = new ArrayList<>(
                subjectRepository.findAll());

        if (subjects.isEmpty()) {

            throw new IllegalStateException(
                    "No subjects configured");
        }

        subjects.sort(
                Comparator.comparing(
                        Subject::getDisplayOrder));

        TopicSelection bestSelection = null;

        double bestScore = Double.NEGATIVE_INFINITY;

        for (Subject subject : subjects) {

            List<Topic> topics = topicRepository
                    .findBySubjectIdOrderByDisplayOrderAsc(
                            subject.getId());

            if (topics == null ||
                    topics.isEmpty()) {

                continue;
            }

            List<QuestionTrend> trends = questionTrendService
                    .getSubjectTrends(
                            subject.getId());

            if (trends == null) {

                trends = new ArrayList<>();
            }

            for (Topic topic : topics) {

                QuestionTrend matchingTrend = findMatchingTrend(
                        topic,
                        trends);

                int existingCount = countExistingQuestions(
                        topic,
                        difficulty);

                double trendScore = matchingTrend != null
                        ? safeTrendScore(
                                matchingTrend)
                        : 0.01;

                /*
                 * Prefer:
                 *
                 * 1. Strong trend score.
                 * 2. Topics with fewer existing questions.
                 *
                 * This prevents the expansion process from
                 * concentrating everything into one topic.
                 */

                double scarcityScore = 1.0
                        / (1.0
                                + existingCount);

                double score = (trendScore * 0.70)
                        + (scarcityScore * 0.30);

                if (bestSelection == null ||
                        score > bestScore) {

                    bestScore = score;

                    bestSelection = new TopicSelection(
                            subject,
                            topic,
                            matchingTrend,
                            matchingTrend != null
                                    ? Boolean.TRUE.equals(
                                            matchingTrend
                                                    .getHighYield())
                                    : true);
                }
            }
        }

        if (bestSelection == null) {

            throw new IllegalStateException(
                    "Unable to select a subject/topic for question generation");
        }

        return bestSelection;
    }

    // ==========================================================
    // MATCH TREND TO TOPIC
    // ==========================================================

    private QuestionTrend findMatchingTrend(
            Topic topic,
            List<QuestionTrend> trends) {

        for (QuestionTrend trend : trends) {

            if (trend == null ||
                    trend.getTopic() == null) {

                continue;
            }

            if (trend.getTopic()
                    .getId()
                    .equals(topic.getId())) {

                return trend;
            }
        }

        return null;
    }

    // ==========================================================
    // COUNT EXISTING TOPIC QUESTIONS
    // ==========================================================

    private int countExistingQuestions(
            Topic topic,
            QuestionDifficulty difficulty) {

        return (int) questionRepository
                .findByTopicIdAndDifficultyAndActiveTrueOrderByIdAsc(
                        topic.getId(),
                        difficulty)
                .size();
    }

    // ==========================================================
    // SAFE TREND SCORE
    // ==========================================================

    private double safeTrendScore(
            QuestionTrend trend) {

        if (trend == null ||
                trend.getTrendScore() == null) {

            return 0.0;
        }

        return Math.max(
                0.0,
                trend.getTrendScore());
    }

    // ==========================================================
    // BUILD RESULT
    // ==========================================================

    private ExpansionResult buildResult(
            int easyCreated,
            int mediumCreated,
            int hardCreated) {

        int currentEasy = getCurrentCount(
                QuestionDifficulty.EASY);

        int currentMedium = getCurrentCount(
                QuestionDifficulty.MEDIUM);

        int currentHard = getCurrentCount(
                QuestionDifficulty.HARD);

        return new ExpansionResult(
                easyCreated,
                mediumCreated,
                hardCreated,
                currentEasy,
                currentMedium,
                currentHard,
                TARGET_EASY,
                TARGET_MEDIUM,
                TARGET_HARD);
    }

    // ==========================================================
    // RESULT
    // ==========================================================

    public static class ExpansionResult {

        private final int easyCreated;
        private final int mediumCreated;
        private final int hardCreated;

        private final int currentEasy;
        private final int currentMedium;
        private final int currentHard;

        private final int targetEasy;
        private final int targetMedium;
        private final int targetHard;

        public ExpansionResult(
                int easyCreated,
                int mediumCreated,
                int hardCreated,
                int currentEasy,
                int currentMedium,
                int currentHard,
                int targetEasy,
                int targetMedium,
                int targetHard) {

            this.easyCreated = easyCreated;

            this.mediumCreated = mediumCreated;

            this.hardCreated = hardCreated;

            this.currentEasy = currentEasy;

            this.currentMedium = currentMedium;

            this.currentHard = currentHard;

            this.targetEasy = targetEasy;

            this.targetMedium = targetMedium;

            this.targetHard = targetHard;
        }

        public int getEasyCreated() {
            return easyCreated;
        }

        public int getMediumCreated() {
            return mediumCreated;
        }

        public int getHardCreated() {
            return hardCreated;
        }

        public int getCurrentEasy() {
            return currentEasy;
        }

        public int getCurrentMedium() {
            return currentMedium;
        }

        public int getCurrentHard() {
            return currentHard;
        }

        public int getTargetEasy() {
            return targetEasy;
        }

        public int getTargetMedium() {
            return targetMedium;
        }

        public int getTargetHard() {
            return targetHard;
        }

        public int getTotalCurrent() {
            return currentEasy
                    + currentMedium
                    + currentHard;
        }

        public int getTotalTarget() {
            return targetEasy
                    + targetMedium
                    + targetHard;
        }

        public boolean isComplete() {

            return currentEasy >= targetEasy
                    && currentMedium >= targetMedium
                    && currentHard >= targetHard;
        }
    }

    // ==========================================================
    // INTERNAL DIFFICULTY NEED
    // ==========================================================

    private static class DifficultyNeed {

        private final QuestionDifficulty difficulty;

        private final int remaining;

        private DifficultyNeed(
                QuestionDifficulty difficulty,
                int remaining) {

            this.difficulty = difficulty;

            this.remaining = remaining;
        }
    }

    // ==========================================================
    // INTERNAL TOPIC SELECTION
    // ==========================================================

    private static class TopicSelection {

        private final Subject subject;

        private final Topic topic;

        private final QuestionTrend trend;

        private final boolean highYield;

        private TopicSelection(
                Subject subject,
                Topic topic,
                QuestionTrend trend,
                boolean highYield) {

            this.subject = subject;

            this.topic = topic;

            this.trend = trend;

            this.highYield = highYield;
        }
    }
}