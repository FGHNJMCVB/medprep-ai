package com.medprep.service;

import com.medprep.ai.AiQuestionGenerator;
import com.medprep.ai.GeneratedQuestionParser;

import com.medprep.dto.AdminQuestionRequest;
import com.medprep.dto.GeneratedQuestionResponse;
import com.medprep.dto.QuestionGenerationRequest;

import com.medprep.entity.Question;
import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;

import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AiQuestionGenerationService {

    private final AiQuestionGenerator aiQuestionGenerator;

    private final GeneratedQuestionParser generatedQuestionParser;

    private final QuestionTrendService questionTrendService;

    private final AdminQuestionService adminQuestionService;

    private final QuestionRepository questionRepository;

    private final SubjectRepository subjectRepository;

    private final TopicRepository topicRepository;

    public AiQuestionGenerationService(
            AiQuestionGenerator aiQuestionGenerator,
            GeneratedQuestionParser generatedQuestionParser,
            QuestionTrendService questionTrendService,
            AdminQuestionService adminQuestionService,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.aiQuestionGenerator = aiQuestionGenerator;

        this.generatedQuestionParser = generatedQuestionParser;

        this.questionTrendService = questionTrendService;

        this.adminQuestionService = adminQuestionService;

        this.questionRepository = questionRepository;

        this.subjectRepository = subjectRepository;

        this.topicRepository = topicRepository;
    }

    // ==========================================================
    // GENERATE QUESTIONS
    // ==========================================================

    public int generateQuestions(
            QuestionGenerationRequest request) {

        return generateAndSaveQuestions(
                request
        ).size();
    }

    // ==========================================================
    // GENERATE AND SAVE QUESTIONS
    // ==========================================================

    public List<Long> generateAndSaveQuestions(
            QuestionGenerationRequest request) {

        // ------------------------------------------------------
        // Validate generation request.
        // ------------------------------------------------------

        validateRequest(
                request
        );

        // ------------------------------------------------------
        // Resolve authoritative database scope.
        //
        // This converts:
        //
        // subjectId -> actual Subject
        // topicId   -> actual Topic
        //
        // Gemini will receive the real names rather than having
        // to infer meaning from numeric IDs.
        // ------------------------------------------------------

        GenerationScope scope =
                resolveGenerationScope(
                        request
                );

        // ------------------------------------------------------
        // Build generation context.
        //
        // This contains:
        //
        // 1. Database-resolved subject name
        // 2. Database-resolved topic name
        // 3. Trend / research context
        // ------------------------------------------------------

        String promptContext =
                buildPromptContext(
                        request,
                        scope
                );

        // ------------------------------------------------------
        // Generate questions using Gemini.
        // ------------------------------------------------------

        String aiResponse =
                aiQuestionGenerator.generateQuestions(
                        request,
                        promptContext
                );

        if (aiResponse == null ||
                aiResponse.trim().isEmpty()) {

            throw new IllegalStateException(
                    "AI returned an empty response"
            );
        }

        // ------------------------------------------------------
        // Parse AI response.
        // ------------------------------------------------------

        GeneratedQuestionResponse generatedResponse =
                generatedQuestionParser.parse(
                        aiResponse
                );

        if (generatedResponse == null) {

            throw new IllegalStateException(
                    "AI response could not be parsed"
            );
        }

        // ------------------------------------------------------
        // Extract questions.
        // ------------------------------------------------------

        List<AdminQuestionRequest> questions =
                generatedResponse.getQuestions();

        if (questions == null) {

            throw new IllegalStateException(
                    "AI response did not contain questions"
            );
        }

        // ------------------------------------------------------
        // EXACT COUNT VALIDATION
        // ------------------------------------------------------

        int requestedCount =
                request.getCount();

        if (questions.size() != requestedCount) {

            throw new IllegalStateException(
                    "AI generated "
                            + questions.size()
                            + " questions, but "
                            + requestedCount
                            + " were requested"
            );
        }

        // ------------------------------------------------------
        // LOAD EXISTING QUESTION KEYS
        //
        // This protects us from duplicates that already exist
        // anywhere in the active question bank.
        // ------------------------------------------------------

        Set<String> existingQuestionKeys =
                loadExistingQuestionKeys();

        // ------------------------------------------------------
        // VALIDATE THE ENTIRE BATCH BEFORE SAVING ANYTHING.
        //
        // This prevents a partially invalid AI response from
        // being written to the database.
        // ------------------------------------------------------

        Set<String> batchQuestionKeys =
                new HashSet<>();

        for (int i = 0;
             i < questions.size();
             i++) {

            AdminQuestionRequest question =
                    questions.get(i);

            // ----------------------------------------------
            // Validate question structure and metadata.
            // ----------------------------------------------

            validateGeneratedQuestion(
                    question,
                    request,
                    i + 1
            );

            // ----------------------------------------------
            // Normalize question text.
            // ----------------------------------------------

            String questionKey =
                    normalizeQuestionText(
                            question.getQuestionText()
                    );

            if (questionKey.isEmpty()) {

                throw new IllegalStateException(
                        "Generated question "
                                + (i + 1)
                                + " has invalid question text"
                );
            }

            // ----------------------------------------------
            // Detect duplicate questions inside the
            // current Gemini response.
            // ----------------------------------------------

            if (!batchQuestionKeys.add(
                    questionKey)) {

                throw new IllegalStateException(
                        "AI generated duplicate question "
                                + "within the same batch at position "
                                + (i + 1)
                );
            }
        }

        // ------------------------------------------------------
        // SAVE VALIDATED QUESTIONS
        // ------------------------------------------------------

        List<Long> questionIds =
                new ArrayList<>();

        for (AdminQuestionRequest question :
                questions) {

            // --------------------------------------------------
            // Normalize question text.
            // --------------------------------------------------

            String questionKey =
                    normalizeQuestionText(
                            question.getQuestionText()
                    );

            // --------------------------------------------------
            // CROSS-BATCH / DATABASE DUPLICATE CHECK
            //
            // If this question already exists in the active
            // question bank, do not save it again.
            // --------------------------------------------------

            if (existingQuestionKeys.contains(
                    questionKey)) {

                continue;
            }

            // --------------------------------------------------
            // Force metadata owned by the resolved database
            // generation scope.
            //
            // Important:
            //
            // We use the resolved IDs rather than blindly using
            // the raw request values.
            // --------------------------------------------------

            question.setSubjectId(
                    scope.subject.getId()
            );

            question.setTopicId(
                    scope.topic != null
                            ? scope.topic.getId()
                            : null
            );

            if (question.getHighYield() == null) {

                question.setHighYield(
                        request.getHighYield()
                );
            }

            if (question.getClinicalCase() == null) {

                question.setClinicalCase(
                        request.getClinicalCase()
                );
            }

            if (question.getImageBased() == null) {

                question.setImageBased(
                        request.getImageBased()
                );
            }

            // --------------------------------------------------
            // Save question.
            // --------------------------------------------------

            try {

                Long questionId =
                        adminQuestionService.createQuestion(
                                question
                        );

                questionIds.add(
                        questionId
                );

                // ------------------------------------------------
                // IMPORTANT:
                //
                // Add the newly saved question to the same
                // in-memory set.
                //
                // This protects us even if the same normalized
                // text somehow appears again during this save
                // operation.
                // ------------------------------------------------

                existingQuestionKeys.add(
                        questionKey
                );

            } catch (IllegalArgumentException exception) {

                String message =
                        exception.getMessage();

                // --------------------------------------------------
                // Database-level duplicate.
                // --------------------------------------------------

                if (message != null &&
                        message.toLowerCase(
                                Locale.ROOT
                        ).contains(
                                "same question text"
                        )) {

                    /*
                     * Do not add the duplicate question to
                     * the current generation result.
                     *
                     * MockTestGenerationService can see
                     * fewer returned IDs and generate
                     * replacements.
                     */

                    existingQuestionKeys.add(
                            questionKey
                    );

                    continue;
                }

                throw exception;
            }
        }

        return questionIds;
    }

    // ==========================================================
    // RESOLVE DATABASE GENERATION SCOPE
    // ==========================================================

    private GenerationScope resolveGenerationScope(
            QuestionGenerationRequest request) {

        Subject subject = null;

        Topic topic = null;

        // ------------------------------------------------------
        // Topic-based generation.
        // ------------------------------------------------------

        if (request.getTopicId() != null) {

            topic = topicRepository
                    .findById(
                            request.getTopicId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Topic not found: "
                                            + request.getTopicId()
                            )
                    );

            subject = topic.getSubject();

            if (subject == null) {

                throw new IllegalStateException(
                        "Topic "
                                + topic.getId()
                                + " has no associated subject"
                );
            }

            // --------------------------------------------------
            // If caller supplied both IDs, make sure they agree.
            // --------------------------------------------------

            if (request.getSubjectId() != null &&
                    !request.getSubjectId().equals(
                            subject.getId()
                    )) {

                throw new IllegalArgumentException(
                        "Subject ID "
                                + request.getSubjectId()
                                + " does not belong to topic "
                                + request.getTopicId()
                );
            }
        }

        // ------------------------------------------------------
        // Subject-only generation.
        // ------------------------------------------------------

        else {

            subject = subjectRepository
                    .findById(
                            request.getSubjectId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Subject not found: "
                                            + request.getSubjectId()
                            )
                    );
        }

        return new GenerationScope(
                subject,
                topic
        );
    }

    // ==========================================================
    // BUILD PROMPT CONTEXT
    // ==========================================================

    private String buildPromptContext(
            QuestionGenerationRequest request,
            GenerationScope scope) {

        StringBuilder context =
                new StringBuilder();

        // ======================================================
        // DATABASE-RESOLVED AUTHORITATIVE SCOPE
        // ======================================================

        context.append(
                "\n\n=========================================================="
        );

        context.append(
                "\nDATABASE-RESOLVED AUTHORITATIVE SCOPE"
        );

        context.append(
                "\n=========================================================="
        );

        context.append(
                "\nDatabase subject ID: "
                        + scope.subject.getId()
        );

        context.append(
                "\nDatabase subject name: "
                        + scope.subject.getName()
        );

        if (scope.topic != null) {

            context.append(
                    "\nDatabase topic ID: "
                            + scope.topic.getId()
            );

            context.append(
                    "\nDatabase topic name: "
                            + scope.topic.getName()
            );

            context.append(
                    "\n\nThe database topic name above is the "
                            + "authoritative topic boundary."
            );

            context.append(
                    "\nDo not infer a different topic from the "
                            + "numeric topic ID."
            );

            context.append(
                    "\nDo not replace this topic with another "
                            + "topic from the same subject."
            );

        } else {

            context.append(
                    "\nDatabase topic: none - subject-level generation"
            );

            context.append(
                    "\nQuestions may cover the subject broadly "
                            + "because no specific topic was requested."
            );
        }

        // ======================================================
        // TREND CONTEXT
        // ======================================================

        List<QuestionTrend> trends;

        if (scope.topic != null) {

            trends = questionTrendService
                    .getTopicTrends(
                            scope.topic.getId()
                    );

        } else {

            trends = questionTrendService
                    .getSubjectTrends(
                            scope.subject.getId()
                    );
        }

        if (trends == null ||
                trends.isEmpty()) {

            return context.toString();
        }

        context.append(
                "\n\n=========================================================="
        );

        context.append(
                "\nTREND / RESEARCH CONTEXT"
        );

        context.append(
                "\n=========================================================="
        );

        context.append(
                "\nUse the supplied trend information ONLY to "
                        + "prioritize high-value concepts that belong "
                        + "to the requested database scope."
        );

        context.append(
                "\nTrend information must NEVER override the "
                        + "database subject/topic boundary."
        );

        int limit =
                Math.min(
                        trends.size(),
                        10
                );

        for (int i = 0;
             i < limit;
             i++) {

            QuestionTrend trend =
                    trends.get(i);

            if (trend == null) {

                continue;
            }

            context.append(
                    "\n\nConcept: "
                            + trend.getConceptTag()
            );

            context.append(
                    "\nTrend score: "
                            + trend.getTrendScore()
            );

            context.append(
                    "\nHistorical frequency: "
                            + trend.getHistoricalFrequency()
            );

            context.append(
                    "\nRecent frequency: "
                            + trend.getRecentFrequency()
            );

            context.append(
                    "\nRecurrence years: "
                            + trend.getRecurrenceYears()
            );

            context.append(
                    "\nClinical weight: "
                            + trend.getClinicalWeight()
            );

            context.append(
                    "\nImage weight: "
                            + trend.getImageWeight()
            );

            context.append(
                    "\nIntegrated weight: "
                            + trend.getIntegratedWeight()
            );

            context.append(
                    "\nHigh yield: "
                            + trend.getHighYield()
            );

            context.append(
                    "\nSource: "
                            + trend.getSourceReference()
            );

            context.append(
                    "\n---"
            );
        }

        return context.toString();
    }

    // ==========================================================
    // NORMALIZE QUESTION TEXT
    // ==========================================================

    private String normalizeQuestionText(
            String questionText) {

        if (questionText == null) {

            return "";
        }

        return questionText
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .replaceAll(
                        "[^a-z0-9 ]",
                        ""
                )
                .trim();
    }

    // ==========================================================
    // VALIDATE GENERATED QUESTION
    // ==========================================================

    private void validateGeneratedQuestion(
            AdminQuestionRequest question,
            QuestionGenerationRequest request,
            int questionNumber) {

        // ------------------------------------------------------
        // Null check.
        // ------------------------------------------------------

        if (question == null) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " is null"
            );
        }

        // ------------------------------------------------------
        // Question text.
        // ------------------------------------------------------

        if (question.getQuestionText() == null ||
                question.getQuestionText()
                        .trim()
                        .isEmpty()) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " has empty question text"
            );
        }

        // ------------------------------------------------------
        // Explanation.
        // ------------------------------------------------------

        if (question.getExplanation() == null ||
                question.getExplanation()
                        .trim()
                        .isEmpty()) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " has empty explanation"
            );
        }

        // ------------------------------------------------------
        // Difficulty.
        // ------------------------------------------------------

        if (question.getDifficulty() == null) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " has no difficulty"
            );
        }

        if (question.getDifficulty() !=
                request.getDifficulty()) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " has difficulty "
                            + question.getDifficulty()
                            + " but "
                            + request.getDifficulty()
                            + " was requested"
            );
        }

        // ------------------------------------------------------
        // Question type.
        // ------------------------------------------------------

        if (question.getQuestionType() == null) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " has no question type"
            );
        }

        // ------------------------------------------------------
        // Options.
        //
        // GeneratedQuestionParser already verifies:
        // exactly 4 options
        // exactly 1 correct option
        // valid option labels
        // non-empty option text
        // ------------------------------------------------------

        if (question.getOptions() == null ||
                question.getOptions().size() != 4) {

            throw new IllegalStateException(
                    "Generated question "
                            + questionNumber
                            + " must contain exactly 4 options"
            );
        }
    }

    // ==========================================================
    // VALIDATE GENERATION REQUEST
    // ==========================================================

    private void validateRequest(
            QuestionGenerationRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Question generation request is required"
            );
        }

        if (request.getSubjectId() == null &&
                request.getTopicId() == null) {

            throw new IllegalArgumentException(
                    "Subject ID or Topic ID is required"
            );
        }

        if (request.getCount() == null ||
                request.getCount() <= 0) {

            throw new IllegalArgumentException(
                    "Question count must be greater than zero"
            );
        }

        if (request.getCount() > 50) {

            throw new IllegalArgumentException(
                    "Maximum 50 questions can be generated per request"
            );
        }

        if (request.getDifficulty() == null) {

            throw new IllegalArgumentException(
                    "Difficulty is required"
            );
        }
    }

    // ==========================================================
    // LOAD EXISTING QUESTION KEYS
    // ==========================================================

    private Set<String> loadExistingQuestionKeys() {

        Set<String> existingQuestionKeys =
                new HashSet<>();

        List<Question> existingQuestions =
                questionRepository.findByActiveTrue();

        for (Question question :
                existingQuestions) {

            if (question == null) {

                continue;
            }

            String key =
                    normalizeQuestionText(
                            question.getQuestionText()
                    );

            if (!key.isEmpty()) {

                existingQuestionKeys.add(
                        key
                );
            }
        }

        return existingQuestionKeys;
    }

    // ==========================================================
    // GENERATION SCOPE
    // ==========================================================

    private static class GenerationScope {

        private final Subject subject;

        private final Topic topic;

        private GenerationScope(
                Subject subject,
                Topic topic) {

            this.subject = subject;

            this.topic = topic;
        }
    }
}