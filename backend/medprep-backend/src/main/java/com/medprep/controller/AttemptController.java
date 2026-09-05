package com.medprep.controller;

import com.medprep.dto.SubmitAnswerRequest;
import com.medprep.dto.SubmitAnswerResponse;
import com.medprep.service.AttemptService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/{questionId}/attempts")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @PathVariable Long questionId,
            @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {

        SubmitAnswerResponse response =
                attemptService.submitAnswer(
                        questionId,
                        request,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
    
}