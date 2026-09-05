package com.medprep.controller;

import com.medprep.service.TopicQuestionBankService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/question-bank")
public class QuestionBankAdminController {

    private final TopicQuestionBankService
            topicQuestionBankService;

    public QuestionBankAdminController(
            TopicQuestionBankService topicQuestionBankService) {

        this.topicQuestionBankService =
                topicQuestionBankService;
    }

    // ==========================================================
    // GET ALL TOPIC STATUS
    // ==========================================================

    @GetMapping("/status")
    public List<TopicQuestionBankService.TopicStatus>
    getAllTopicStatus(
            @RequestParam(
                    name = "target",
                    defaultValue = "20"
            )
            int target) {

        return topicQuestionBankService
                .getAllTopicStatus(target);
    }

    // ==========================================================
    // GET SINGLE TOPIC STATUS
    // ==========================================================

    @GetMapping("/status/topic/{topicId}")
    public TopicQuestionBankService.TopicStatus
    getTopicStatus(
            @PathVariable Long topicId,
            @RequestParam(
                    name = "target",
                    defaultValue = "20"
            )
            int target) {

        return topicQuestionBankService
                .getTopicStatus(
                        topicId,
                        target
                );
    }

    // ==========================================================
    // FILL SINGLE TOPIC
    // ==========================================================

    @PostMapping("/fill/topic/{topicId}")
    public TopicQuestionBankService.TopicFillResult
    fillTopic(
            @PathVariable Long topicId,
            @RequestParam(
                    name = "target",
                    defaultValue = "20"
            )
            int target) {

        return topicQuestionBankService
                .fillTopic(
                        topicId,
                        target
                );
    }

    // ==========================================================
    // FILL ALL TOPICS
    // ==========================================================

    @PostMapping("/fill/all")
    public List<TopicQuestionBankService.TopicFillResult>
    fillAllTopics(
            @RequestParam(
                    name = "target",
                    defaultValue = "20"
            )
            int target) {

        return topicQuestionBankService
                .fillAllTopics(target);
    }
}