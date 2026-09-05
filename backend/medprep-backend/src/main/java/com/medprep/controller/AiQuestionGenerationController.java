package com.medprep.controller;

import com.medprep.dto.QuestionGenerationRequest;
import com.medprep.service.AiQuestionGenerationService;
import com.medprep.service.QuestionBankGenerationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/questions")
public class AiQuestionGenerationController {

    private final AiQuestionGenerationService aiQuestionGenerationService;

    private final QuestionBankGenerationService questionBankGenerationService;

    public AiQuestionGenerationController(
            AiQuestionGenerationService aiQuestionGenerationService,
            QuestionBankGenerationService questionBankGenerationService) {

        this.aiQuestionGenerationService =
                aiQuestionGenerationService;

        this.questionBankGenerationService =
                questionBankGenerationService;
    }

    // ==========================================================
    // GENERATE QUESTIONS
    // ==========================================================

    @PostMapping("/generate")
    public ResponseEntity<List<Long>> generateQuestions(
            @RequestBody QuestionGenerationRequest request) {

        List<Long> questionIds =
                aiQuestionGenerationService
                        .generateAndSaveQuestions(
                                request
                        );

        return ResponseEntity.ok(
                questionIds
        );
    }

    // ==========================================================
    // QUESTION BANK STATUS
    // ==========================================================

    @GetMapping("/bank/status")
    public ResponseEntity<List<Map<String, Object>>> getBankStatus() {

        return ResponseEntity.ok(
                questionBankGenerationService
                        .getAllTopicProgress()
        );
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS FOR ONE TOPIC
    // ==========================================================

    @PostMapping("/bank/generate/topic/{topicId}")
    public ResponseEntity<Map<String, Object>> generateTopicBank(
            @PathVariable Long topicId) {

        return ResponseEntity.ok(
                questionBankGenerationService
                        .generateMissingForTopic(
                                topicId
                        )
        );
    }

    // ==========================================================
    // GENERATE MISSING QUESTIONS FOR MULTIPLE TOPICS
    // ==========================================================

    @PostMapping("/bank/generate-missing")
    public ResponseEntity<Map<String, Object>> generateMissing(
            @RequestParam(
                    defaultValue = "1"
            )
            int maxTopics) {

        return ResponseEntity.ok(
                questionBankGenerationService
                        .generateMissingForTopics(
                                maxTopics
                        )
        );
    }
}