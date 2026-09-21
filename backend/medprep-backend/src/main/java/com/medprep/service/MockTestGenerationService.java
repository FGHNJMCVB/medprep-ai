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

        int displayOrder = 1;

        for(Question question : selectedQuestions) {
            sessionQuestionRepository.save(
                    new SessionQuestion(
                            session,
                            question,
                            displayOrder++
                    )
            );
        }

        session.setTotalQuestions(selectedQuestions.size());
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
