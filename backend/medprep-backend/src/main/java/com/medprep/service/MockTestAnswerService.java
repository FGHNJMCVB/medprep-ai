package com.medprep.service;

import com.medprep.dto.MockTestAnswerRequest;
import com.medprep.dto.MockTestAnswerResponse;

import com.medprep.entity.Attempt;
import com.medprep.entity.PracticeSession;
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
public class MockTestAnswerService {

    private final PracticeSessionRepository practiceSessionRepository;

    private final SessionQuestionRepository sessionQuestionRepository;

    private final QuestionOptionRepository questionOptionRepository;

    private final AttemptRepository attemptRepository;

    private final UserRepository userRepository;

    public MockTestAnswerService(
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
    // SUBMIT ANSWER
    // ==========================================================

    @Transactional
    public MockTestAnswerResponse submitAnswer(
            Long sessionId,
            Long sessionQuestionId,
            MockTestAnswerRequest request,
            String email) {

        // ------------------------------------------------------
        // Validate request
        // ------------------------------------------------------

        if(request == null) {

            throw new IllegalArgumentException(
                    "Answer request is required"
            );
        }

        if(request.getSelectedOptionId() == null) {

            throw new IllegalArgumentException(
                    "Selected option ID is required"
            );
        }

        int timeTakenSeconds =
                request.getTimeTakenSeconds() == null
                        ? 0
                        : request.getTimeTakenSeconds();

        if(timeTakenSeconds < 0) {

            throw new IllegalArgumentException(
                    "Time taken cannot be negative"
            );
        }

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
        // Find practice session
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
        // Verify session belongs to authenticated user
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
        // Only active sessions accept answers
        // ------------------------------------------------------

        if(!"IN_PROGRESS".equals(
                String.valueOf(
                        session.getStatus()
                ))) {

            throw new IllegalArgumentException(
                    "This mock test is not in progress"
            );
        }

        // ------------------------------------------------------
        // Find session question
        // ------------------------------------------------------

        SessionQuestion sessionQuestion =
                sessionQuestionRepository
                        .findById(sessionQuestionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session question not found: "
                                                + sessionQuestionId
                                )
                        );

        // ------------------------------------------------------
        // Verify question belongs to this session
        // ------------------------------------------------------

        if(sessionQuestion.getSession() == null ||
           sessionQuestion.getSession().getId() == null ||
           !sessionQuestion.getSession()
                   .getId()
                   .equals(sessionId)) {

            throw new IllegalArgumentException(
                    "Session question does not belong to this session"
            );
        }

        // ------------------------------------------------------
        // Prevent answering same question twice
        // ------------------------------------------------------

        if(Boolean.TRUE.equals(
                sessionQuestion.getAnswered()
        )) {

            throw new IllegalArgumentException(
                    "This question has already been answered"
            );
        }

        // ------------------------------------------------------
        // Find selected option
        // ------------------------------------------------------

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

        // ------------------------------------------------------
        // Verify selected option belongs to this question
        // ------------------------------------------------------

        if(selectedOption.getQuestion() == null ||
           selectedOption.getQuestion().getId() == null ||
           !selectedOption.getQuestion()
                   .getId()
                   .equals(
                           sessionQuestion
                                   .getQuestion()
                                   .getId()
                   )) {

            throw new IllegalArgumentException(
                    "Selected option does not belong to this question"
            );
        }

        // ------------------------------------------------------
        // Determine correctness from database
        //
        // Never trust correctness information from client.
        // ------------------------------------------------------

        boolean correct =
                Boolean.TRUE.equals(
                        selectedOption.getCorrect()
                );

        // ------------------------------------------------------
        // Create attempt history record
        // ------------------------------------------------------

        Attempt attempt =
                new Attempt(
                        user,
                        sessionQuestion.getQuestion(),
                        selectedOption,
                        correct,
                        timeTakenSeconds,
                        LocalDateTime.now()
                );

        attemptRepository.save(
                attempt
        );

        // ------------------------------------------------------
        // Mark current session question as answered
        // ------------------------------------------------------

        sessionQuestion.markAnswered(
                correct
        );

        sessionQuestionRepository.save(
                sessionQuestion
        );

        // ------------------------------------------------------
        // Update session counters
        // ------------------------------------------------------

        int answeredQuestions =
                session.getAnsweredQuestions();

        int correctAnswers =
                session.getCorrectAnswers();

        answeredQuestions++;

        if(correct) {
            correctAnswers++;
        }

        session.setAnsweredQuestions(
                answeredQuestions
        );

        session.setCorrectAnswers(
                correctAnswers
        );

        practiceSessionRepository.save(
                session
        );

        // ------------------------------------------------------
        // Return result
        // ------------------------------------------------------

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