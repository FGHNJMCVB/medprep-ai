package com.medprep.service;

import com.medprep.dto.MockTestAnswerRequest;
import com.medprep.dto.MockTestAnswerResponse;

import com.medprep.entity.Attempt;
import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionType;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.SessionQuestion;
import com.medprep.entity.User;

import com.medprep.repository.AttemptRepository;
import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.QuestionOptionRepository;
import com.medprep.repository.SessionQuestionRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PracticeAnswerService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public PracticeAnswerService(
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository,
            QuestionOptionRepository questionOptionRepository,
            AttemptRepository attemptRepository,
            UserRepository userRepository) {

        this.practiceSessionRepository =
                practiceSessionRepository;

        this.sessionQuestionRepository =
                sessionQuestionRepository;

        this.questionOptionRepository =
                questionOptionRepository;

        this.attemptRepository =
                attemptRepository;

        this.userRepository =
                userRepository;
    }

    // ==========================================================
    // SUBMIT PRACTICE ANSWER
    // ==========================================================

    @Transactional
    public MockTestAnswerResponse submitAnswer(
            Long sessionId,
            Long sessionQuestionId,
            MockTestAnswerRequest request,
            String email) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Answer request is required"
            );
        }

        if (request.getSelectedOptionId() == null) {
            throw new IllegalArgumentException(
                    "Selected option ID is required"
            );
        }

        int timeTakenSeconds =
                request.getTimeTakenSeconds() == null
                        ? 0
                        : request.getTimeTakenSeconds();

        if (timeTakenSeconds < 0) {
            throw new IllegalArgumentException(
                    "Time taken cannot be negative"
            );
        }

        // ======================================================
        // USER
        // ======================================================

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        // ======================================================
        // SESSION
        // ======================================================

        PracticeSession session =
                practiceSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Practice session not found: "
                                                + sessionId
                                )
                        );

        // ======================================================
        // OWNERSHIP
        // ======================================================

        if (session.getUser() == null ||
                session.getUser().getId() == null ||
                !session.getUser()
                        .getId()
                        .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "You do not have access to this session"
            );
        }

        // ======================================================
        // SESSION TYPE
        // ======================================================

        if (session.getType() !=
                PracticeSessionType.PRACTICE) {

            throw new IllegalArgumentException(
                    "This is not a practice session"
            );
        }

        // ======================================================
        // SESSION STATUS
        // ======================================================

        if (!"IN_PROGRESS".equals(
                String.valueOf(
                        session.getStatus()
                )
        )) {

            throw new IllegalArgumentException(
                    "This practice session is not in progress"
            );
        }

        // ======================================================
        // SESSION QUESTION
        // ======================================================

        SessionQuestion sessionQuestion =
                sessionQuestionRepository
                        .findById(sessionQuestionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session question not found: "
                                                + sessionQuestionId
                                )
                        );

        // ======================================================
        // SESSION QUESTION OWNERSHIP
        // ======================================================

        if (sessionQuestion.getSession() == null ||
                sessionQuestion.getSession().getId() == null ||
                !sessionQuestion.getSession()
                        .getId()
                        .equals(sessionId)) {

            throw new IllegalArgumentException(
                    "Session question does not belong to "
                            + "this practice session"
            );
        }

        // ======================================================
        // PREVENT DOUBLE ANSWER
        // ======================================================

        if (Boolean.TRUE.equals(
                sessionQuestion.getAnswered())) {

            throw new IllegalArgumentException(
                    "This question has already been answered"
            );
        }

        // ======================================================
        // SELECTED OPTION
        // ======================================================

        QuestionOption selectedOption =
                questionOptionRepository
                        .findById(
                                request.getSelectedOptionId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Selected option not found: "
                                                + request
                                                .getSelectedOptionId()
                                )
                        );

        // ======================================================
        // OPTION MUST BELONG TO QUESTION
        // ======================================================

        if (selectedOption.getQuestion() == null ||
                selectedOption.getQuestion().getId() == null ||
                !selectedOption.getQuestion()
                        .getId()
                        .equals(
                                sessionQuestion
                                        .getQuestion()
                                        .getId()
                        )) {

            throw new IllegalArgumentException(
                    "Selected option does not belong to "
                            + "this question"
            );
        }

        // ======================================================
        // DETERMINE CORRECTNESS FROM DATABASE
        // ======================================================

        boolean correct =
                Boolean.TRUE.equals(
                        selectedOption.getCorrect()
                );

        // ======================================================
        // SAVE ATTEMPT
        // ======================================================

        Attempt attempt =
                new Attempt(
                        user,
                        sessionQuestion.getQuestion(),
                        selectedOption,
                        correct,
                        timeTakenSeconds,
                        LocalDateTime.now()
                );

        attemptRepository.save(attempt);

        // ======================================================
        // MARK SESSION QUESTION ANSWERED
        // ======================================================

        sessionQuestion.markAnswered(correct);

        sessionQuestionRepository.save(
                sessionQuestion
        );

        // ======================================================
        // UPDATE SESSION COUNTERS
        // ======================================================

        int answeredQuestions =
                session.getAnsweredQuestions();

        int correctAnswers =
                session.getCorrectAnswers();

        answeredQuestions++;

        if (correct) {
            correctAnswers++;
        }

        session.setAnsweredQuestions(
                answeredQuestions
        );

        session.setCorrectAnswers(
                correctAnswers
        );

        practiceSessionRepository.save(session);

        // ======================================================
        // RESPONSE
        // ======================================================

        return new MockTestAnswerResponse(
                session.getId(),
                sessionQuestion.getId(),
                sessionQuestion
                        .getQuestion()
                        .getId(),
                selectedOption.getId(),
                correct,
                answeredQuestions,
                correctAnswers,
                session.getTotalQuestions()
        );
    }
}