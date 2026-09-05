package com.medprep.service;

import com.medprep.dto.MockTestResultResponse;
import com.medprep.dto.MockTestHistoryResponse;

import com.medprep.entity.MockTestConfig;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.User;

import com.medprep.repository.MockTestConfigRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockTestResultService {

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final MockTestConfigRepository mockTestConfigRepository;

    private final UserRepository userRepository;

    public MockTestResultService(
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
    // GET RESULT
    // ==========================================================

    @Transactional(readOnly = true)
    public MockTestResultResponse getResult(
            Long sessionId,
            String email) {

        User user =
                findUser(email);

        PracticeSession session =
                findSession(sessionId);

        validateSessionOwnership(
                session,
                user
        );

        validateMockTest(
                session
        );

        if(session.getStatus()
                != PracticeSessionStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Mock test has not been submitted yet"
            );
        }

        MockTestConfig config =
                getFmgeConfig();

        ResultValues result =
                calculateResult(
                        session,
                        config
                );

        return new MockTestResultResponse(
                session.getId(),
                session.getStatus(),
                result.totalQuestions,
                result.answeredQuestions,
                result.correctAnswers,
                result.incorrectAnswers,
                result.unansweredQuestions,
                result.score,
                result.percentage,
                config.getPassingMarks(),
                result.passed,
                config.getNegativeMarking(),
                session.getStartedAt(),
                session.getCompletedAt()
        );
    }

    // ==========================================================
    // GET HISTORY
    // ==========================================================

    @Transactional(readOnly = true)
    public List<MockTestHistoryResponse> getHistory(
            String email) {

        User user =
                findUser(email);

        MockTestConfig config =
                getFmgeConfig();

        List<PracticeSession> sessions =
                practiceSessionRepository
                        .findByUserIdOrderByStartedAtDesc(
                                user.getId()
                        );

        List<MockTestHistoryResponse> history =
                new ArrayList<>();

        for(PracticeSession session :
                sessions) {

            if(session == null) {
                continue;
            }

            if(session.getType()
                    != PracticeSessionType.MOCK_TEST) {

                continue;
            }

            /*
             * Only completed mock tests belong in history.
             *
             * An active IN_PROGRESS session is not a completed
             * result and is handled through the current session
             * APIs instead.
             */

            if(session.getStatus()
                    != PracticeSessionStatus.COMPLETED) {

                continue;
            }

            ResultValues result =
                    calculateResult(
                            session,
                            config
                    );

            history.add(
                    new MockTestHistoryResponse(
                            session.getId(),
                            session.getStatus(),
                            result.totalQuestions,
                            result.answeredQuestions,
                            result.correctAnswers,
                            result.incorrectAnswers,
                            result.unansweredQuestions,
                            result.score,
                            result.percentage,
                            result.passed,
                            session.getStartedAt(),
                            session.getCompletedAt()
                    )
            );
        }

        return history;
    }

    // ==========================================================
    // CALCULATE RESULT
    // ==========================================================

    private ResultValues calculateResult(
            PracticeSession session,
            MockTestConfig config) {

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                session.getId()
                        );

        int totalQuestions =
                sessionQuestions.size();

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

        int unansweredQuestions =
                totalQuestions -
                        answeredQuestions;

        double score =
                calculateScore(
                        correctAnswers,
                        incorrectAnswers,
                        config
                );

        double percentage =
                totalQuestions > 0
                        ? (score / totalQuestions) * 100.0
                        : 0.0;

        boolean passed =
                score >= config.getPassingMarks();

        return new ResultValues(
                totalQuestions,
                answeredQuestions,
                correctAnswers,
                incorrectAnswers,
                unansweredQuestions,
                score,
                percentage,
                passed
        );
    }

    // ==========================================================
    // SCORE
    // ==========================================================

    private double calculateScore(
            int correctAnswers,
            int incorrectAnswers,
            MockTestConfig config) {

        double score =
                correctAnswers;

        /*
         * Your current FMGE config has:
         *
         * negativeMarking = false
         *
         * Therefore incorrect answers currently contribute
         * zero penalty.
         *
         * There is no numeric penalty field in MockTestConfig,
         * so we must not invent one.
         */

        if(Boolean.TRUE.equals(
                config.getNegativeMarking()
        )) {

            // No numeric penalty configured yet.
        }

        return score;
    }

    // ==========================================================
    // FIND USER
    // ==========================================================

    private User findUser(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    // ==========================================================
    // FIND SESSION
    // ==========================================================

    private PracticeSession findSession(
            Long sessionId) {

        return practiceSessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Practice session not found: "
                                        + sessionId
                        )
                );
    }

    // ==========================================================
    // VALIDATE OWNERSHIP
    // ==========================================================

    private void validateSessionOwnership(
            PracticeSession session,
            User user) {

        if(session.getUser() == null ||
           session.getUser().getId() == null ||
           !session.getUser()
                   .getId()
                   .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You do not have access to this session"
            );
        }
    }

    // ==========================================================
    // VALIDATE MOCK TEST
    // ==========================================================

    private void validateMockTest(
            PracticeSession session) {

        if(session.getType()
                != PracticeSessionType.MOCK_TEST) {

            throw new IllegalArgumentException(
                    "This session is not a mock test"
            );
        }
    }

    // ==========================================================
    // GET FMGE CONFIG
    // ==========================================================

    private MockTestConfig getFmgeConfig() {

        return mockTestConfigRepository
                .findByName("FMGE")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "FMGE mock test configuration not found"
                        )
                );
    }

    // ==========================================================
    // INTERNAL RESULT HOLDER
    // ==========================================================

    private static class ResultValues {

        private final int totalQuestions;

        private final int answeredQuestions;

        private final int correctAnswers;

        private final int incorrectAnswers;

        private final int unansweredQuestions;

        private final double score;

        private final double percentage;

        private final boolean passed;

        private ResultValues(
                int totalQuestions,
                int answeredQuestions,
                int correctAnswers,
                int incorrectAnswers,
                int unansweredQuestions,
                double score,
                double percentage,
                boolean passed) {

            this.totalQuestions =
                    totalQuestions;

            this.answeredQuestions =
                    answeredQuestions;

            this.correctAnswers =
                    correctAnswers;

            this.incorrectAnswers =
                    incorrectAnswers;

            this.unansweredQuestions =
                    unansweredQuestions;

            this.score =
                    score;

            this.percentage =
                    percentage;

            this.passed =
                    passed;
        }
    }
}