package com.medprep.service;

import com.medprep.entity.PracticeSession;
import com.medprep.entity.Question;
import com.medprep.entity.SessionQuestion;

import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SessionQuestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MockTestGenerationService {

    private static final int FIXED_MOCK_SET_QUESTION_COUNT = 300;
    private static final int FIXED_MOCK_PART_QUESTION_COUNT = 150;
    private static final String FIXED_MOCK_SET_PREFIX =
            "FMGE-MOCK-01-";

    private final PracticeSessionRepository practiceSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionRepository questionRepository;
    private final MockTestBlueprintService mockTestBlueprintService;

    public MockTestGenerationService(
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            QuestionRepository questionRepository,
            MockTestBlueprintService mockTestBlueprintService) {

        this.practiceSessionRepository = practiceSessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
        this.questionRepository = questionRepository;
        this.mockTestBlueprintService = mockTestBlueprintService;
    }

    // ==========================================================
    // ASSEMBLE COMPLETE MOCK TEST FROM THE QUESTION BANK
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

        if(totalQuestions == FIXED_MOCK_SET_QUESTION_COUNT &&
                useFixedMockSetWhenAvailable(session)) {

            return;
        }

        List<MockTestBlueprintService.GenerationBlock> blueprint =
                mockTestBlueprintService.buildBlueprint(totalQuestions);

        if(blueprint == null || blueprint.isEmpty()) {
            throw new IllegalStateException(
                    "Unable to build mock test blueprint"
            );
        }

        List<Question> selectedQuestions = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        for(MockTestBlueprintService.GenerationBlock block : blueprint) {
            if(block == null || block.getQuestionCount() <= 0) {
                continue;
            }

            int required = block.getQuestionCount();

            required -= addQuestions(
                    questionRepository
                            .findByTopicIdAndDifficultyAndActiveTrueOrderByIdAsc(
                                    block.getTopic().getId(),
                                    block.getDifficulty()
                            ),
                    required,
                    selectedQuestions,
                    selectedIds
            );

            // Prefer the planned topic when its exact difficulty pool is
            // temporarily short, then preserve difficulty within the subject.
            if(required > 0) {
                required -= addQuestions(
                        questionRepository
                                .findByTopicIdAndActiveTrueOrderByIdAsc(
                                        block.getTopic().getId()
                                ),
                        required,
                        selectedQuestions,
                        selectedIds
                );
            }

            if(required > 0) {
                required -= addQuestions(
                        questionRepository
                                .findBySubjectIdAndDifficultyAndActiveTrueOrderByIdAsc(
                                        block.getSubject().getId(),
                                        block.getDifficulty()
                                ),
                        required,
                        selectedQuestions,
                        selectedIds
                );
            }

            if(required > 0) {
                required -= addQuestions(
                        questionRepository
                                .findBySubjectIdAndActiveTrueOrderByIdAsc(
                                        block.getSubject().getId()
                                ),
                        required,
                        selectedQuestions,
                        selectedIds
                );
            }

            if(required > 0) {
                throw new IllegalStateException(
                        "The question bank does not have enough unique active questions for "
                                + block.getSubject().getName()
                                + " - "
                                + block.getTopic().getName()
                                + ". Add at least "
                                + required
                                + " more question(s) for this blueprint allocation."
                );
            }
        }

        if(selectedQuestions.size() != totalQuestions) {
            throw new IllegalStateException(
                    "Unable to assemble complete mock test. Required: "
                            + totalQuestions
                            + ", selected: "
                            + selectedQuestions.size()
            );
        }

        Collections.shuffle(selectedQuestions);

        saveQuestionsForSession(
                session,
                selectedQuestions
        );
    }

    // ==========================================================
    // FIXED FMGE MOCK SET 01
    // ==========================================================

    private boolean useFixedMockSetWhenAvailable(
            PracticeSession session) {

        List<Question> fixedQuestions =
                questionRepository
                        .findByConceptTagStartingWithOrderByConceptTagAsc(
                                FIXED_MOCK_SET_PREFIX
                        );

        if(fixedQuestions == null || fixedQuestions.isEmpty()) {
            return false;
        }

        if(fixedQuestions.size() != FIXED_MOCK_SET_QUESTION_COUNT) {
            throw new IllegalStateException(
                    "FMGE Mock Set 01 is incomplete. Expected "
                            + FIXED_MOCK_SET_QUESTION_COUNT
                            + " questions but found "
                            + fixedQuestions.size()
            );
        }

        Set<Long> questionIds = new HashSet<>();

        for(int index = 0; index < fixedQuestions.size(); index++) {
            Question question = fixedQuestions.get(index);

            if(question == null || question.getId() == null) {
                throw new IllegalStateException(
                        "FMGE Mock Set 01 contains an invalid question"
                );
            }

            if(!questionIds.add(question.getId())) {
                throw new IllegalStateException(
                        "FMGE Mock Set 01 contains a duplicate question"
                );
            }

            int part = index < FIXED_MOCK_PART_QUESTION_COUNT ? 1 : 2;
            int partQuestionNumber =
                    (index % FIXED_MOCK_PART_QUESTION_COUNT) + 1;

            String expectedTag = String.format(
                    "FMGE-MOCK-01-P%d-Q%03d",
                    part,
                    partQuestionNumber
            );

            if(!expectedTag.equals(question.getConceptTag())) {
                throw new IllegalStateException(
                        "FMGE Mock Set 01 order is invalid. Expected tag "
                                + expectedTag
                                + " but found "
                                + question.getConceptTag()
                );
            }
        }

        saveQuestionsForSession(
                session,
                fixedQuestions
        );

        return true;
    }

    private void saveQuestionsForSession(
            PracticeSession session,
            List<Question> questions) {

        int displayOrder = 1;

        for(Question question : questions) {
            sessionQuestionRepository.save(
                    new SessionQuestion(
                            session,
                            question,
                            displayOrder++
                    )
            );
        }

        session.setTotalQuestions(questions.size());
        session.setAnsweredQuestions(0);
        session.setCorrectAnswers(0);

        practiceSessionRepository.save(session);
    }

    private int addQuestions(
            List<Question> candidates,
            int maximum,
            List<Question> selectedQuestions,
            Set<Long> selectedIds) {

        if(maximum <= 0 || candidates == null || candidates.isEmpty()) {
            return 0;
        }

        List<Question> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);

        int added = 0;

        for(Question question : shuffled) {
            if(question == null ||
               question.getId() == null ||
               selectedIds.contains(question.getId())) {
                continue;
            }

            selectedQuestions.add(question);
            selectedIds.add(question.getId());
            added++;

            if(added == maximum) {
                break;
            }
        }

        return added;
    }
}
