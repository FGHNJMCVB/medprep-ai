package com.medprep.service;

import com.medprep.dto.MockTestResultResponse;

import com.medprep.entity.MockTestConfig;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.User;

import com.medprep.repository.MockTestConfigRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MockTestSubmissionService {

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final MockTestConfigRepository mockTestConfigRepository;

    private final UserRepository userRepository;

    public MockTestSubmissionService(
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            MockTestConfigRepository mockTestConfigRepository,
            UserRepository userRepository) {

        this.practiceSessionRepository =
                practiceSessionRepository;

        this.sessionQuestionRepository =
                sessionQuestionRepository;

        this.mockTestConfigRepository =
                mockTestConfigRepository;

        this.userRepository =
                userRepository;
    }

    // ==========================================================
    // SUBMIT MOCK TEST
    // ==========================================================

    @Transactional
    public MockTestResultResponse submitMockTest(
            Long sessionId,
            String email) {

        // ------------------------------------------------------
        // Find authenticated user
        // ------------------------------------------------------

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        // ------------------------------------------------------
        // Find session
        // ------------------------------------------------------

        PracticeSession session =
                practiceSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Practice session not found: "
                                                + sessionId
                                )
                        );

        // ------------------------------------------------------
        // Verify session ownership
        // ------------------------------------------------------

        if(session.getUser() == null ||
           session.getUser().getId() == null ||
           !session.getUser()
                   .getId()
                   .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You do not have access to this session"
            );
        }

        // ------------------------------------------------------
        // Only IN_PROGRESS sessions can be submitted.
        // ------------------------------------------------------

        if(session.getStatus()
                != PracticeSessionStatus.IN_PROGRESS) {

            throw new IllegalArgumentException(
                    "This mock test has already been submitted"
            );
        }

        // ------------------------------------------------------
        // Load FMGE configuration.
        // ------------------------------------------------------

        MockTestConfig config =
                mockTestConfigRepository
                        .findByName("FMGE")
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "FMGE mock test configuration not found"
                                )
                        );

        // ------------------------------------------------------
        // Load all session questions.
        // ------------------------------------------------------

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                sessionId
                        );

        if(sessionQuestions == null ||
           sessionQuestions.isEmpty()) {

            throw new IllegalStateException(
                    "Mock test contains no questions"
            );
        }

        // ------------------------------------------------------
        // Recalculate counters from session questions.
        //
        // Do not rely only on session counters because this
        // gives submission a consistent source of truth.
        // ------------------------------------------------------

        int answeredQuestions = 0;

        int correctAnswers = 0;

        int incorrectAnswers = 0;

        for(SessionQuestion sessionQuestion :
                sessionQuestions) {

            if(Boolean.TRUE.equals(
                    sessionQuestion.getAnswered()
            )) {

                answeredQuestions++;

                if(Boolean.TRUE.equals(
                        sessionQuestion.getCorrect()
                )) {

                    correctAnswers++;

                } else {

                    incorrectAnswers++;
                }
            }
        }

        int totalQuestions =
                sessionQuestions.size();

        int unansweredQuestions =
                totalQuestions
                        - answeredQuestions;

        // ------------------------------------------------------
        // Score calculation.
        //
        // Current configuration:
        //
        // negativeMarking = false
        //
        // Therefore:
        //
        // score = correct answers
        //
        // When negative marking is enabled later, the penalty
        // can be applied here.
        // ------------------------------------------------------

        double score =
                calculateScore(
                        correctAnswers,
                        incorrectAnswers,
                        config
                );

        // ------------------------------------------------------
        // Calculate percentage.
        // ------------------------------------------------------

        double percentage = 0.0;

        if(totalQuestions > 0) {

            percentage =
                    (score / totalQuestions)
                            * 100.0;
        }

        // ------------------------------------------------------
        // Passing rule.
        // ------------------------------------------------------

        boolean passed =
                score >= config.getPassingMarks();

        // ------------------------------------------------------
        // Update session.
        // ------------------------------------------------------

        session.setTotalQuestions(
                totalQuestions
        );

        session.setAnsweredQuestions(
                answeredQuestions
        );

        session.setCorrectAnswers(
                correctAnswers
        );

        session.setStatus(
                PracticeSessionStatus.COMPLETED
        );

        LocalDateTime completedAt =
                LocalDateTime.now();

        session.setCompletedAt(
                completedAt
        );

        practiceSessionRepository.save(
                session
        );

        // ------------------------------------------------------
        // Return result.
        // ------------------------------------------------------

        return new MockTestResultResponse(
                session.getId(),
                session.getStatus(),
                totalQuestions,
                answeredQuestions,
                correctAnswers,
                incorrectAnswers,
                unansweredQuestions,
                score,
                percentage,
                config.getPassingMarks(),
                passed,
                config.getNegativeMarking(),
                session.getStartedAt(),
                completedAt
        );
    }

    // ==========================================================
    // CALCULATE SCORE
    // ==========================================================

    private double calculateScore(
            int correctAnswers,
            int incorrectAnswers,
            MockTestConfig config) {

        double score =
                correctAnswers;

        /*
         * The current database configuration has negative marking
         * disabled, so incorrect answers do not reduce the score.
         *
         * We intentionally keep the branch here so the scoring
         * behavior can be expanded when a penalty value is added
         * to MockTestConfig.
         */

        if(Boolean.TRUE.equals(
                config.getNegativeMarking()
        )) {

            /*
             * No numeric negative-marking penalty currently exists
             * in MockTestConfig.
             *
             * Until that configuration exists, do not invent one.
             */
        }

        return score;
    }
}