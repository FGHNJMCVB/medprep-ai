package com.medprep.ai;

import com.medprep.dto.QuestionGenerationRequest;

public interface AiQuestionGenerator {

    String generateQuestions(
            QuestionGenerationRequest request,
            String promptContext
    );
}