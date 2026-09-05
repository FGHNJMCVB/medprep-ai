package com.medprep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medprep.dto.QuestionGenerationRequest;
import com.medprep.entity.Question;
import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Topic;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopicQuestionBankService {

    /*
     * Keep Gemini calls small.
     *
     * The existing AI generation service supports larger requests,
     * but small batches are safer for reliability and duplicate handling.
     */
    private static final int GENERATION_BATCH_SIZE = 3;

    /*
     * Long-term topic target:
     *
     * EASY   = 25%
     * MEDIUM = 50%
     * HARD   = 25%
     *
     * For target 20:
     *
     * EASY   = 5
     * MEDIUM = 10
     * HARD   = 5
     */
    private static final int EASY_PERCENT = 25;
    private static final int MEDIUM_PERCENT = 50;
    private static final int HARD_PERCENT = 25;

    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final AiQuestionGenerationService aiQuestionGenerationService;
    private final QuestionTrendService questionTrendService;
    private final ObjectMapper objectMapper;

    public TopicQuestionBankService(
            TopicRepository topicRepository,
            QuestionRepository questionRepository,
            AiQuestionGenerationService aiQuestionGenerationService,
            QuestionTrendService questionTrendService,
            ObjectMapper objectMapper) {

        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.aiQuestionGenerationService =
                aiQuestionGenerationService;
        this.questionTrendService =
                questionTrendService;
        this.objectMapper = objectMapper;
    }

    // ==========================================================
    // GET ALL TOPIC STATUS
    // ==========================================================

    @Transactional(readOnly = true)
    public List<TopicStatus> getAllTopicStatus(
            int targetQuestions) {

        validateTarget(targetQuestions);

        List<Topic> topics =
                new ArrayList<>(
                        topicRepository.findAll()
                );

        topics.sort(
                (first, second) -> {

                    if(first.getSubject() == null ||
                            second.getSubject() == null) {

                        return Long.compare(
                                first.getId(),
                                second.getId()
                        );
                    }

                    int subjectOrder =
                            Integer.compare(
                                    first.getSubject()
                                            .getDisplayOrder(),
                                    second.getSubject()
                                            .getDisplayOrder()
                            );

                    if(subjectOrder != 0) {
                        return subjectOrder;
                    }

                    return Integer.compare(
                            first.getDisplayOrder(),
                            second.getDisplayOrder()
                    );
                }
        );

        List<TopicStatus> result =
                new ArrayList<>();

        for(Topic topic : topics) {

            result.add(
                    buildTopicStatus(
                            topic,
                            targetQuestions
                    )
            );
        }

        return result;
    }

    // ==========================================================
    // GET SINGLE TOPIC STATUS
    // ==========================================================

    @Transactional(readOnly = true)
    public TopicStatus getTopicStatus(
            Long topicId,
            int targetQuestions) {

        validateTarget(targetQuestions);

        Topic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Topic not found: "
                                                + topicId
                                )
                        );

        return buildTopicStatus(
                topic,
                targetQuestions
        );
    }

    // ==========================================================
    // FILL ONE TOPIC
    // ==========================================================

    public TopicFillResult fillTopic(
            Long topicId,
            int targetQuestions) {

        validateTarget(targetQuestions);

        Topic topic =
                topicRepository
                        .findById(topicId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Topic not found: "
                                                + topicId
                                )
                        );

        int initialCount =
                getActiveQuestions(topic.getId())
                        .size();

        Map<QuestionDifficulty, Integer>
                generatedByDifficulty =
                new EnumMap<>(
                        QuestionDifficulty.class
                );

        for(QuestionDifficulty difficulty :
                QuestionDifficulty.values()) {

            generatedByDifficulty.put(
                    difficulty,
                    0
            );
        }

        String error = null;

        try {

            for(QuestionDifficulty difficulty :
                    QuestionDifficulty.values()) {

                int difficultyTarget =
                        getDifficultyTarget(
                                targetQuestions,
                                difficulty
                        );

                int currentCount =
                        countQuestionsByDifficulty(
                                topic.getId(),
                                difficulty
                        );

                int missing =
                        Math.max(
                                0,
                                difficultyTarget - currentCount
                        );

                if(missing <= 0) {
                    continue;
                }

                int generated =
                        generateMissingQuestions(
                                topic,
                                difficulty,
                                missing
                        );

                generatedByDifficulty.put(
                        difficulty,
                        generated
                );
            }

        } catch(Exception exception) {

            error =
                    exception.getMessage();

            if(error == null ||
                    error.trim().isEmpty()) {

                error =
                        exception.getClass()
                                .getSimpleName();
            }
        }

        int finalCount =
                getActiveQuestions(topic.getId())
                        .size();

        TopicStatus status =
                buildTopicStatus(
                        topic,
                        targetQuestions
                );

        return new TopicFillResult(
                topic.getId(),
                topic.getName(),
                topic.getSubject() == null
                        ? null
                        : topic.getSubject().getId(),
                initialCount,
                finalCount,
                finalCount - initialCount,
                status.getMissingQuestions(),
                generatedByDifficulty.get(
                        QuestionDifficulty.EASY
                ),
                generatedByDifficulty.get(
                        QuestionDifficulty.MEDIUM
                ),
                generatedByDifficulty.get(
                        QuestionDifficulty.HARD
                ),
                status.getEasyQuestions(),
                status.getMediumQuestions(),
                status.getHardQuestions(),
                status.isComplete(),
                error
        );
    }

    // ==========================================================
    // FILL ALL TOPICS
    // ==========================================================

    public List<TopicFillResult> fillAllTopics(
            int targetQuestions) {

        validateTarget(targetQuestions);

        List<Topic> topics =
                new ArrayList<>(
                        topicRepository.findAll()
                );

        topics.sort(
                (first, second) -> {

                    if(first.getSubject() == null ||
                            second.getSubject() == null) {

                        return Long.compare(
                                first.getId(),
                                second.getId()
                        );
                    }

                    int subjectOrder =
                            Integer.compare(
                                    first.getSubject()
                                            .getDisplayOrder(),
                                    second.getSubject()
                                            .getDisplayOrder()
                            );

                    if(subjectOrder != 0) {
                        return subjectOrder;
                    }

                    return Integer.compare(
                            first.getDisplayOrder(),
                            second.getDisplayOrder()
                    );
                }
        );

        List<TopicFillResult> results =
                new ArrayList<>();

        for(Topic topic : topics) {

            results.add(
                    fillTopic(
                            topic.getId(),
                            targetQuestions
                    )
            );
        }

        return results;
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS
    // ==========================================================

    private int generateMissingQuestions(
            Topic topic,
            QuestionDifficulty difficulty,
            int missing) {

        int totalGenerated = 0;

        while(missing > 0) {

            int batchSize =
                    Math.min(
                            missing,
                            GENERATION_BATCH_SIZE
                    );

            QuestionGenerationRequest request =
                    buildGenerationRequest(
                            topic,
                            difficulty,
                            batchSize
                    );

            List<Long> generatedQuestionIds =
                    aiQuestionGenerationService
                            .generateAndSaveQuestions(
                                    request
                            );

            int generated =
                    generatedQuestionIds == null
                            ? 0
                            : generatedQuestionIds.size();

            totalGenerated += generated;

            /*
             * Prevent an infinite loop when Gemini returns only
             * duplicates or otherwise saves zero new questions.
             */
            if(generated <= 0) {
                break;
            }

            missing -= generated;
        }

        return totalGenerated;
    }

    // ==========================================================
    // BUILD GENERATION REQUEST
    // ==========================================================

    private QuestionGenerationRequest buildGenerationRequest(
            Topic topic,
            QuestionDifficulty difficulty,
            int count) {

        boolean highYield =
                isHighYield(topic);

        String focus =
                buildFocus(topic);

        Map<String, Object> values =
                new LinkedHashMap<>();

        /*
         * We deliberately send topicId only.
         *
         * AiQuestionGenerationService already supports
         * topic-specific generation and uses topic trends.
         */
        values.put(
                "subjectId",
                null
        );

        values.put(
                "topicId",
                topic.getId()
        );

        values.put(
                "count",
                count
        );

        values.put(
                "difficulty",
                difficulty.name()
        );

        values.put(
                "highYield",
                highYield
        );

        values.put(
                "clinicalCase",
                shouldPreferClinicalQuestions(
                        topic
                )
        );

        values.put(
                "imageBased",
                false
        );

        values.put(
                "focus",
                focus
        );

        return objectMapper.convertValue(
                values,
                QuestionGenerationRequest.class
        );
    }

    // ==========================================================
    // BUILD TOPIC FOCUS
    // ==========================================================

    private String buildFocus(
            Topic topic) {

        StringBuilder focus =
                new StringBuilder();

        focus.append(
                "Build a high-quality FMGE question bank "
        );

        focus.append(
                "specifically for the topic "
        );

        focus.append(
                topic.getName()
        );

        focus.append(
                ". Cover distinct concepts within this topic. "
        );

        focus.append(
                "Do not repeat the same fact with different wording. "
        );

        focus.append(
                "Prefer clinically useful, exam-relevant concepts. "
        );

        focus.append(
                "Use the supplied trend context to prioritize "
                        + "important recurring concepts."
        );

        return focus.toString();
    }

    // ==========================================================
    // HIGH-YIELD DETECTION
    // ==========================================================

    private boolean isHighYield(
            Topic topic) {

        List<QuestionTrend> trends =
                questionTrendService
                        .getTopicTrends(
                                topic.getId()
                        );

        if(trends == null ||
                trends.isEmpty()) {

            return false;
        }

        for(QuestionTrend trend : trends) {

            if(trend != null &&
                    Boolean.TRUE.equals(
                            trend.getHighYield()
                    )) {

                return true;
            }
        }

        return false;
    }

    // ==========================================================
    // CLINICAL QUESTION PREFERENCE
    // ==========================================================

    private boolean shouldPreferClinicalQuestions(
            Topic topic) {

        List<QuestionTrend> trends =
                questionTrendService
                        .getTopicTrends(
                                topic.getId()
                        );

        if(trends == null ||
                trends.isEmpty()) {

            return true;
        }

        double clinicalWeight = 0.0;

        int count = 0;

        for(QuestionTrend trend : trends) {

            if(trend == null ||
                    trend.getClinicalWeight() == null) {

                continue;
            }

            clinicalWeight +=
                    trend.getClinicalWeight();

            count++;
        }

        if(count == 0) {
            return true;
        }

        return (clinicalWeight / count) >= 0.50;
    }

    // ==========================================================
    // ACTIVE QUESTIONS
    // ==========================================================

    private List<Question> getActiveQuestions(
            Long topicId) {

        return questionRepository
                .findByTopicIdAndActiveTrueOrderByIdAsc(
                        topicId
                );
    }

    // ==========================================================
    // COUNT BY DIFFICULTY
    // ==========================================================

    private int countQuestionsByDifficulty(
            Long topicId,
            QuestionDifficulty difficulty) {

        int count = 0;

        for(Question question :
                getActiveQuestions(topicId)) {

            if(question == null) {
                continue;
            }

            if(question.getDifficulty() ==
                    difficulty) {

                count++;
            }
        }

        return count;
    }

    // ==========================================================
    // DIFFICULTY TARGET
    // ==========================================================

    private int getDifficultyTarget(
            int total,
            QuestionDifficulty difficulty) {

        if(difficulty ==
                QuestionDifficulty.EASY) {

            return Math.round(
                    total * EASY_PERCENT / 100.0f
            );
        }

        if(difficulty ==
                QuestionDifficulty.MEDIUM) {

            return Math.round(
                    total * MEDIUM_PERCENT / 100.0f
            );
        }

        /*
         * HARD gets the remainder so the three targets
         * always add up exactly to total.
         */
        return total
                - Math.round(
                        total * EASY_PERCENT / 100.0f
                )
                - Math.round(
                        total * MEDIUM_PERCENT / 100.0f
                );
    }

    // ==========================================================
    // BUILD TOPIC STATUS
    // ==========================================================

    private TopicStatus buildTopicStatus(
            Topic topic,
            int targetQuestions) {

        int easy =
                countQuestionsByDifficulty(
                        topic.getId(),
                        QuestionDifficulty.EASY
                );

        int medium =
                countQuestionsByDifficulty(
                        topic.getId(),
                        QuestionDifficulty.MEDIUM
                );

        int hard =
                countQuestionsByDifficulty(
                        topic.getId(),
                        QuestionDifficulty.HARD
                );

        int total =
                easy + medium + hard;

        int easyTarget =
                getDifficultyTarget(
                        targetQuestions,
                        QuestionDifficulty.EASY
                );

        int mediumTarget =
                getDifficultyTarget(
                        targetQuestions,
                        QuestionDifficulty.MEDIUM
                );

        int hardTarget =
                getDifficultyTarget(
                        targetQuestions,
                        QuestionDifficulty.HARD
                );

        int missing =
                Math.max(
                        0,
                        targetQuestions - total
                );

        return new TopicStatus(
                topic.getId(),
                topic.getName(),
                topic.getSubject() == null
                        ? null
                        : topic.getSubject().getId(),
                topic.getSubject() == null
                        ? null
                        : topic.getSubject().getName(),
                total,
                targetQuestions,
                missing,
                easy,
                medium,
                hard,
                easyTarget,
                mediumTarget,
                hardTarget,
                easy >= easyTarget &&
                        medium >= mediumTarget &&
                        hard >= hardTarget
        );
    }

    // ==========================================================
    // VALIDATE TARGET
    // ==========================================================

    private void validateTarget(
            int targetQuestions) {

        if(targetQuestions <= 0) {

            throw new IllegalArgumentException(
                    "Target questions must be greater than zero"
            );
        }

        if(targetQuestions > 100) {

            throw new IllegalArgumentException(
                    "Target questions cannot exceed 100"
            );
        }
    }

    // ==========================================================
    // TOPIC STATUS
    // ==========================================================

    public static class TopicStatus {

        private final Long topicId;
        private final String topicName;
        private final Long subjectId;
        private final String subjectName;

        private final int totalQuestions;
        private final int targetQuestions;
        private final int missingQuestions;

        private final int easyQuestions;
        private final int mediumQuestions;
        private final int hardQuestions;

        private final int easyTarget;
        private final int mediumTarget;
        private final int hardTarget;

        private final boolean complete;

        public TopicStatus(
                Long topicId,
                String topicName,
                Long subjectId,
                String subjectName,
                int totalQuestions,
                int targetQuestions,
                int missingQuestions,
                int easyQuestions,
                int mediumQuestions,
                int hardQuestions,
                int easyTarget,
                int mediumTarget,
                int hardTarget,
                boolean complete) {

            this.topicId = topicId;
            this.topicName = topicName;
            this.subjectId = subjectId;
            this.subjectName = subjectName;
            this.totalQuestions = totalQuestions;
            this.targetQuestions = targetQuestions;
            this.missingQuestions = missingQuestions;
            this.easyQuestions = easyQuestions;
            this.mediumQuestions = mediumQuestions;
            this.hardQuestions = hardQuestions;
            this.easyTarget = easyTarget;
            this.mediumTarget = mediumTarget;
            this.hardTarget = hardTarget;
            this.complete = complete;
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

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getTargetQuestions() {
            return targetQuestions;
        }

        public int getMissingQuestions() {
            return missingQuestions;
        }

        public int getEasyQuestions() {
            return easyQuestions;
        }

        public int getMediumQuestions() {
            return mediumQuestions;
        }

        public int getHardQuestions() {
            return hardQuestions;
        }

        public int getEasyTarget() {
            return easyTarget;
        }

        public int getMediumTarget() {
            return mediumTarget;
        }

        public int getHardTarget() {
            return hardTarget;
        }

        public boolean isComplete() {
            return complete;
        }
    }

    // ==========================================================
    // FILL RESULT
    // ==========================================================

    public static class TopicFillResult {

        private final Long topicId;
        private final String topicName;
        private final Long subjectId;

        private final int initialQuestions;
        private final int finalQuestions;
        private final int generatedQuestions;
        private final int remainingQuestions;

        private final int generatedEasy;
        private final int generatedMedium;
        private final int generatedHard;

        private final int finalEasy;
        private final int finalMedium;
        private final int finalHard;

        private final boolean complete;

        private final String error;

        public TopicFillResult(
                Long topicId,
                String topicName,
                Long subjectId,
                int initialQuestions,
                int finalQuestions,
                int generatedQuestions,
                int remainingQuestions,
                int generatedEasy,
                int generatedMedium,
                int generatedHard,
                int finalEasy,
                int finalMedium,
                int finalHard,
                boolean complete,
                String error) {

            this.topicId = topicId;
            this.topicName = topicName;
            this.subjectId = subjectId;
            this.initialQuestions = initialQuestions;
            this.finalQuestions = finalQuestions;
            this.generatedQuestions = generatedQuestions;
            this.remainingQuestions = remainingQuestions;
            this.generatedEasy = generatedEasy;
            this.generatedMedium = generatedMedium;
            this.generatedHard = generatedHard;
            this.finalEasy = finalEasy;
            this.finalMedium = finalMedium;
            this.finalHard = finalHard;
            this.complete = complete;
            this.error = error;
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

        public int getInitialQuestions() {
            return initialQuestions;
        }

        public int getFinalQuestions() {
            return finalQuestions;
        }

        public int getGeneratedQuestions() {
            return generatedQuestions;
        }

        public int getRemainingQuestions() {
            return remainingQuestions;
        }

        public int getGeneratedEasy() {
            return generatedEasy;
        }

        public int getGeneratedMedium() {
            return generatedMedium;
        }

        public int getGeneratedHard() {
            return generatedHard;
        }

        public int getFinalEasy() {
            return finalEasy;
        }

        public int getFinalMedium() {
            return finalMedium;
        }

        public int getFinalHard() {
            return finalHard;
        }

        public boolean isComplete() {
            return complete;
        }

        public String getError() {
            return error;
        }
    }
}