package com.medprep.controller;

import com.medprep.dto.PracticeSessionResponse;
import com.medprep.dto.PracticeSessionResultResponse;
import com.medprep.dto.StartPracticeRequest;
import com.medprep.service.PracticeSessionService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practice-sessions")
public class PracticeSessionController {

    private final PracticeSessionService practiceSessionService;

    public PracticeSessionController(
            PracticeSessionService practiceSessionService) {

        this.practiceSessionService = practiceSessionService;
    }

    // ==========================================================
    // START PRACTICE SESSION
    // ==========================================================

    @PostMapping
    public PracticeSessionResponse startPractice(
            @RequestBody StartPracticeRequest request,
            Authentication authentication) {

        return practiceSessionService.startPractice(
                request,
                authentication.getName());
    }

    // ==========================================================
    // GET PRACTICE SESSION
    // ==========================================================

    @GetMapping("/{sessionId}")
    public PracticeSessionResponse getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        return practiceSessionService.getSession(
                sessionId,
                authentication.getName());
    }

    // ==========================================================
    // FINISH PRACTICE SESSION
    // ==========================================================

    @PostMapping("/{sessionId}/finish")
    public PracticeSessionResponse finishSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        return practiceSessionService.finishSession(
                sessionId,
                authentication.getName());
    }

    // ==========================================================
    // GET PRACTICE SESSION RESULT
    // ==========================================================

    @GetMapping("/{sessionId}/result")
    public PracticeSessionResultResponse getSessionResult(
            @PathVariable Long sessionId,
            Authentication authentication) {

        return practiceSessionService.getSessionResult(
                sessionId,
                authentication.getName());
    }
}