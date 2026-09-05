package com.medprep.controller;

import com.medprep.dto.QuestionResponse;
import com.medprep.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/topics/{topicId}/questions")
    public List<QuestionResponse> getQuestionsByTopic(
            @PathVariable Long topicId) {

        return questionService.getQuestionsByTopic(topicId);
    }

    @GetMapping("/subjects/{subjectId}/questions")
    public List<QuestionResponse> getQuestionsBySubject(
            @PathVariable Long subjectId) {

        return questionService.getQuestionsBySubject(subjectId);
    }
}