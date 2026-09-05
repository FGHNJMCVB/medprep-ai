package com.medprep.controller;

import com.medprep.dto.AdminBulkQuestionRequest;
import com.medprep.dto.AdminQuestionRequest;

import com.medprep.service.AdminQuestionService;
import com.medprep.service.QuestionBankExpansionService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    private final QuestionBankExpansionService
            questionBankExpansionService;

    public AdminQuestionController(
            AdminQuestionService adminQuestionService,
            QuestionBankExpansionService questionBankExpansionService) {

        this.adminQuestionService =
                adminQuestionService;

        this.questionBankExpansionService =
                questionBankExpansionService;
    }

    // ==========================================================
    // CREATE SINGLE QUESTION
    // ==========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createQuestion(
            @RequestBody AdminQuestionRequest request) {

        return adminQuestionService.createQuestion(
                request
        );
    }

    // ==========================================================
    // CREATE QUESTIONS IN BULK
    // ==========================================================

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public int createQuestionsInBulk(
            @RequestBody AdminBulkQuestionRequest request) {

        return adminQuestionService.createQuestionsInBulk(
                request
        );
    }

    // ==========================================================
    // EXPAND QUESTION BANK
    // ==========================================================

    @PostMapping("/expand")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionBankExpansionService.ExpansionResult
            expandQuestionBank(
                    @RequestParam(
                            defaultValue = "1"
                    ) int maxQuestions) {

        return questionBankExpansionService
                .expandQuestionBank(
                        maxQuestions
                );
    }
}