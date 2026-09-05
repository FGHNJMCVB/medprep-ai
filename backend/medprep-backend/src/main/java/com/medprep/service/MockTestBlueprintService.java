package com.medprep.service;

import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;

import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockTestBlueprintService {

    private final SubjectRepository subjectRepository;

    private final TopicRepository topicRepository;

    private final QuestionTrendService questionTrendService;

    public MockTestBlueprintService(
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionTrendService questionTrendService) {

        this.subjectRepository =
                subjectRepository;

        this.topicRepository =
                topicRepository;

        this.questionTrendService =
                questionTrendService;
    }

    // ==========================================================
    // BUILD COMPLETE EXAM BLUEPRINT
    // ==========================================================

    public List<GenerationBlock> buildBlueprint(
            int totalQuestions) {

        if(totalQuestions <= 0) {

            throw new IllegalArgumentException(
                    "Total questions must be greater than zero"
            );
        }

        List<Subject> subjects =
                new ArrayList<>(
                        subjectRepository.findAll()
                );

        subjects.sort(
                Comparator.comparing(
                        Subject::getDisplayOrder
                )
        );

        List<GenerationBlock> blueprint =
                new ArrayList<>();

        int remainingQuestions =
                totalQuestions;

        int globalQuestionIndex =
                0;

        /*
         * Create the exact difficulty targets for the
         * complete exam.
         *
         * 300 questions:
         *
         * EASY   = 60
         * MEDIUM = 180
         * HARD   = 60
         *
         * 5 questions:
         *
         * EASY   = 1
         * MEDIUM = 3
         * HARD   = 1
         */
        DifficultyTargets difficultyTargets =
                createDifficultyTargets(
                        totalQuestions
                );

        // ------------------------------------------------------
        // Allocate subject questions.
        // ------------------------------------------------------

        for(Subject subject :
                subjects) {

            if(remainingQuestions <= 0) {
                break;
            }

            int subjectQuestions =
                    Math.min(
                            subject.getWeightageMarks(),
                            remainingQuestions
                    );

            if(subjectQuestions <= 0) {
                continue;
            }

            List<QuestionTrend> trends =
                    getRankedSubjectTrends(
                            subject
                    );

            /*
             * Try trend-based topic allocation first.
             *
             * IMPORTANT:
             * The trend path now considers every configured
             * topic for the subject instead of only the top 5.
             *
             * Every topic receives at least one question when
             * the subject has enough questions allocated for all
             * of its topics.
             */
            if(!trends.isEmpty()) {

                int blocksBefore =
                        blueprint.size();

                globalQuestionIndex =
                        addTrendWeightedBlocks(
                                subject,
                                subjectQuestions,
                                trends,
                                blueprint,
                                globalQuestionIndex,
                                difficultyTargets
                        );

                if(blueprint.size() > blocksBefore) {

                    remainingQuestions -=
                            subjectQuestions;

                    continue;
                }
            }

            // --------------------------------------------------
            // Fallback when no trend data exists.
            // --------------------------------------------------

            globalQuestionIndex =
                    addFallbackBlocks(
                            subject,
                            subjectQuestions,
                            blueprint,
                            globalQuestionIndex,
                            difficultyTargets
                    );

            remainingQuestions -=
                    subjectQuestions;
        }

        // ------------------------------------------------------
        // All requested questions must be allocated.
        // ------------------------------------------------------

        if(remainingQuestions != 0) {

            throw new IllegalStateException(
                    "Blueprint could not allocate all questions. "
                            + "Remaining: "
                            + remainingQuestions
            );
        }

        // ------------------------------------------------------
        // Difficulty distribution must be exact.
        // ------------------------------------------------------

        if(difficultyTargets.easyRemaining != 0 ||
                difficultyTargets.mediumRemaining != 0 ||
                difficultyTargets.hardRemaining != 0) {

            throw new IllegalStateException(
                    "Difficulty blueprint is not balanced. "
                            + "EASY remaining: "
                            + difficultyTargets.easyRemaining
                            + ", MEDIUM remaining: "
                            + difficultyTargets.mediumRemaining
                            + ", HARD remaining: "
                            + difficultyTargets.hardRemaining
            );
        }

        // ------------------------------------------------------
        // Final question-count sanity check.
        // ------------------------------------------------------

        int blueprintQuestionCount =
                blueprint.stream()
                        .mapToInt(
                                GenerationBlock::getQuestionCount
                        )
                        .sum();

        if(blueprintQuestionCount != totalQuestions) {

            throw new IllegalStateException(
                    "Blueprint question count mismatch. "
                            + "Required: "
                            + totalQuestions
                            + ", Planned: "
                            + blueprintQuestionCount
            );
        }

        return blueprint;
    }

    // ==========================================================
    // CREATE EXACT DIFFICULTY TARGETS
    // ==========================================================

    private DifficultyTargets createDifficultyTargets(
            int totalQuestions) {

        int easy =
                (int)Math.floor(
                        totalQuestions * 0.20
                );

        int hard =
                (int)Math.floor(
                        totalQuestions * 0.20
                );

        int medium =
                totalQuestions
                        - easy
                        - hard;

        /*
         * For a very small development exam, make sure
         * that an exam of at least 3 questions contains
         * at least one EASY and one HARD question.
         *
         * Examples:
         *
         * 3 -> 1 / 1 / 1
         * 4 -> 1 / 2 / 1
         * 5 -> 1 / 3 / 1
         *
         * For 300 this remains:
         *
         * 60 / 180 / 60
         */
        if(totalQuestions >= 3) {

            if(easy == 0) {
                easy = 1;
            }

            if(hard == 0) {
                hard = 1;
            }

            medium =
                    totalQuestions
                            - easy
                            - hard;
        }

        return new DifficultyTargets(
                easy,
                medium,
                hard
        );
    }

    // ==========================================================
    // GET RANKED TRENDS
    // ==========================================================

    private List<QuestionTrend> getRankedSubjectTrends(
            Subject subject) {

        List<QuestionTrend> trends =
                questionTrendService
                        .getSubjectTrends(
                                subject.getId()
                        );

        if(trends == null ||
                trends.isEmpty()) {

            return new ArrayList<>();
        }

        List<QuestionTrend> ranked =
                new ArrayList<>();

        for(QuestionTrend trend :
                trends) {

            if(trend == null ||
                    trend.getTopic() == null) {

                continue;
            }

            boolean duplicateTopic =
                    false;

            for(QuestionTrend existing :
                    ranked) {

                if(existing.getTopic()
                        .getId()
                        .equals(
                                trend.getTopic().getId()
                        )) {

                    duplicateTopic = true;

                    break;
                }
            }

            if(!duplicateTopic) {

                ranked.add(
                        trend
                );
            }
        }

        ranked.sort(
                Comparator.comparing(
                        this::safeScore
                ).reversed()
        );

        return ranked;
    }

    // ==========================================================
    // TREND-WEIGHTED SUBJECT ALLOCATION
    // ==========================================================

    private int addTrendWeightedBlocks(
            Subject subject,
            int questionCount,
            List<QuestionTrend> trends,
            List<GenerationBlock> blueprint,
            int globalQuestionIndex,
            DifficultyTargets difficultyTargets) {

        /*
         * Load ALL configured topics for the subject.
         *
         * The previous implementation considered only the top
         * five trend topics. That meant topics outside the top
         * five could never receive generated questions through
         * the trend path.
         */
        List<Topic> allTopics =
                topicRepository
                        .findBySubjectIdOrderByDisplayOrderAsc(
                                subject.getId()
                        );

        if(allTopics == null ||
                allTopics.isEmpty()) {

            return globalQuestionIndex;
        }

        /*
         * We cannot give one question to every topic if the
         * subject itself has fewer questions allocated than
         * the number of configured topics.
         *
         * In that case, prioritize the highest trend-scoring
         * topics.
         *
         * Otherwise, every configured topic gets at least one.
         */
        int usableTopicCount =
                Math.min(
                        allTopics.size(),
                        questionCount
                );

        if(usableTopicCount <= 0) {

            return globalQuestionIndex;
        }

        List<TopicAllocation> allocations =
                createInitialAllocations(
                        allTopics,
                        trends,
                        usableTopicCount,
                        questionCount
                );

        for(TopicAllocation allocation :
                allocations) {

            if(allocation.questionCount <= 0) {
                continue;
            }

            int remaining =
                    allocation.questionCount;

            while(remaining > 0) {

                int batchSize =
                        Math.min(
                                remaining,
                                10
                        );

                globalQuestionIndex =
                        addDifficultyBlocks(
                                subject,
                                allocation,
                                batchSize,
                                blueprint,
                                globalQuestionIndex,
                                difficultyTargets
                        );

                remaining -=
                        batchSize;
            }
        }

        return globalQuestionIndex;
    }

    // ==========================================================
    // ADD DIFFICULTY BLOCKS
    // ==========================================================

    private int addDifficultyBlocks(
            Subject subject,
            TopicAllocation allocation,
            int count,
            List<GenerationBlock> blueprint,
            int globalQuestionIndex,
            DifficultyTargets difficultyTargets) {

        if(count <= 0) {

            return globalQuestionIndex;
        }

        int remaining =
                count;

        while(remaining > 0) {

            // --------------------------------------------------
            // EASY
            // --------------------------------------------------

            int easyCount =
                    Math.min(
                            remaining,
                            difficultyTargets.easyRemaining
                    );

            if(easyCount > 0) {

                blueprint.add(
                        createGenerationBlock(
                                subject,
                                allocation,
                                easyCount,
                                QuestionDifficulty.EASY,
                                globalQuestionIndex
                        )
                );

                remaining -=
                        easyCount;

                difficultyTargets.easyRemaining -=
                        easyCount;

                globalQuestionIndex +=
                        easyCount;
            }

            if(remaining <= 0) {
                break;
            }

            // --------------------------------------------------
            // MEDIUM
            // --------------------------------------------------

            int mediumCount =
                    Math.min(
                            remaining,
                            difficultyTargets.mediumRemaining
                    );

            if(mediumCount > 0) {

                blueprint.add(
                        createGenerationBlock(
                                subject,
                                allocation,
                                mediumCount,
                                QuestionDifficulty.MEDIUM,
                                globalQuestionIndex
                        )
                );

                remaining -=
                        mediumCount;

                difficultyTargets.mediumRemaining -=
                        mediumCount;

                globalQuestionIndex +=
                        mediumCount;
            }

            if(remaining <= 0) {
                break;
            }

            // --------------------------------------------------
            // HARD
            // --------------------------------------------------

            int hardCount =
                    Math.min(
                            remaining,
                            difficultyTargets.hardRemaining
                    );

            if(hardCount > 0) {

                blueprint.add(
                        createGenerationBlock(
                                subject,
                                allocation,
                                hardCount,
                                QuestionDifficulty.HARD,
                                globalQuestionIndex
                        )
                );

                remaining -=
                        hardCount;

                difficultyTargets.hardRemaining -=
                        hardCount;

                globalQuestionIndex +=
                        hardCount;
            }

            // --------------------------------------------------
            // Safety check.
            // --------------------------------------------------

            if(easyCount == 0 &&
                    mediumCount == 0 &&
                    hardCount == 0) {

                throw new IllegalStateException(
                        "Unable to allocate difficulty for "
                                + subject.getName()
                                + " - "
                                + allocation.topic.getName()
                );
            }
        }

        return globalQuestionIndex;
    }

    // ==========================================================
    // CREATE GENERATION BLOCK
    // ==========================================================

    private GenerationBlock createGenerationBlock(
            Subject subject,
            TopicAllocation allocation,
            int count,
            QuestionDifficulty difficulty,
            int globalQuestionIndex) {

        boolean clinicalCase =
                chooseClinicalBaseline(
                        globalQuestionIndex
                );

        boolean imageBased =
                chooseImageBaseline(
                        globalQuestionIndex
                );

        return new GenerationBlock(
                subject,
                allocation.topic,
                count,
                difficulty,
                clinicalCase,
                imageBased,
                allocation.trend,
                buildDifficultyProfile(
                        difficulty,
                        count
                ),
                buildStyleProfile()
        );
    }

    // ==========================================================
    // INITIAL TOPIC ALLOCATIONS
    // ==========================================================

    private List<TopicAllocation> createInitialAllocations(
            List<Topic> allTopics,
            List<QuestionTrend> trends,
            int topicCount,
            int questionCount) {

        List<TopicAllocation> allocations =
                new ArrayList<>();

        /*
         * Build a lookup from topic ID to trend.
         *
         * This allows every configured topic to participate
         * even when a topic has no QuestionTrend row.
         */
        Map<Long, QuestionTrend> trendByTopicId =
                new HashMap<>();

        for(QuestionTrend trend :
                trends) {

            if(trend == null ||
                    trend.getTopic() == null ||
                    trend.getTopic().getId() == null) {

                continue;
            }

            trendByTopicId.put(
                    trend.getTopic().getId(),
                    trend
            );
        }

        /*
         * First select the topics.
         *
         * If there are enough questions for all configured
         * topics, use every topic.
         *
         * If there are fewer questions than topics, choose
         * the highest-trend topics first.
         */
        List<Topic> selectedTopics =
                new ArrayList<>(
                        allTopics
                );

        if(questionCount < allTopics.size()) {

            selectedTopics.sort(
                    Comparator.comparing(
                           (Topic topic) -> safeScore(
                                    trendByTopicId.get(
                                            topic.getId()
                                    )
                            )
                    ).reversed()
            );

            selectedTopics =
                    new ArrayList<>(
                            selectedTopics.subList(
                                    0,
                                    topicCount
                            )
                    );

            /*
             * Restore normal display order after selecting
             * the highest-priority topics.
             */
            selectedTopics.sort(
                    Comparator.comparing(
                            Topic::getDisplayOrder
                    )
            );
        }

        /*
         * Every selected topic gets at least one question.
         */
        for(Topic topic :
                selectedTopics) {

            QuestionTrend trend =
                    trendByTopicId.get(
                            topic.getId()
                    );

            allocations.add(
                    new TopicAllocation(
                            topic,
                            trend,
                            1
                    )
            );
        }

        int selectedTopicCount =
                allocations.size();

        int remaining =
                questionCount
                        - selectedTopicCount;

        if(remaining <= 0) {

            return allocations;
        }

        /*
         * Distribute the remaining questions using trend
         * scores.
         *
         * Topics without trend data still receive a small
         * baseline weight so they are not completely ignored.
         */
        double totalScore =
                0.0;

        for(TopicAllocation allocation :
                allocations) {

            totalScore +=
                    Math.max(
                            safeScore(
                                    allocation.trend
                            ),
                            0.01
                    );
        }

        int allocated =
                0;

        List<Double> remainders =
                new ArrayList<>();

        for(TopicAllocation allocation :
                allocations) {

            double score =
                    Math.max(
                            safeScore(
                                    allocation.trend
                            ),
                            0.01
                    );

            double exact =
                    remaining
                            * score
                            / totalScore;

            int whole =
                    (int)Math.floor(
                            exact
                    );

            allocation.questionCount +=
                    whole;

            allocated +=
                    whole;

            remainders.add(
                    exact - whole
            );
        }

        /*
         * Largest remainder method makes sure every question
         * is assigned exactly once.
         */
        int leftover =
                remaining
                        - allocated;

        while(leftover > 0) {

            int bestIndex =
                    0;

            for(int i = 1;
                i < remainders.size();
                i++) {

                if(remainders.get(i) >
                        remainders.get(bestIndex)) {

                    bestIndex =
                            i;
                }
            }

            allocations
                    .get(bestIndex)
                    .questionCount++;

            remainders.set(
                    bestIndex,
                    -1.0
            );

            leftover--;
        }

        return allocations;
    }

    // ==========================================================
    // FALLBACK TOPIC ALLOCATION
    // ==========================================================

    private int addFallbackBlocks(
            Subject subject,
            int questionCount,
            List<GenerationBlock> blueprint,
            int globalQuestionIndex,
            DifficultyTargets difficultyTargets) {

        List<Topic> topics =
                topicRepository
                        .findBySubjectIdOrderByDisplayOrderAsc(
                                subject.getId()
                        );

        if(topics == null ||
                topics.isEmpty()) {

            throw new IllegalStateException(
                    "No topics configured for subject: "
                            + subject.getName()
            );
        }

        /*
         * The fallback path also guarantees topic coverage
         * whenever the subject has enough questions for all
         * configured topics.
         */
        int usableTopicCount =
                Math.min(
                        topics.size(),
                        questionCount
                );

        List<TopicAllocation> allocations =
                new ArrayList<>();

        /*
         * Every usable topic gets one question first.
         */
        for(int i = 0;
            i < usableTopicCount;
            i++) {

            allocations.add(
                    new TopicAllocation(
                            topics.get(i),
                            null,
                            1
                    )
            );
        }

        int remaining =
                questionCount
                        - usableTopicCount;

        if(remaining > 0) {

            /*
             * Distribute remaining questions round-robin
             * across all selected topics.
             */
            int topicIndex =
                    0;

            while(remaining > 0) {

                TopicAllocation allocation =
                        allocations.get(
                                topicIndex
                                        % allocations.size()
                        );

                allocation.questionCount++;

                remaining--;

                topicIndex++;
            }
        }

        for(TopicAllocation allocation :
                allocations) {

            int allocationRemaining =
                    allocation.questionCount;

            while(allocationRemaining > 0) {

                int batchSize =
                        Math.min(
                                allocationRemaining,
                                10
                        );

                globalQuestionIndex =
                        addDifficultyBlocks(
                                subject,
                                allocation,
                                batchSize,
                                blueprint,
                                globalQuestionIndex,
                                difficultyTargets
                        );

                allocationRemaining -=
                        batchSize;
            }
        }

        return globalQuestionIndex;
    }

    // ==========================================================
    // DIFFICULTY PROFILE
    // ==========================================================

    private String buildDifficultyProfile(
            QuestionDifficulty difficulty,
            int count) {

        return
                "Generate exactly "
                        + count
                        + " "
                        + difficulty.name()
                        + " difficulty question"
                        + (count == 1 ? "" : "s")
                        + " for this block. "
                        + "Do not change the requested difficulty.";
    }

    // ==========================================================
    // QUESTION STYLE PROFILE
    // ==========================================================

    private String buildStyleProfile() {

        return
                "Prioritize approximately 65-70% "
                        + "clinical/application questions and "
                        + "30-35% direct or conceptual questions "
                        + "across the complete exam. "
                        + "Include integrated reasoning whenever "
                        + "medically appropriate. "
                        + "Do not claim an item is an official PYQ.";
    }

    // ==========================================================
    // CLINICAL QUESTION BASELINE
    // ==========================================================

    private boolean chooseClinicalBaseline(
            int questionIndex) {

        /*
         * Seven out of every ten question positions are
         * designated as clinical/application oriented.
         */
        int position =
                questionIndex % 10;

        return position < 7;
    }

    // ==========================================================
    // IMAGE BASELINE
    // ==========================================================

    private boolean chooseImageBaseline(
            int questionIndex) {

        /*
         * Keep false until the application has a real
         * image-question asset pipeline.
         *
         * We should never mark a text-only question as
         * image-based.
         */
        return false;
    }

    // ==========================================================
    // SAFE TREND SCORE
    // ==========================================================

    private double safeScore(
            QuestionTrend trend) {

        if(trend == null ||
                trend.getTrendScore() == null ||
                trend.getTrendScore() < 0.0) {

            return 0.0;
        }

        return trend.getTrendScore();
    }

    // ==========================================================
    // TOPIC ALLOCATION
    // ==========================================================

    private static class TopicAllocation {

        private final Topic topic;

        private final QuestionTrend trend;

        private int questionCount;

        private TopicAllocation(
                Topic topic,
                QuestionTrend trend,
                int questionCount) {

            this.topic =
                    topic;

            this.trend =
                    trend;

            this.questionCount =
                    questionCount;
        }
    }

    // ==========================================================
    // DIFFICULTY TARGETS
    // ==========================================================

    private static class DifficultyTargets {

        private int easyRemaining;

        private int mediumRemaining;

        private int hardRemaining;

        private DifficultyTargets(
                int easyRemaining,
                int mediumRemaining,
                int hardRemaining) {

            this.easyRemaining =
                    easyRemaining;

            this.mediumRemaining =
                    mediumRemaining;

            this.hardRemaining =
                    hardRemaining;
        }
    }

    // ==========================================================
    // GENERATION BLOCK
    // ==========================================================

    public static class GenerationBlock {

        private final Subject subject;

        private final Topic topic;

        private final int questionCount;

        private final QuestionDifficulty difficulty;

        private final boolean clinicalCase;

        private final boolean imageBased;

        private final QuestionTrend trend;

        private final String difficultyProfile;

        private final String styleProfile;

        public GenerationBlock(
                Subject subject,
                Topic topic,
                int questionCount,
                QuestionDifficulty difficulty,
                boolean clinicalCase,
                boolean imageBased,
                QuestionTrend trend,
                String difficultyProfile,
                String styleProfile) {

            this.subject =
                    subject;

            this.topic =
                    topic;

            this.questionCount =
                    questionCount;

            this.difficulty =
                    difficulty;

            this.clinicalCase =
                    clinicalCase;

            this.imageBased =
                    imageBased;

            this.trend =
                    trend;

            this.difficultyProfile =
                    difficultyProfile;

            this.styleProfile =
                    styleProfile;
        }

        public Subject getSubject() {
            return subject;
        }

        public Topic getTopic() {
            return topic;
        }

        public int getQuestionCount() {
            return questionCount;
        }

        public QuestionDifficulty getDifficulty() {
            return difficulty;
        }

        public boolean isClinicalCase() {
            return clinicalCase;
        }

        public boolean isImageBased() {
            return imageBased;
        }

        public QuestionTrend getTrend() {
            return trend;
        }

        public String getDifficultyProfile() {
            return difficultyProfile;
        }

        public String getStyleProfile() {
            return styleProfile;
        }
    }

    // ==========================================================
    // BLUEPRINT PREVIEW
    // ==========================================================

    public List<GenerationBlock> previewBlueprint(
            int totalQuestions) {

        return buildBlueprint(
                totalQuestions
        );
    }
}