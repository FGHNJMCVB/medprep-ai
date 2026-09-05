package com.medprep.service;

import com.medprep.dto.PracticeHistoryResponse;
import com.medprep.dto.PracticeReviewQuestionResponse;
import com.medprep.dto.PracticeReviewResponse;
import com.medprep.dto.PracticeSessionResponse;
import com.medprep.dto.PracticeSessionResultResponse;
import com.medprep.dto.QuestionOptionResponse;
import com.medprep.dto.SessionQuestionResponse;
import com.medprep.dto.StartPracticeRequest;

import com.medprep.entity.Attempt;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.PracticeSessionType;
import com.medprep.entity.Question;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;
import com.medprep.entity.User;

import com.medprep.repository.AttemptRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PracticeSessionService {

        private final AttemptRepository attemptRepository;

        private final PracticeSessionRepository practiceSessionRepository;
        private final SessionQuestionRepository sessionQuestionRepository;
        private final QuestionRepository questionRepository;
        private final SubjectRepository subjectRepository;
        private final TopicRepository topicRepository;
        private final UserRepository userRepository;

        public PracticeSessionService(
                        AttemptRepository attemptRepository,
                        PracticeSessionRepository practiceSessionRepository,
                        SessionQuestionRepository sessionQuestionRepository,
                        QuestionRepository questionRepository,
                        SubjectRepository subjectRepository,
                        TopicRepository topicRepository,
                        UserRepository userRepository) {

                this.attemptRepository = attemptRepository;

                this.practiceSessionRepository = practiceSessionRepository;

                this.sessionQuestionRepository = sessionQuestionRepository;

                this.questionRepository = questionRepository;

                this.subjectRepository = subjectRepository;

                this.topicRepository = topicRepository;

                this.userRepository = userRepository;
        }

        // ==========================================================
        // START PRACTICE
        // ==========================================================

        @Transactional
        public PracticeSessionResponse startPractice(
                        StartPracticeRequest request,
                        String userEmail) {

                User user = userRepository
                                .findByEmail(userEmail)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Authenticated user not found"));

                if (request == null) {

                        throw new IllegalArgumentException(
                                        "Practice request is required");
                }

                if (request.getNumberOfQuestions() == null ||
                                request.getNumberOfQuestions() <= 0) {

                        throw new IllegalArgumentException(
                                        "Number of questions must be greater than zero");
                }

                if (request.getSubjectId() == null &&
                                request.getTopicId() == null) {

                        throw new IllegalArgumentException(
                                        "Subject ID or Topic ID is required");
                }

                if (request.getSubjectId() != null &&
                                request.getTopicId() != null) {

                        throw new IllegalArgumentException(
                                        "Provide either subjectId or topicId, not both");
                }

                Subject subject = null;
                Topic topic = null;

                List<Question> questions;

                // ======================================================
                // TOPIC PRACTICE
                // ======================================================

                if (request.getTopicId() != null) {

                        topic = topicRepository
                                        .findById(request.getTopicId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Topic not found: "
                                                                        + request.getTopicId()));

                        subject = topic.getSubject();

                        questions = questionRepository
                                        .findByTopicIdAndActiveTrueOrderByIdAsc(
                                                        topic.getId());
                }

                // ======================================================
                // SUBJECT PRACTICE
                // ======================================================

                else {

                        subject = subjectRepository
                                        .findById(request.getSubjectId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Subject not found: "
                                                                        + request.getSubjectId()));

                        questions = questionRepository
                                        .findBySubjectIdAndActiveTrueOrderByIdAsc(
                                                        subject.getId());
                }

                if (questions.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "No questions available for the selected "
                                                        + "subject or topic");
                }

                // ======================================================
                // RANDOMIZE QUESTIONS
                // ======================================================

                List<Question> selectedQuestions = new ArrayList<>(questions);

                Collections.shuffle(selectedQuestions);

                int requestedCount = request.getNumberOfQuestions();

                if (requestedCount < selectedQuestions.size()) {

                        selectedQuestions = new ArrayList<>(
                                        selectedQuestions.subList(
                                                        0,
                                                        requestedCount));
                }

                // ======================================================
                // CREATE SESSION
                // ======================================================

                PracticeSession session = new PracticeSession(
                                user,
                                PracticeSessionType.PRACTICE,
                                PracticeSessionStatus.IN_PROGRESS,
                                subject,
                                topic,
                                selectedQuestions.size(),
                                0,
                                0,
                                LocalDateTime.now());

                session = practiceSessionRepository.save(session);

                // ======================================================
                // CREATE SESSION QUESTIONS
                // ======================================================

                int displayOrder = 1;

                for (Question question : selectedQuestions) {

                        SessionQuestion sessionQuestion = new SessionQuestion(
                                        session,
                                        question,
                                        displayOrder++);

                        sessionQuestionRepository.save(
                                        sessionQuestion);
                }

                return buildResponse(session);
        }

        // ==========================================================
        // GET SESSION
        // ==========================================================

        @Transactional(readOnly = true)
        public PracticeSessionResponse getSession(
                        Long sessionId,
                        String userEmail) {

                PracticeSession session = getOwnedSession(
                                sessionId,
                                userEmail);

                return buildResponse(session);
        }

        // ==========================================================
        // FINISH SESSION
        // ==========================================================

        @Transactional
        public PracticeSessionResponse finishSession(
                        Long sessionId,
                        String userEmail) {

                PracticeSession session = getOwnedSession(
                                sessionId,
                                userEmail);

                if (session.getStatus() == PracticeSessionStatus.COMPLETED) {

                        return buildResponse(session);
                }

                long answered = sessionQuestionRepository
                                .countBySessionIdAndAnsweredTrue(
                                                sessionId);

                long correct = sessionQuestionRepository
                                .countBySessionIdAndCorrectTrue(
                                                sessionId);

                session.setAnsweredQuestions(
                                (int) answered);

                session.setCorrectAnswers(
                                (int) correct);

                session.setStatus(
                                PracticeSessionStatus.COMPLETED);

                session.setCompletedAt(
                                LocalDateTime.now());

                practiceSessionRepository.save(
                                session);

                return buildResponse(session);
        }

        // ==========================================================
        // GET PRACTICE RESULT
        // ==========================================================

        @Transactional(readOnly = true)
        public PracticeSessionResultResponse getSessionResult(
                        Long sessionId,
                        String userEmail) {

                PracticeSession session = getOwnedSession(
                                sessionId,
                                userEmail);

                long answered = sessionQuestionRepository
                                .countBySessionIdAndAnsweredTrue(
                                                sessionId);

                long correct = sessionQuestionRepository
                                .countBySessionIdAndCorrectTrue(
                                                sessionId);

                int total = session.getTotalQuestions();

                int wrong = (int) answered -
                                (int) correct;

                int unanswered = total -
                                (int) answered;

                double accuracy = 0.0;

                if (answered > 0) {

                        accuracy = ((double) correct /
                                        answered) *
                                        100.0;
                }

                accuracy = Math.round(
                                accuracy * 100.0) / 100.0;

                return new PracticeSessionResultResponse(
                                session.getId(),
                                session.getStatus().name(),
                                total,
                                (int) answered,
                                (int) correct,
                                wrong,
                                unanswered,
                                accuracy);
        }

        // ==========================================================
        // GET PRACTICE HISTORY
        // ==========================================================

        @Transactional(readOnly = true)
        public List<PracticeHistoryResponse> getPracticeHistory(
                        String userEmail) {

                // ------------------------------------------------------
                // Find authenticated user.
                // ------------------------------------------------------

                User user = userRepository
                                .findByEmail(userEmail)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Authenticated user not found"));

                // ------------------------------------------------------
                // Load completed practice sessions only.
                //
                // Ordered from newest to oldest.
                // ------------------------------------------------------

                List<PracticeSession> sessions = practiceSessionRepository
                                .findByUserIdAndTypeAndStatusOrderByCompletedAtDesc(
                                                user.getId(),
                                                PracticeSessionType.PRACTICE,
                                                PracticeSessionStatus.COMPLETED);

                List<PracticeHistoryResponse> history = new ArrayList<>();

                // ------------------------------------------------------
                // Build history response.
                // ------------------------------------------------------

                for (PracticeSession session : sessions) {

                        if (session == null) {
                                continue;
                        }

                        int totalQuestions = session.getTotalQuestions();

                        int answeredQuestions = session.getAnsweredQuestions();

                        int correctAnswers = session.getCorrectAnswers();

                        int wrongAnswers = answeredQuestions -
                                        correctAnswers;

                        int unansweredQuestions = totalQuestions -
                                        answeredQuestions;

                        double accuracy = 0.0;

                        if (answeredQuestions > 0) {

                                accuracy = ((double) correctAnswers /
                                                answeredQuestions) *
                                                100.0;

                                accuracy = Math.round(
                                                accuracy * 100.0) / 100.0;
                        }

                        history.add(
                                        new PracticeHistoryResponse(
                                                        session.getId(),

                                                        session.getSubject() != null
                                                                        ? session.getSubject().getId()
                                                                        : null,

                                                        session.getTopic() != null
                                                                        ? session.getTopic().getId()
                                                                        : null,

                                                        totalQuestions,
                                                        answeredQuestions,
                                                        correctAnswers,
                                                        wrongAnswers,
                                                        unansweredQuestions,
                                                        accuracy,

                                                        session.getStartedAt() != null
                                                                        ? session.getStartedAt().toString()
                                                                        : null,

                                                        session.getCompletedAt() != null
                                                                        ? session.getCompletedAt().toString()
                                                                        : null));
                }

                return history;
        }

        // ==========================================================
        // GET PRACTICE REVIEW
        // ==========================================================

        @Transactional(readOnly = true)
        public PracticeReviewResponse getSessionReview(
                        Long sessionId,
                        String userEmail) {

                // ------------------------------------------------------
                // Verify that the session belongs to the user.
                // ------------------------------------------------------

                PracticeSession session = getOwnedSession(
                                sessionId,
                                userEmail);

                // ------------------------------------------------------
                // Review should only be available after completion.
                // ------------------------------------------------------

                if (session.getStatus() != PracticeSessionStatus.COMPLETED) {

                        throw new IllegalArgumentException(
                                        "Practice session is not completed yet");
                }

                // ------------------------------------------------------
                // Load session questions in the original order.
                // ------------------------------------------------------

                List<SessionQuestion> sessionQuestions = sessionQuestionRepository
                                .findBySessionIdOrderByDisplayOrderAsc(
                                                sessionId);

                List<PracticeReviewQuestionResponse> reviewQuestions = new ArrayList<>();

                int answeredQuestions = 0;
                int correctAnswers = 0;

                LocalDateTime startedAt = session.getStartedAt();

                LocalDateTime completedAt = session.getCompletedAt();

                // ------------------------------------------------------
                // Build review question by question.
                // ------------------------------------------------------

                for (SessionQuestion sessionQuestion : sessionQuestions) {

                        if (sessionQuestion == null ||
                                        sessionQuestion.getQuestion() == null) {

                                continue;
                        }

                        Question question = sessionQuestion.getQuestion();

                        // ==================================================
                        // BUILD OPTIONS
                        // ==================================================

                        List<QuestionOptionResponse> optionResponses = new ArrayList<>();

                        List<QuestionOption> sortedOptions = question.getOptions() == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(
                                                        question.getOptions());

                        sortedOptions.sort(
                                        Comparator.comparing(
                                                        option -> option.getDisplayOrder() == null
                                                                        ? 0
                                                                        : option.getDisplayOrder()));

                        QuestionOption correctOption = null;

                        for (QuestionOption option : sortedOptions) {

                                if (option == null) {
                                        continue;
                                }

                                // --------------------------------------------------
                                // Safe option response.
                                //
                                // DO NOT expose "correct" here.
                                // --------------------------------------------------

                                optionResponses.add(
                                                new QuestionOptionResponse(
                                                                option.getId(),
                                                                option.getOptionLabel(),
                                                                option.getOptionText(),
                                                                option.getDisplayOrder()));

                                // --------------------------------------------------
                                // Find the correct option for review.
                                // --------------------------------------------------

                                if (Boolean.TRUE.equals(
                                                option.getCorrect())) {

                                        correctOption = option;
                                }
                        }

                        // ==================================================
                        // FIND USER ATTEMPT FOR THIS SESSION
                        // ==================================================

                        Optional<Attempt> attemptOptional = Optional.empty();

                        if (session.getUser() != null &&
                                        session.getUser().getId() != null &&
                                        startedAt != null &&
                                        completedAt != null) {

                                attemptOptional = attemptRepository
                                                .findTopByUserIdAndQuestionIdAndAttemptedAtBetweenOrderByAttemptedAtDesc(
                                                                session.getUser().getId(),
                                                                question.getId(),
                                                                startedAt,
                                                                completedAt);
                        }

                        // ==================================================
                        // DEFAULT REVIEW VALUES
                        // ==================================================

                        Long selectedOptionId = null;

                        String selectedOptionLabel = null;

                        String selectedOptionText = null;

                        Boolean correct = null;

                        Integer timeTakenSeconds = null;

                        // ==================================================
                        // ATTEMPT EXISTS
                        // ==================================================

                        if (attemptOptional.isPresent()) {

                                Attempt attempt = attemptOptional.get();

                                answeredQuestions++;

                                if (Boolean.TRUE.equals(
                                                attempt.getCorrect())) {

                                        correctAnswers++;
                                }

                                // ----------------------------------------------
                                // Selected option
                                // ----------------------------------------------

                                if (attempt.getSelectedOption() != null) {

                                        selectedOptionId = attempt.getSelectedOption()
                                                        .getId();

                                        selectedOptionLabel = attempt.getSelectedOption()
                                                        .getOptionLabel();

                                        selectedOptionText = attempt.getSelectedOption()
                                                        .getOptionText();
                                }

                                // ----------------------------------------------
                                // Correctness
                                // ----------------------------------------------

                                correct = attempt.getCorrect();

                                // ----------------------------------------------
                                // Time taken
                                // ----------------------------------------------

                                timeTakenSeconds = attempt.getTimeTakenSeconds();
                        }

                        // ==================================================
                        // CREATE REVIEW QUESTION RESPONSE
                        // ==================================================

                        reviewQuestions.add(
                                        new PracticeReviewQuestionResponse(
                                                        sessionQuestion.getId(),
                                                        sessionQuestion.getDisplayOrder(),
                                                        question.getId(),
                                                        question.getQuestionText(),
                                                        question.getExplanation(),

                                                        selectedOptionId,
                                                        selectedOptionLabel,
                                                        selectedOptionText,

                                                        correctOption != null
                                                                        ? correctOption.getId()
                                                                        : null,

                                                        correctOption != null
                                                                        ? correctOption.getOptionLabel()
                                                                        : null,

                                                        correctOption != null
                                                                        ? correctOption.getOptionText()
                                                                        : null,

                                                        correct,
                                                        timeTakenSeconds,
                                                        optionResponses));
                }

                // ======================================================
                // REVIEW SUMMARY
                // ======================================================

                int totalQuestions = session.getTotalQuestions();

                int wrongAnswers = answeredQuestions -
                                correctAnswers;

                int unansweredQuestions = totalQuestions -
                                answeredQuestions;

                double accuracy = 0.0;

                if (answeredQuestions > 0) {

                        accuracy = ((double) correctAnswers /
                                        answeredQuestions) *
                                        100.0;

                        accuracy = Math.round(
                                        accuracy * 100.0) / 100.0;
                }

                // ======================================================
                // RETURN COMPLETE REVIEW
                // ======================================================

                return new PracticeReviewResponse(
                                session.getId(),
                                session.getStatus().name(),
                                totalQuestions,
                                answeredQuestions,
                                correctAnswers,
                                wrongAnswers,
                                unansweredQuestions,
                                accuracy,
                                reviewQuestions);
        }

        // ==========================================================
        // VERIFY SESSION OWNERSHIP
        // ==========================================================

        private PracticeSession getOwnedSession(
                        Long sessionId,
                        String userEmail) {

                PracticeSession session = practiceSessionRepository
                                .findById(sessionId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Practice session not found: "
                                                                + sessionId));

                if (session.getUser() == null ||
                                !session.getUser()
                                                .getEmail()
                                                .equalsIgnoreCase(userEmail)) {

                        throw new IllegalArgumentException(
                                        "Practice session does not belong to "
                                                        + "the authenticated user");
                }

                return session;
        }

        // ==========================================================
        // BUILD RESPONSE
        // ==========================================================

        private PracticeSessionResponse buildResponse(
                        PracticeSession session) {

                List<SessionQuestion> sessionQuestions = sessionQuestionRepository
                                .findBySessionIdOrderByDisplayOrderAsc(
                                                session.getId());

                List<SessionQuestionResponse> questions = new ArrayList<>();

                for (SessionQuestion sessionQuestion : sessionQuestions) {

                        List<QuestionOptionResponse> optionResponses = new ArrayList<>();

                        List<QuestionOption> options = sessionQuestion
                                        .getQuestion()
                                        .getOptions();

                        if (options != null) {

                                List<QuestionOption> sortedOptions = new ArrayList<>(options);

                                sortedOptions.sort(
                                                Comparator.comparing(
                                                                option -> option.getDisplayOrder() == null
                                                                                ? 0
                                                                                : option.getDisplayOrder()));

                                for (QuestionOption option : sortedOptions) {

                                        optionResponses.add(
                                                        new QuestionOptionResponse(
                                                                        option.getId(),
                                                                        option.getOptionLabel(),
                                                                        option.getOptionText(),
                                                                        option.getDisplayOrder()));
                                }
                        }

                        questions.add(
                                        new SessionQuestionResponse(
                                                        sessionQuestion.getId(),
                                                        sessionQuestion.getDisplayOrder(),
                                                        sessionQuestion
                                                                        .getQuestion()
                                                                        .getId(),
                                                        sessionQuestion
                                                                        .getQuestion()
                                                                        .getQuestionText(),
                                                        optionResponses));
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

                                questions);
        }
}