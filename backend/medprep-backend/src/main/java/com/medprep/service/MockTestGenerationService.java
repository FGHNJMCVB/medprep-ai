package com.medprep.service;

import com.medprep.dto.QuestionGenerationRequest;

import com.medprep.entity.PracticeSession;
import com.medprep.entity.Question;
import com.medprep.entity.SessionQuestion;

import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SessionQuestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MockTestGenerationService {

    private final AiQuestionGenerationService aiQuestionGenerationService;

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final QuestionRepository questionRepository;

    private final MockTestBlueprintService mockTestBlueprintService;

    public MockTestGenerationService(
            AiQuestionGenerationService aiQuestionGenerationService,
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            QuestionRepository questionRepository,
            MockTestBlueprintService mockTestBlueprintService) {

        this.aiQuestionGenerationService =
                aiQuestionGenerationService;

        this.practiceSessionRepository =
                practiceSessionRepository;

        this.sessionQuestionRepository =
                sessionQuestionRepository;

        this.questionRepository =
                questionRepository;

        this.mockTestBlueprintService =
                mockTestBlueprintService;
    }

    // ==========================================================
    // GENERATE COMPLETE MOCK TEST
    // ==========================================================

    @Transactional(rollbackFor = Exception.class)
    public void generateMockTest(
            PracticeSession session,
            int totalQuestions) {

        if(session == null) {

            throw new IllegalArgumentException(
                    "Practice session is required"
            );
        }

        if(totalQuestions <= 0) {

            throw new IllegalArgumentException(
                    "Total questions must be greater than zero"
            );
        }

        List<MockTestBlueprintService.GenerationBlock> blueprint =
                mockTestBlueprintService.buildBlueprint(
                        totalQuestions
                );

        if(blueprint == null ||
           blueprint.isEmpty()) {

            throw new IllegalStateException(
                    "Unable to build mock test blueprint"
            );
        }

        int generatedCount = 0;

        int questionNumber = 1;

        for(MockTestBlueprintService.GenerationBlock block :
                blueprint) {

            if(block == null) {

                throw new IllegalStateException(
                        "Mock test blueprint contains a null block"
                );
            }

            if(generatedCount >= totalQuestions) {

                break;
            }

            int remainingForBlock =
                    Math.min(
                            block.getQuestionCount(),
                            totalQuestions - generatedCount
                    );

            if(remainingForBlock <= 0) {

                continue;
            }

            while(remainingForBlock > 0) {

                int batchSize =
                        Math.min(
                                remainingForBlock,
                                10
                        );

                QuestionGenerationRequest request =
                        buildGenerationRequest(
                                block,
                                batchSize
                        );

                List<Long> questionIds =
                        aiQuestionGenerationService
                                .generateAndSaveQuestions(
                                        request
                                );

                if(questionIds == null ||
                   questionIds.isEmpty()) {

                    throw new IllegalStateException(
                            "AI generated no usable questions for "
                                    + block.getSubject().getName()
                                    + " - "
                                    + block.getTopic().getName()
                    );
                }

                int acceptedInBatch = 0;

                for(Long questionId :
                        questionIds) {

                    if(questionId == null) {

                        continue;
                    }

                    if(generatedCount >= totalQuestions) {

                        break;
                    }

                    if(acceptedInBatch >= batchSize) {

                        break;
                    }

                    Question question =
                            questionRepository
                                    .findById(questionId)
                                    .orElseThrow(() ->
                                            new IllegalStateException(
                                                    "Generated question not found: "
                                                            + questionId
                                            )
                                    );

                    SessionQuestion sessionQuestion =
                            new SessionQuestion(
                                    session,
                                    question,
                                    questionNumber
                            );

                    sessionQuestionRepository.save(
                            sessionQuestion
                    );

                    questionNumber++;

                    generatedCount++;

                    acceptedInBatch++;
                }

                if(acceptedInBatch <= 0) {

                    throw new IllegalStateException(
                            "No questions were accepted from AI batch for "
                                    + block.getSubject().getName()
                                    + " - "
                                    + block.getTopic().getName()
                    );
                }

                remainingForBlock -=
                        acceptedInBatch;
            }
        }

        // ======================================================
        // FINAL VALIDATION
        // ======================================================

        if(generatedCount != totalQuestions) {

            throw new IllegalStateException(
                    "Unable to generate complete mock test. "
                            + "Required: "
                            + totalQuestions
                            + ", Generated: "
                            + generatedCount
        );
        }

        // ======================================================
        // UPDATE SESSION ONLY AFTER COMPLETE GENERATION
        // ======================================================

        session.setTotalQuestions(
                generatedCount
        );

        session.setAnsweredQuestions(
                0
        );

        session.setCorrectAnswers(
                0
        );

        practiceSessionRepository.save(
                session
        );
    }

    // ==========================================================
    // BUILD AI GENERATION REQUEST
    // ==========================================================

    private QuestionGenerationRequest buildGenerationRequest(
            MockTestBlueprintService.GenerationBlock block,
            int count) {

        QuestionGenerationRequest request =
                new QuestionGenerationRequest();

        request.setSubjectId(
                block.getSubject().getId()
        );

        request.setTopicId(
                block.getTopic().getId()
        );

        request.setCount(
                count
        );

        request.setDifficulty(
                block.getDifficulty()
        );

        request.setHighYield(
                block.getTrend() != null
                        ? block.getTrend().getHighYield()
                        : true
        );

        request.setClinicalCase(
                block.isClinicalCase()
        );

        request.setImageBased(
                block.isImageBased()
        );

        StringBuilder focus =
                new StringBuilder();

        focus.append(
                "Generate original FMGE-aligned questions "
                        + "for "
                        + block.getSubject().getName()
                        + " - "
                        + block.getTopic().getName()
                        + "."
        );

        if(block.getTrend() != null) {

            focus.append(
                    " Prioritize the high-yield concept: "
                            + block.getTrend().getConceptTag()
                            + "."
            );

            focus.append(
                    " Use the trend information as a topic-priority "
                            + "signal, not as proof that an exact question "
                            + "will appear in a future examination."
            );
        }

        focus.append(" ");

        focus.append(
                block.getDifficultyProfile()
        );

        focus.append(" ");

        focus.append(
                block.getStyleProfile()
        );

        focus.append(
                " Every question must have exactly four options "
                        + "and exactly one correct answer."
        );

        focus.append(
                " Avoid repeating the same fact or question pattern "
                        + "within the batch."
        );

        focus.append(
                " Explanations must be medically accurate and "
                        + "directly support the correct answer."
        );

        request.setFocus(
                focus.toString()
        );

        return request;
    }
}