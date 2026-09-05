package com.medprep.controller;

import com.medprep.dto.AdminQuestionTrendBulkRequest;
import com.medprep.dto.AdminQuestionTrendRequest;
import com.medprep.service.AdminQuestionTrendService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/question-trends")
public class AdminQuestionTrendController {

    private final AdminQuestionTrendService adminQuestionTrendService;

    public AdminQuestionTrendController(
            AdminQuestionTrendService adminQuestionTrendService) {

        this.adminQuestionTrendService =
                adminQuestionTrendService;
    }

    // ==========================================================
    // CREATE / UPDATE SINGLE TREND
    // ==========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long saveTrend(
            @RequestBody AdminQuestionTrendRequest request) {

        return adminQuestionTrendService.saveTrend(
                request
        );
    }

    // ==========================================================
    // CREATE / UPDATE TRENDS IN BULK
    // ==========================================================

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public int saveTrendsInBulk(
            @RequestBody AdminQuestionTrendBulkRequest request) {

        return adminQuestionTrendService.saveTrendsInBulk(
                request
        );
    }
}