package com.medprep.controller;

import com.medprep.dto.MockTestAnswerRequest;
import com.medprep.dto.MockTestAnswerResponse;
import com.medprep.dto.MockTestResponse;
import com.medprep.dto.MockTestStartRequest;
import com.medprep.dto.PracticeSessionResponse;

import com.medprep.service.MockTestAnswerService;
import com.medprep.service.MockTestBlueprintService;
import com.medprep.service.MockTestService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.medprep.dto.MockTestResultResponse;
import com.medprep.service.MockTestSubmissionService;

import com.medprep.dto.MockTestHistoryResponse;
import com.medprep.service.MockTestResultService;

import java.util.List;

@RestController
@RequestMapping("/api/mock-tests")
public class MockTestController {

    private final MockTestService mockTestService;
    private final MockTestBlueprintService mockTestBlueprintService;
    private final MockTestAnswerService mockTestAnswerService;
    private final MockTestSubmissionService mockTestSubmissionService;
    private final MockTestResultService mockTestResultService;

    public MockTestController(
            MockTestService mockTestService,
            MockTestBlueprintService mockTestBlueprintService,
            MockTestAnswerService mockTestAnswerService,
            MockTestSubmissionService mockTestSubmissionService,
            MockTestResultService mockTestResultService) {

        this.mockTestService = mockTestService;

        this.mockTestBlueprintService = mockTestBlueprintService;

        this.mockTestAnswerService = mockTestAnswerService;

        this.mockTestSubmissionService = mockTestSubmissionService;
        this.mockTestResultService = mockTestResultService;
    }

    // ==========================================================
    // GET FMGE MOCK TEST CONFIGURATION
    // ==========================================================

    @GetMapping("/config")
    public MockTestResponse getFmgeConfig() {

        return mockTestService.getFmgeConfig();
    }

    // ==========================================================
    // START FMGE MOCK TEST
    // ==========================================================

    @PostMapping("/start")
    public PracticeSessionResponse startFmgeMockTest(
            @RequestBody MockTestStartRequest request,
            Authentication authentication) {

        return mockTestService.startFmgeMockTest(
                request,
                authentication.getName());
    }

    // ==========================================================
    // PREVIEW MOCK TEST BLUEPRINT
    // ==========================================================

    @GetMapping("/blueprint")
    public List<MockTestBlueprintService.GenerationBlock> previewBlueprint(
            @RequestParam(defaultValue = "300") int questions) {

        return mockTestBlueprintService.previewBlueprint(
                questions);
    }

    // ==========================================================
    // SUBMIT ANSWER
    // ==========================================================

    @PostMapping("/{sessionId}/questions/{sessionQuestionId}/answer")
    public MockTestAnswerResponse submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long sessionQuestionId,
            @RequestBody MockTestAnswerRequest request,
            Authentication authentication) {

        return mockTestAnswerService.submitAnswer(
                sessionId,
                sessionQuestionId,
                request,
                authentication.getName());
    }
    // ==========================================================
    // SUBMIT MOCK TEST
    // ==========================================================

    @PostMapping("/{sessionId}/submit")
    public MockTestResultResponse submitMockTest(
            @PathVariable Long sessionId,
            Authentication authentication) {

        return mockTestSubmissionService.submitMockTest(
                sessionId,
                authentication.getName());
    }
    // ==========================================================
    // GET COMPLETED MOCK TEST RESULT
    // ==========================================================

    @GetMapping("/{sessionId}/result")
    public MockTestResultResponse getMockTestResult(
            @PathVariable Long sessionId,
            Authentication authentication) {

        return mockTestResultService.getResult(
                sessionId,
                authentication.getName());
    }
    // ==========================================================
    // GET MOCK TEST HISTORY
    // ==========================================================

    @GetMapping("/history")
    public List<MockTestHistoryResponse> getMockTestHistory(
            Authentication authentication) {

        return mockTestResultService.getHistory(
                authentication.getName());
    }
    // ==========================================================
    // GET CURRENT MOCK TEST
    // ==========================================================

    @GetMapping("/current")
    public PracticeSessionResponse getCurrentMockTest(
            Authentication authentication) {

        return mockTestService.getCurrentMockTest(
                authentication.getName());
    }
}