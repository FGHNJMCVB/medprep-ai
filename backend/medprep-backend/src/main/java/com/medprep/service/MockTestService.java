package com.medprep.service;

import com.medprep.dto.MockTestResponse;
import com.medprep.dto.MockTestStartRequest;
import com.medprep.dto.PracticeSessionResponse;
import com.medprep.dto.QuestionOptionResponse;
import com.medprep.dto.SessionQuestionResponse;

import com.medprep.entity.MockTestConfig;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.User;

import com.medprep.repository.MockTestConfigRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MockTestService {

    private final MockTestConfigRepository mockTestConfigRepository;

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final UserRepository userRepository;

    private final MockTestGenerationService mockTestGenerationService;

    public MockTestService(
            MockTestConfigRepository mockTestConfigRepository,
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            UserRepository userRepository,
            MockTestGenerationService mockTestGenerationService) {

        this.mockTestConfigRepository =
                mockTestConfigRepository;

        this.practiceSessionRepository =
                practiceSessionRepository;

        this.sessionQuestionRepository =
                sessionQuestionRepository;

        this.userRepository =
                userRepository;

        this.mockTestGenerationService =
                mockTestGenerationService;
    }

    // ==========================================================
    // GET FMGE MOCK TEST CONFIGURATION
    // ==========================================================

    @Transactional(readOnly = true)
    public MockTestResponse getFmgeConfig() {

        MockTestConfig config =
                mockTestConfigRepository
                        .findByName("FMGE")
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "FMGE mock test configuration not found"
                                )
                        );

        return new MockTestResponse(
                config.getId(),
                config.getName(),
                config.getTotalQuestions(),
                config.getPartQuestions(),
                config.getPartDurationMinutes(),
                config.getPassingMarks(),
                config.getNegativeMarking()
        );
    }

    // ==========================================================
    // START FMGE MOCK TEST
    // ==========================================================

    @Transactional(
            rollbackFor = Exception.class
    )
    public PracticeSessionResponse startFmgeMockTest(
            MockTestStartRequest request,
            String userEmail) {

        // ------------------------------------------------------
        // Find authenticated user.
        // ------------------------------------------------------

        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated user not found"
                                )
                        );

        // ------------------------------------------------------
        // Validate request.
        // ------------------------------------------------------

        if(request == null) {

            throw new IllegalArgumentException(
                    "Mock test start request is required"
            );
        }

        if(request.getConfigId() == null) {

            throw new IllegalArgumentException(
                    "Mock test configuration ID is required"
            );
        }

        // ------------------------------------------------------
        // Load configuration.
        // ------------------------------------------------------

        MockTestConfig config =
                mockTestConfigRepository
                        .findById(
                                request.getConfigId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Mock test configuration not found: "
                                                + request.getConfigId()
                                )
                        );

        // ------------------------------------------------------
        // Prevent multiple active mock tests.
        // ------------------------------------------------------

        List<PracticeSession> activeSessions =
                practiceSessionRepository
                        .findByUserIdAndStatusOrderByStartedAtDesc(
                                user.getId(),
                                PracticeSessionStatus.IN_PROGRESS
                        );

        boolean activeMockTestExists =
                activeSessions.stream()
                        .anyMatch(session ->
                                session.getType()
                                        == PracticeSessionType.MOCK_TEST
                        );

        if(activeMockTestExists) {

            throw new IllegalArgumentException(
                    "You already have a mock test in progress"
            );
        }

        // ------------------------------------------------------
        // Create empty session.
        // ------------------------------------------------------

        PracticeSession session =
                new PracticeSession(
                        user,
                        PracticeSessionType.MOCK_TEST,
                        PracticeSessionStatus.IN_PROGRESS,
                        null,
                        null,
                        0,
                        0,
                        0,
                        LocalDateTime.now()
                );

        session =
                practiceSessionRepository.save(
                        session
                );

        // ------------------------------------------------------
        // Generate complete exam.
        // ------------------------------------------------------

        mockTestGenerationService.generateMockTest(
                session,
                config.getTotalQuestions()
        );

        // ------------------------------------------------------
        // Reload generated questions.
        // ------------------------------------------------------

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                session.getId()
                        );

        if(sessionQuestions == null ||
           sessionQuestions.isEmpty()) {

            throw new IllegalStateException(
                    "Mock test generation produced no questions"
            );
        }

        // ------------------------------------------------------
        // Build response questions.
        // ------------------------------------------------------

        List<SessionQuestionResponse> responseQuestions =
                buildSessionQuestionResponses(
                        sessionQuestions
                );

        // ------------------------------------------------------
        // Final validation.
        // ------------------------------------------------------

        if(responseQuestions.size()
                != config.getTotalQuestions()) {

            throw new IllegalStateException(
                    "Generated mock test contains "
                            + responseQuestions.size()
                            + " questions, but "
                            + config.getTotalQuestions()
                            + " were required"
            );
        }

        return new PracticeSessionResponse(
                session.getId(),
                session.getType(),
                session.getStatus(),
                session.getSubject() != null
                        ? session.getSubject().getId()
                        : null,
                session.getTopic() != null
                        ? session.getTopic().getId()
                        : null,
                session.getTotalQuestions(),
                session.getAnsweredQuestions(),
                session.getCorrectAnswers(),
                session.getStartedAt(),
                session.getCompletedAt(),
                responseQuestions
        );
    }

    // ==========================================================
    // GET CURRENT IN-PROGRESS MOCK TEST
    // ==========================================================

    @Transactional(readOnly = true)
    public PracticeSessionResponse getCurrentMockTest(
            String userEmail) {

        // ------------------------------------------------------
        // Find authenticated user.
        // ------------------------------------------------------

        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated user not found"
                                )
                        );

        // ------------------------------------------------------
        // Find active sessions.
        // ------------------------------------------------------

        List<PracticeSession> activeSessions =
                practiceSessionRepository
                        .findByUserIdAndStatusOrderByStartedAtDesc(
                                user.getId(),
                                PracticeSessionStatus.IN_PROGRESS
                        );

        // ------------------------------------------------------
        // Find active mock test.
        // ------------------------------------------------------

        PracticeSession currentSession =
                null;

        for(PracticeSession session :
                activeSessions) {

            if(session.getType()
                    == PracticeSessionType.MOCK_TEST) {

                currentSession =
                        session;

                break;
            }
        }

        // ------------------------------------------------------
        // No current mock test.
        // ------------------------------------------------------

        if(currentSession == null) {

            throw new IllegalArgumentException(
                    "No mock test is currently in progress"
            );
        }

        // ------------------------------------------------------
        // Load session questions.
        // ------------------------------------------------------

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                currentSession.getId()
                        );

        if(sessionQuestions == null ||
           sessionQuestions.isEmpty()) {

            throw new IllegalStateException(
                    "Current mock test contains no questions"
            );
        }

        // ------------------------------------------------------
        // Build response questions WITH OPTIONS.
        // ------------------------------------------------------

        List<SessionQuestionResponse> responseQuestions =
                buildSessionQuestionResponses(
                        sessionQuestions
                );

        return new PracticeSessionResponse(
                currentSession.getId(),
                currentSession.getType(),
                currentSession.getStatus(),
                currentSession.getSubject() != null
                        ? currentSession.getSubject().getId()
                        : null,
                currentSession.getTopic() != null
                        ? currentSession.getTopic().getId()
                        : null,
                currentSession.getTotalQuestions(),
                currentSession.getAnsweredQuestions(),
                currentSession.getCorrectAnswers(),
                currentSession.getStartedAt(),
                currentSession.getCompletedAt(),
                responseQuestions
        );
    }

    // ==========================================================
    // BUILD SESSION QUESTION RESPONSES
    // ==========================================================

    private List<SessionQuestionResponse>
    buildSessionQuestionResponses(
            List<SessionQuestion> sessionQuestions) {

        List<SessionQuestionResponse> responseQuestions =
                new ArrayList<>();

        for(SessionQuestion sessionQuestion :
                sessionQuestions) {

            if(sessionQuestion == null ||
               sessionQuestion.getQuestion() == null) {

                throw new IllegalStateException(
                        "Invalid session question encountered"
                );
            }

            // --------------------------------------------------
            // Build option responses.
            //
            // IMPORTANT:
            // We expose:
            // - option ID
            // - label
            // - text
            // - display order
            //
            // We DO NOT expose:
            // - correct
            // --------------------------------------------------

            List<QuestionOptionResponse> optionResponses =
                    new ArrayList<>();

            List<QuestionOption> options =
                    sessionQuestion
                            .getQuestion()
                            .getOptions();

            if(options == null ||
               options.size() != 4) {

                throw new IllegalStateException(
                        "Question "
                                + sessionQuestion
                                        .getQuestion()
                                        .getId()
                                + " must contain exactly 4 options"
                );
            }

            // --------------------------------------------------
            // Sort options consistently.
            // --------------------------------------------------

            List<QuestionOption> sortedOptions =
                    new ArrayList<>(
                            options
                    );

            sortedOptions.sort(
                    Comparator.comparing(
                            QuestionOption::getDisplayOrder
                    )
            );

            // --------------------------------------------------
            // Convert entity → safe DTO.
            // --------------------------------------------------

            for(QuestionOption option :
                    sortedOptions) {

                if(option == null) {

                    throw new IllegalStateException(
                            "Question contains a null option"
                    );
                }

                optionResponses.add(
                        new QuestionOptionResponse(
                                option.getId(),
                                option.getOptionLabel(),
                                option.getOptionText(),
                                option.getDisplayOrder()
                        )
                );
            }

            // --------------------------------------------------
            // Build session-question response.
            // --------------------------------------------------

            responseQuestions.add(
                    new SessionQuestionResponse(
                            sessionQuestion.getId(),
                            sessionQuestion.getDisplayOrder(),
                            sessionQuestion
                                    .getQuestion()
                                    .getId(),
                            sessionQuestion
                                    .getQuestion()
                                    .getQuestionText(),
                            optionResponses
                    )
            );
        }

        return responseQuestions;
    }
}