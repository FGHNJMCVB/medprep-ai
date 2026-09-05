package com.medprep.service;

import com.medprep.dto.AdminBulkQuestionRequest;
import com.medprep.dto.AdminQuestionOptionRequest;
import com.medprep.dto.AdminQuestionRequest;

import com.medprep.entity.Question;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;

import com.medprep.repository.QuestionOptionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminQuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;

    public AdminQuestionService(
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository) {

        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    // ==========================================================
    // CREATE SINGLE QUESTION
    // ==========================================================

    @Transactional
    public Long createQuestion(AdminQuestionRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Question request is required");
        }

        // ==========================================================
        // VALIDATE BASIC QUESTION DATA
        // ==========================================================

        if (request.getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject ID is required");
        }

        if (request.getTopicId() == null) {

            throw new IllegalArgumentException(
                    "Topic ID is required");
        }

        if (request.getQuestionText() == null ||
                request.getQuestionText().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Question text is required");
        }

        if (request.getExplanation() == null ||
                request.getExplanation().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Question explanation is required");
        }

        if (request.getDifficulty() == null) {

            throw new IllegalArgumentException(
                    "Question difficulty is required");
        }

        if (request.getQuestionType() == null) {

            throw new IllegalArgumentException(
                    "Question type is required");
        }

        // ==========================================================
        // DUPLICATE QUESTION CHECK
        // ==========================================================

        String normalizedQuestionText = request.getQuestionText()
                .trim();

        if (questionRepository
                .existsByQuestionTextIgnoreCase(
                        normalizedQuestionText)) {

            throw new IllegalArgumentException(
                    "A question with the same question text "
                            + "already exists");
        }

        // ==========================================================
        // VALIDATE SUBJECT
        // ==========================================================

        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subject not found: "
                                + request.getSubjectId()));

        // ==========================================================
        // VALIDATE TOPIC
        // ==========================================================

        Topic topic = topicRepository
                .findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Topic not found: "
                                + request.getTopicId()));

        // ==========================================================
        // TOPIC MUST BELONG TO SUBJECT
        // ==========================================================

        if (!topic.getSubject()
                .getId()
                .equals(subject.getId())) {

            throw new IllegalArgumentException(
                    "Topic does not belong to the selected subject");
        }

        // ==========================================================
        // VALIDATE OPTIONS
        // ==========================================================

        List<AdminQuestionOptionRequest> options = request.getOptions();

        if (options == null || options.size() != 4) {

            throw new IllegalArgumentException(
                    "Exactly 4 options are required");
        }

        int correctCount = 0;

        Set<String> optionLabels = new HashSet<String>();

        Set<Integer> displayOrders = new HashSet<Integer>();

        for (AdminQuestionOptionRequest option : options) {

            if (option == null) {

                throw new IllegalArgumentException(
                        "Option cannot be null");
            }

            // ------------------------------------------------------
            // Option label
            // ------------------------------------------------------

            if (option.getOptionLabel() == null ||
                    option.getOptionLabel().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Option label is required");
            }

            String optionLabel = option.getOptionLabel()
                    .trim()
                    .toUpperCase();

            if (!optionLabels.add(optionLabel)) {

                throw new IllegalArgumentException(
                        "Duplicate option label: "
                                + optionLabel);
            }

            // ------------------------------------------------------
            // Option text
            // ------------------------------------------------------

            if (option.getOptionText() == null ||
                    option.getOptionText().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Option text is required");
            }

            // ------------------------------------------------------
            // Display order
            // ------------------------------------------------------

            if (option.getDisplayOrder() == null) {

                throw new IllegalArgumentException(
                        "Option display order is required");
            }

            if (!displayOrders.add(
                    option.getDisplayOrder())) {

                throw new IllegalArgumentException(
                        "Duplicate option display order: "
                                + option.getDisplayOrder());
            }

            // ------------------------------------------------------
            // Correct flag
            // ------------------------------------------------------

            if (option.getCorrect() == null) {

                throw new IllegalArgumentException(
                        "Option correct flag is required");
            }

            if (Boolean.TRUE.equals(
                    option.getCorrect())) {

                correctCount++;
            }
        }

        // ==========================================================
        // VALIDATE OPTION LABELS
        // ==========================================================

        if (!optionLabels.contains("A") ||
                !optionLabels.contains("B") ||
                !optionLabels.contains("C") ||
                !optionLabels.contains("D")) {

            throw new IllegalArgumentException(
                    "Options must contain labels A, B, C and D");
        }

        // ==========================================================
        // VALIDATE DISPLAY ORDERS
        // ==========================================================

        if (!displayOrders.contains(1) ||
                !displayOrders.contains(2) ||
                !displayOrders.contains(3) ||
                !displayOrders.contains(4)) {

            throw new IllegalArgumentException(
                    "Option display orders must be 1, 2, 3 and 4");
        }

        // ==========================================================
        // EXACTLY ONE CORRECT ANSWER
        // ==========================================================

        if (correctCount != 1) {

            throw new IllegalArgumentException(
                    "Exactly one option must be correct");
        }

        // ==========================================================
        // CREATE QUESTION
        // ==========================================================

        Question question = new Question(
                request.getQuestionText().trim(),
                request.getExplanation().trim(),
                request.getDifficulty(),
                request.getQuestionType(),
                request.getConceptTag(),
                request.getSourceYear(),
                request.getSourceSession(),
                request.getSourceReference(),
                request.getHighYield() != null
                        ? request.getHighYield()
                        : false,
                request.getClinicalCase() != null
                        ? request.getClinicalCase()
                        : false,
                request.getImageBased() != null
                        ? request.getImageBased()
                        : false,
                true,
                subject,
                topic);

        Question savedQuestion = questionRepository.save(question);

        // ==========================================================
        // CREATE OPTIONS
        // ==========================================================

        for (AdminQuestionOptionRequest option : options) {

            String optionLabel = option.getOptionLabel()
                    .trim()
                    .toUpperCase();

            QuestionOption questionOption = new QuestionOption(
                    optionLabel,
                    option.getOptionText()
                            .trim(),
                    option.getDisplayOrder(),
                    option.getCorrect(),
                    savedQuestion);

            questionOptionRepository.save(
                    questionOption);
        }

        return savedQuestion.getId();
    }

    // ==========================================================
    // CREATE QUESTIONS IN BULK
    // ==========================================================

    @Transactional
    public int createQuestionsInBulk(
            AdminBulkQuestionRequest request) {

        if (request == null ||
                request.getQuestions() == null ||
                request.getQuestions().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one question is required");
        }

        int createdCount = 0;

        for (int i = 0; i < request.getQuestions().size(); i++) {

            AdminQuestionRequest questionRequest = request.getQuestions().get(i);

            try {

                createQuestion(
                        questionRequest);

                createdCount++;

            } catch (Exception exception) {

                throw new IllegalArgumentException(
                        "Failed to import question at index "
                                + i
                                + ": "
                                + exception.getMessage(),
                        exception);
            }
        }

        return createdCount;
    }
}