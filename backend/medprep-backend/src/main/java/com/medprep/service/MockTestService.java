package com.medprep.service;

import com.medprep.dto.MockTestResponse;
import com.medprep.dto.MockTestStartRequest;
import com.medprep.dto.PracticeSessionResponse;
import com.medprep.dto.QuestionOptionResponse;
import com.medprep.dto.SessionQuestionResponse;
import com.medprep.dto.SubjectMockTestStartRequest;

import com.medprep.entity.MockTestConfig;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;
import com.medprep.entity.Question;
import com.medprep.entity.QuestionDifficulty;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.Subject;
import com.medprep.entity.User;

import com.medprep.repository.MockTestConfigRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MockTestService {

    private static final Set<Integer> SUBJECT_MOCK_QUESTION_COUNTS =
            Set.of(50, 100, 150);

    private final MockTestConfigRepository mockTestConfigRepository;

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final UserRepository userRepository;

    private final SubjectRepository subjectRepository;

    private final QuestionRepository questionRepository;

    private final MockTestGenerationService mockTestGenerationService;

    public MockTestService(
            MockTestConfigRepository mockTestConfigRepository,
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            MockTestGenerationService mockTestGenerationService) {

        this.mockTestConfigRepository =
                mockTestConfigRepository;

        this.practiceSessionRepository =
                practiceSessionRepository;

        this.sessionQuestionRepository =
                sessionQuestionRepository;

        this.userRepository =
                userRepository;

        this.subjectRepository =
                subjectRepository;

        this.questionRepository =
                questionRepository;

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
                        .anyMatch(this::isMockTestSession);

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
        // Assemble the complete exam from active question-bank records.
        // ------------------------------------------------------

        mockTestGenerationService.generateMockTest(
                session,
                config.getTotalQuestions()
        );

        // ------------------------------------------------------
        // Reload selected questions.
        // ------------------------------------------------------

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                session.getId()
                        );

        if(sessionQuestions == null ||
           sessionQuestions.isEmpty()) {

            throw new IllegalStateException(
                    "Mock test selection produced no questions"
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
                    "Selected mock test contains "
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
    // START SUBJECT MOCK TEST
    // ==========================================================

    @Transactional
    public PracticeSessionResponse startSubjectMockTest(
            SubjectMockTestStartRequest request,
            String userEmail) {

        if(request == null) {
            throw new IllegalArgumentException(
                    "Subject mock test request is required"
            );
        }

        if(request.getSubjectId() == null) {
            throw new IllegalArgumentException(
                    "Subject ID is required"
            );
        }

        int requestedCount =
                request.getNumberOfQuestions() == null
                        ? 150
                        : request.getNumberOfQuestions();

        if(!SUBJECT_MOCK_QUESTION_COUNTS.contains(requestedCount)) {
            throw new IllegalArgumentException(
                    "Subject mock test must contain 50, 100, or 150 questions"
            );
        }

        User user =
                userRepository
                        .findByEmail(userEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated user not found"
                                )
                        );

        Subject subject =
                subjectRepository
                        .findById(request.getSubjectId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found: "
                                                + request.getSubjectId()
                                )
                        );

        ensureNoActiveMockTest(user);

        List<Question> availableQuestions =
                questionRepository
                        .findBySubjectIdAndActiveTrueOrderByIdAsc(
                                subject.getId()
                        );

        if(availableQuestions.size() < requestedCount) {
            throw new IllegalArgumentException(
                    subject.getName()
                            + " has only "
                            + availableQuestions.size()
                            + " active questions; "
                            + requestedCount
                            + " are required"
            );
        }

        List<Question> selectedQuestions =
                selectSubjectMockQuestions(
                        availableQuestions,
                        requestedCount
                );

        PracticeSession session =
                new PracticeSession(
                        user,
                        PracticeSessionType.SUBJECT_MOCK_TEST,
                        PracticeSessionStatus.IN_PROGRESS,
                        subject,
                        null,
                        selectedQuestions.size(),
                        0,
                        0,
                        LocalDateTime.now()
                );

        session = practiceSessionRepository.save(session);

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

        List<SessionQuestion> sessionQuestions =
                sessionQuestionRepository
                        .findBySessionIdOrderByDisplayOrderAsc(
                                session.getId()
                        );

        return new PracticeSessionResponse(
                session.getId(),
                session.getType(),
                session.getStatus(),
                subject.getId(),
                null,
                session.getTotalQuestions(),
                session.getAnsweredQuestions(),
                session.getCorrectAnswers(),
                session.getStartedAt(),
                session.getCompletedAt(),
                buildSessionQuestionResponses(sessionQuestions)
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

            if(isMockTestSession(session)) {

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

    private void ensureNoActiveMockTest(
            User user) {

        boolean activeMockTestExists =
                practiceSessionRepository
                        .findByUserIdAndStatusOrderByStartedAtDesc(
                                user.getId(),
                                PracticeSessionStatus.IN_PROGRESS
                        )
                        .stream()
                        .anyMatch(this::isMockTestSession);

        if(activeMockTestExists) {
            throw new IllegalArgumentException(
                    "You already have a mock test in progress"
            );
        }
    }

    private boolean isMockTestSession(
            PracticeSession session) {

        return session != null &&
                (session.getType() == PracticeSessionType.MOCK_TEST ||
                 session.getType() == PracticeSessionType.SUBJECT_MOCK_TEST);
    }

    private List<Question> selectSubjectMockQuestions(
            List<Question> availableQuestions,
            int requestedCount) {

        int easyTarget = requestedCount / 5;
        int hardTarget = requestedCount / 5;
        int mediumTarget =
                requestedCount - easyTarget - hardTarget;

        List<Question> selected = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        addBalancedQuestions(
                availableQuestions,
                QuestionDifficulty.EASY,
                easyTarget,
                selected,
                selectedIds
        );

        addBalancedQuestions(
                availableQuestions,
                QuestionDifficulty.MEDIUM,
                mediumTarget,
                selected,
                selectedIds
        );

        addBalancedQuestions(
                availableQuestions,
                QuestionDifficulty.HARD,
                hardTarget,
                selected,
                selectedIds
        );

        if(selected.size() < requestedCount) {
            addBalancedQuestions(
                    availableQuestions,
                    null,
                    requestedCount - selected.size(),
                    selected,
                    selectedIds
            );
        }

        if(selected.size() != requestedCount) {
            throw new IllegalStateException(
                    "Unable to assemble the requested subject mock test"
            );
        }

        Collections.shuffle(selected);

        return selected;
    }

    private void addBalancedQuestions(
            List<Question> availableQuestions,
            QuestionDifficulty difficulty,
            int count,
            List<Question> selected,
            Set<Long> selectedIds) {

        Map<Long, ArrayDeque<Question>> questionsByTopic =
                new LinkedHashMap<>();

        List<Question> shuffled =
                new ArrayList<>(availableQuestions);

        Collections.shuffle(shuffled);

        for(Question question : shuffled) {
            if(question == null ||
               question.getId() == null ||
               question.getTopic() == null ||
               question.getTopic().getId() == null ||
               selectedIds.contains(question.getId()) ||
               (difficulty != null &&
                question.getDifficulty() != difficulty)) {
                continue;
            }

            questionsByTopic
                    .computeIfAbsent(
                            question.getTopic().getId(),
                            ignored -> new ArrayDeque<>()
                    )
                    .add(question);
        }

        while(count > 0 && !questionsByTopic.isEmpty()) {
            boolean addedInRound = false;

            for(ArrayDeque<Question> topicQuestions :
                    questionsByTopic.values()) {

                Question question = topicQuestions.pollFirst();

                if(question == null) {
                    continue;
                }

                selected.add(question);
                selectedIds.add(question.getId());
                count--;
                addedInRound = true;

                if(count == 0) {
                    break;
                }
            }

            if(!addedInRound) {
                break;
            }
        }
    }
}
