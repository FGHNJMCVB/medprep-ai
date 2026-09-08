package com.medprep.controller;

import com.medprep.service.QuestionBankGenerationService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/question-bank")
public class QuestionBankAdminController {

    private final QuestionBankGenerationService
            questionBankGenerationService;

    public QuestionBankAdminController(
            QuestionBankGenerationService questionBankGenerationService) {

        this.questionBankGenerationService =
                questionBankGenerationService;
    }

    // ==========================================================
    // GET ALL TOPIC STATUS
    // ==========================================================

    @GetMapping("/status")
    public List<Map<String, Object>>
    getAllTopicStatus() {

        return questionBankGenerationService
                .getAllTopicProgress();
    }

    // ==========================================================
    // FILL SINGLE TOPIC
    // ==========================================================

    @PostMapping("/fill/topic/{topicId}")
    public Map<String, Object>
    fillTopic(
            @PathVariable Long topicId) {

        return questionBankGenerationService
                .generateMissingForTopic(topicId);
    }

    // ==========================================================
    // FILL MULTIPLE TOPICS
    // ==========================================================

    @PostMapping("/fill/all")
    public Map<String, Object>
    fillAllTopics(
            @RequestParam(
                    name = "maxTopics",
                    defaultValue = "1"
            )
            int maxTopics) {

        return questionBankGenerationService
                .generateMissingForTopics(maxTopics);
    }
}