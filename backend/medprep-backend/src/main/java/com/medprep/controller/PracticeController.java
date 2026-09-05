package com.medprep.controller;

import com.medprep.dto.MockTestAnswerRequest;
import com.medprep.dto.MockTestAnswerResponse;
import com.medprep.dto.PracticeHistoryResponse;
import com.medprep.dto.PracticeReviewResponse;
import com.medprep.dto.PracticeSessionResponse;
import com.medprep.dto.PracticeSessionResultResponse;
import com.medprep.dto.StartPracticeRequest;

import com.medprep.service.PracticeAnswerService;
import com.medprep.service.PracticeSessionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeSessionService practiceSessionService;
    private final PracticeAnswerService practiceAnswerService;

    public PracticeController(
            PracticeSessionService practiceSessionService,
            PracticeAnswerService practiceAnswerService) {

        this.practiceSessionService =
                practiceSessionService;

        this.practiceAnswerService =
                practiceAnswerService;
    }

    // ==========================================================
    // START PRACTICE
    // ==========================================================

    @PostMapping("/start")
    public ResponseEntity<PracticeSessionResponse> startPractice(
            @RequestBody StartPracticeRequest request,
            Authentication authentication) {

        PracticeSessionResponse response =
                practiceSessionService.startPractice(
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // GET PRACTICE SESSION
    // ==========================================================

    @GetMapping("/{sessionId}")
    public ResponseEntity<PracticeSessionResponse> getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        PracticeSessionResponse response =
                practiceSessionService.getSession(
                        sessionId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // SUBMIT ANSWER
    // ==========================================================

    @PostMapping(
            "/{sessionId}/questions/{sessionQuestionId}/answer"
    )
    public ResponseEntity<MockTestAnswerResponse> submitAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long sessionQuestionId,
            @RequestBody MockTestAnswerRequest request,
            Authentication authentication) {

        MockTestAnswerResponse response =
                practiceAnswerService.submitAnswer(
                        sessionId,
                        sessionQuestionId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // FINISH PRACTICE
    // ==========================================================

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<PracticeSessionResponse> finishSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        PracticeSessionResponse response =
                practiceSessionService.finishSession(
                        sessionId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // RESULT
    // ==========================================================

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<PracticeSessionResultResponse> getResult(
            @PathVariable Long sessionId,
            Authentication authentication) {

        PracticeSessionResultResponse response =
                practiceSessionService.getSessionResult(
                        sessionId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // REVIEW
    // ==========================================================

    @GetMapping("/{sessionId}/review")
    public ResponseEntity<PracticeReviewResponse> getReview(
            @PathVariable Long sessionId,
            Authentication authentication) {

        PracticeReviewResponse response =
                practiceSessionService.getSessionReview(
                        sessionId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/history")
public ResponseEntity<List<PracticeHistoryResponse>> getPracticeHistory(
        Authentication authentication) {

    List<PracticeHistoryResponse> response =
            practiceSessionService.getPracticeHistory(
                    authentication.getName()
            );

    return ResponseEntity.ok(response);
}
}