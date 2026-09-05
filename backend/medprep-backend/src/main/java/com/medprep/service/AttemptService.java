package com.medprep.service;

import com.medprep.dto.SubmitAnswerRequest;
import com.medprep.dto.SubmitAnswerResponse;
import com.medprep.entity.Attempt;
import com.medprep.entity.Question;
import com.medprep.entity.QuestionOption;
import com.medprep.entity.User;
import com.medprep.repository.AttemptRepository;
import com.medprep.repository.QuestionOptionRepository;
import com.medprep.repository.QuestionRepository;
import com.medprep.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.medprep.entity.PracticeSession;
import com.medprep.entity.PracticeSessionStatus;
import com.medprep.entity.SessionQuestion;

import com.medprep.repository.PracticeSessionRepository;
import com.medprep.repository.SessionQuestionRepository;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final UserRepository userRepository;

    private final PracticeSessionRepository practiceSessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;

    public AttemptService(
            AttemptRepository attemptRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            UserRepository userRepository,
            PracticeSessionRepository practiceSessionRepository,
            SessionQuestionRepository sessionQuestionRepository) {

        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.userRepository = userRepository;
        this.practiceSessionRepository = practiceSessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
    }

    @Transactional
    public SubmitAnswerResponse submitAnswer(
            Long questionId,
            SubmitAnswerRequest request,
            String userEmail) {

        if (request.getOptionId() == null) {
            throw new IllegalArgumentException(
                    "Option ID is required");
        }

        if (request.getTimeTakenSeconds() == null ||
                request.getTimeTakenSeconds() < 0) {

            throw new IllegalArgumentException(
                    "Time taken must be zero or greater");
        }

        // ----------------------------------------------------------
        // Get the logged-in user from JWT authentication
        // ----------------------------------------------------------

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user not found"));

        // ----------------------------------------------------------
        // Find question
        // ----------------------------------------------------------

        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found: "
                                + questionId));

        // ----------------------------------------------------------
        // Find selected option
        // ----------------------------------------------------------

        QuestionOption selectedOption = questionOptionRepository
                .findById(request.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option not found: "
                                + request.getOptionId()));

        // ----------------------------------------------------------
        // IMPORTANT:
        // Make sure option belongs to this question.
        // ----------------------------------------------------------

        if (!selectedOption.getQuestion()
                .getId()
                .equals(question.getId())) {

            throw new IllegalArgumentException(
                    "Selected option does not belong to this question");
        }

        // ----------------------------------------------------------
        // Check answer
        // ----------------------------------------------------------

        boolean isCorrect = Boolean.TRUE.equals(
                selectedOption.getCorrect());
        // ----------------------------------------------------------
        // Update Practice Session if this answer belongs to a session
        // ----------------------------------------------------------

        if (request.getSessionId() != null) {

            PracticeSession session = practiceSessionRepository
                    .findById(request.getSessionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Practice session not found: "
                                    + request.getSessionId()));

            // Make sure this session belongs to the logged-in user
            if (!session.getUser()
                    .getEmail()
                    .equalsIgnoreCase(userEmail)) {

                throw new IllegalArgumentException(
                        "Practice session does not belong to "
                                + "the authenticated user");
            }

            // A completed session cannot accept another answer
            if (session.getStatus() != PracticeSessionStatus.IN_PROGRESS) {

                throw new IllegalArgumentException(
                        "Practice session is already completed");
            }

            // Find this question inside the session
            SessionQuestion sessionQuestion = sessionQuestionRepository
                    .findBySessionIdAndQuestionId(
                            session.getId(),
                            questionId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Question does not belong "
                                    + "to this practice session"));

            // Prevent answering the same session question twice
            if (Boolean.TRUE.equals(
                    sessionQuestion.getAnswered())) {

                throw new IllegalArgumentException(
                        "Question has already been answered");
            }

            // Mark session question
            sessionQuestion.markAnswered(isCorrect);

            sessionQuestionRepository.save(sessionQuestion);

            // Recalculate session progress
            long answered = sessionQuestionRepository
                    .countBySessionIdAndAnsweredTrue(
                            session.getId());

            long correct = sessionQuestionRepository
                    .countBySessionIdAndCorrectTrue(
                            session.getId());

            session.setAnsweredQuestions(
                    (int) answered);

            session.setCorrectAnswers(
                    (int) correct);

            practiceSessionRepository.save(session);
        }

        // ----------------------------------------------------------
        // Save attempt
        // ----------------------------------------------------------

        Attempt attempt = new Attempt(
                user,
                question,
                selectedOption,
                isCorrect,
                request.getTimeTakenSeconds(),
                LocalDateTime.now());

        Attempt savedAttempt = attemptRepository.save(attempt);

        // ----------------------------------------------------------
        // Find correct option
        // ----------------------------------------------------------

        List<QuestionOption> correctOptions = questionOptionRepository
                .findByQuestionIdAndCorrectTrue(
                        questionId);

        if (correctOptions.isEmpty()) {

            throw new IllegalStateException(
                    "No correct option configured for question: "
                            + questionId);
        }

        QuestionOption correctOption = correctOptions.get(0);

        return new SubmitAnswerResponse(
                savedAttempt.getId(),
                isCorrect,

                selectedOption.getId(),
                selectedOption.getOptionLabel(),

                correctOption.getId(),
                correctOption.getOptionLabel(),

                question.getExplanation());
    }
}